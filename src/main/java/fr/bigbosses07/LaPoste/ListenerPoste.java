package fr.bigbosses07.LaPoste;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Lightable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class ListenerPoste implements Listener {

    private final LaPoste laPoste;

    public ListenerPoste(LaPoste laPoste){
        this.laPoste = laPoste;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(getNbAddresses(player) > 0){
            int mails = 0;
            player.sendMessage("§7SPFF -- Poste");
            for (String address : laPoste.getConfig().getConfigurationSection("players." + player.getName() + ".addresses").getKeys(false)){
                int nbMails = laPoste.getConfig().getInt("players." + player.getName() + ".addresses." + address + ".mails");
                if(nbMails > 0){
                    player.sendMessage(" Vous avez " + nbMails + " courriers chez vous : §7" + laPoste.getConfig().getString("players." + player.getName() + ".addresses." + address + ".description"));
                    mails += nbMails;
                }
            }
            if(mails == 0) {
                player.sendMessage("§7 Vous n'avez reçu aucun courrier.");
            }else {
                player.sendMessage("§c Vous avez reçu " + mails + " courriers en tout.");
            }
        }
    }

    @EventHandler
    public void clic(PlayerInteractEvent event){
        Action action = event.getAction();
        if(action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK){
            Block block = event.getClickedBlock();
            if(block != null && block.getType() == Material.CHEST){
                BoxInfos boxInfos = getBox(block);
                if(boxInfos != null){
                    Player player = event.getPlayer();
                    if(action == Action.RIGHT_CLICK_BLOCK){
                        ItemStack item = event.getItem();
                        Chest chest = (Chest) block.getState();
                        Inventory chestInventory = chest.getBlockInventory();
                        if(item != null && item.getType() == Material.WRITTEN_BOOK){
                            event.setCancelled(true);
                            ItemStack itemCopy = item.clone();
                            itemCopy.setAmount(1);
                            int availablePosition = getInventoryAvailablePosition(chestInventory, item);
                            if(availablePosition == -1){
                                player.sendMessage("Impossible de poster ce courrier. La boite est pleine ou un courrier similaire se trouve déjà dans la boite");
                            }else{
                                item.setAmount(item.getAmount()-1);
                                chestInventory.setItem(availablePosition, itemCopy);
                                player.sendMessage("Votre courrier a bien été posté !");
                                addOneMail(boxInfos);
                                updateLight(block);
                                laPoste.saveConf();
                            }
                        }else if(player.getName().equals(boxInfos.playerName)){
                            player.sendMessage("Bienvenue dans votre boite aux lettres");
                            setMailNumber(boxInfos, 0);
                            updateLight(block, false);
                            laPoste.saveConf();
                        }else if(isOp(player)){
                            player.sendMessage("Boite aux lettres de : " + boxInfos.playerName);
                            if(mailBoxIsEmpty(chestInventory)){
                                setMailNumber(boxInfos, 0);
                                updateLight(block, false);
                                laPoste.saveConf();
                            }
                        }else{
                            event.setCancelled(true);
                            player.sendMessage("Vous n'avez pas accès a cette boite aux lettres");

                        }
                    }else{
                        if(player.getName().equals(boxInfos.playerName)) {
                            player.sendMessage("Suppression de votre boite aux lettres. En cas de mauvaise manipulation, contactez un administrateur");
                            destroyMailBox(boxInfos, block);
                            laPoste.saveConf();
                        }else if(isOp(player)){
                            player.sendMessage("Suppression de la boite aux lettres de : " + boxInfos.playerName);
                            destroyMailBox(boxInfos, block);
                            laPoste.saveConf();
                        }else{
                            event.setCancelled(true);
                            player.sendMessage("Vous n'avez pas la permission de détruire cette boite aux lettres. Contactez le propriétaire de la boite ou un administrateur en cas de problème");
                        }
                    }
                }
            }
        }
    }

    private BoxInfos getBox(Block block){
        Location location = block.getLocation();
        ConfigurationSection playersConfigurationSection = laPoste.getConfig().getConfigurationSection("players");
        if(playersConfigurationSection != null){
            for(String playerName : playersConfigurationSection.getKeys(false)){
                ConfigurationSection addressesConfigurationSection = laPoste.getConfig().getConfigurationSection("players." + playerName + ".addresses");
                if(addressesConfigurationSection != null){
                    for(String address : addressesConfigurationSection.getKeys(false)){
                        if(laPoste.getConfig().getBoolean("players." + playerName + ".addresses." + address + ".chest.isSet")) {
                            int x = laPoste.getConfig().getInt("players." + playerName + ".addresses." + address + ".chest.x");
                            int y = laPoste.getConfig().getInt("players." + playerName + ".addresses." + address + ".chest.y");
                            int z = laPoste.getConfig().getInt("players." + playerName + ".addresses." + address + ".chest.z");
                            if(location.getBlockX() == x && location.getBlockY() == y && location.getBlockZ() == z){
                                return new BoxInfos(playerName, address);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private int getInventoryAvailablePosition(Inventory inventory, ItemStack item){
        for(int i = 0 ; i < 27 ; i++){
            if(inventory.getItem(i) == null){
                return i;
            }else if(inventory.getItem(i).isSimilar(item)){
                return -1;
            }
        }
        return -1;
    }

    private void updateLight(Block chestBlock){
        updateLight(chestBlock, true);
    }

    private void updateLight(Block chestBlock, boolean value){
        Location location = chestBlock.getLocation();
        Directional directional = (Directional)chestBlock.getBlockData();
        BlockFace blockface = directional.getFacing();
        int lampx = 0;
        int lampz = 0;
        if(blockface == BlockFace.NORTH) {
            lampz = 1;
        }else if(blockface == BlockFace.WEST){
            lampx = 1;
        }else if(blockface == BlockFace.SOUTH){
            lampz = -1;
        }else if(blockface == BlockFace.EAST){
            lampx = -1;
        }
        Block lampBlock = chestBlock.getWorld().getBlockAt(location.add(lampx, 0, lampz));
        lampBlock.setType(Material.REDSTONE_LAMP);
        Lightable lightable = (Lightable)lampBlock.getBlockData();
        lightable.setLit(value);
        lampBlock.setBlockData(lightable);
    }

    private boolean isOp(Player player){
        return laPoste.getConfig().getInt("ops."+player.getName()) == 1;
    }

    private int getMailNumber(BoxInfos boxInfos){
        return laPoste.getConfig().getInt("players." + boxInfos.playerName + ".addresses." + boxInfos.address + ".mails");
    }

    private void setMailNumber(BoxInfos boxInfos, int mailNumber){
        laPoste.getConfig().set("players." + boxInfos.playerName + ".addresses." + boxInfos.address + ".mails", mailNumber);
    }

    private void addOneMail(BoxInfos boxInfos){
        setMailNumber(boxInfos, getMailNumber(boxInfos)+1);
    }

    private int getNbAddresses(Player player){
        return laPoste.getConfig().getInt("players." + player.getName() + ".nbAddresses");
    }

    private void destroyMailBox(BoxInfos boxInfos, Block chestBlock){
        Location location = chestBlock.getLocation();
        laPoste.getConfig().set("players." + boxInfos.playerName + ".addresses." + boxInfos.address + ".chest.isSet", false);
    }

    private boolean mailBoxIsEmpty(Inventory inventory){
        for(int i = 0 ; i < 27 ; i++){
            if(inventory.getItem(i) != null){
                return false;
            }
        }
        return true;
    }


    private static class BoxInfos{
        String playerName;
        String address;
        public BoxInfos(String playerName, String address){
            this.playerName = playerName;
            this.address = address;
        }
    }
}
