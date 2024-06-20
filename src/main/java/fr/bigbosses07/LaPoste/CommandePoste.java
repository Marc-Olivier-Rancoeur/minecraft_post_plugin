package fr.bigbosses07.LaPoste;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class CommandePoste implements CommandExecutor {

    private final LaPoste laPoste;
    private Player commandPlayer;

    public CommandePoste(LaPoste laPoste) {
        this.laPoste = laPoste;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            commandPlayer = (Player) sender;
            if(args.length > 0) {
                switch(args[0].toLowerCase()) {
                    case "address":
                        addressHandler(args);
                        break;
                    case "box":
                        boxHandler(args);
                        break;
                    case "op":
                        opHandler(args, true);
                        break;
                    case "deop":
                        opHandler(args, false);
                        break;
                    default:
                        helpHandler();
                        break;
                }
            }
        }
        return true;
    }

    private void addressHandler(String[] args){
        if(args.length > 2) {
            Player player;
            if((player = laPoste.getPlayerFromName(args[2])) != null) {
                switch (args[1].toLowerCase()) {
                    case "list":
                        Set<String> addressesSet = laPoste.getPlayerAddresses(player.getName());
                        if(addressesSet.isEmpty()){
                            commandPlayer.sendMessage("Ce joueur ne dispose pas d'adresse");
                        }else {
                            commandPlayer.sendMessage("Liste des " + addressesSet.size() + " adresses de : §7" + player.getName() + "§f");
                            for (String address : addressesSet) {
                                commandPlayer.sendMessage(laPoste.getConfig().getString("players." + player.getName() + ".addresses." + address + ".description") + " : Courriers : " + laPoste.getConfig().getString("players." + player.getName() + ".addresses." + address + ".mails"));
                            }
                        }
                        break;
                    case "add":
                        if(args.length == 5 || args.length == 6){
                            if(!doAddressExist(player, args[3])){
                                laPoste.getConfig().set("players."+player.getName()+".nbAddresses", getNbAddresses(player)+1);
                                laPoste.getConfig().set("players."+player.getName()+".addresses."+args[3]+".mails", 0);
                                laPoste.getConfig().set("players."+player.getName()+".addresses."+args[3]+".description", args[4]);
                                laPoste.getConfig().set("players."+player.getName()+".addresses."+args[3]+".chest.isSet", false);
                                laPoste.saveConf();
                                commandPlayer.sendMessage("Ajout de l'adresse : "+ args[3] + " au joueur : " + player.getName() + " avec succès");
                                if(args.length == 6 && args[5].equalsIgnoreCase("true")){
                                    // Go directly to chest
                                }else{
                                    break;
                                }
                            }else{
                                commandPlayer.sendMessage("Ce joueur possède déjà une adresse nommée : " + args[3]);
                                break;
                            }
                        }else {
                            break;
                        }
                    case "chest":
                        if(args.length > 3) {
                            if (doAddressExist(player, args[3]) && !addressHaveChest(player, args[3])) {
                                Location location = commandPlayer.getLocation();

                                setChest(location, true);

                                laPoste.getConfig().set("players." + player.getName() + ".addresses." + args[3] + ".chest.isSet", true);
                                laPoste.getConfig().set("players." + player.getName() + ".addresses." + args[3] + ".chest.x", location.getBlockX());
                                laPoste.getConfig().set("players." + player.getName() + ".addresses." + args[3] + ".chest.y", location.getBlockY());
                                laPoste.getConfig().set("players." + player.getName() + ".addresses." + args[3] + ".chest.z", location.getBlockZ());
                                laPoste.saveConf();
                                commandPlayer.sendMessage("Ajout du coffre pour l'adresse : " + args[3] + " pour le joueur " + player.getName() + " avec succès");
                            } else {
                                commandPlayer.sendMessage("L'adresse indiquée n'existe pas ou elle dispose déjà une boite");
                            }
                        }else{
                            commandPlayer.sendMessage("La commande est malformée");
                        }
                        break;
                    case "remove":
                        if(args.length == 4){
                            if(doAddressExist(player, args[3])) {
                                laPoste.getConfig().set("players."+player.getName()+".addresses."+args[3], null);
                                laPoste.getConfig().set("players."+player.getName()+".nbAddresses", getNbAddresses(player)-1);
                                laPoste.saveConf();
                                commandPlayer.sendMessage("L'adresse " + args[3] + " de " + player.getName() + " n'existe plus");
                            }else{
                                commandPlayer.sendMessage("L'adresse indiquée n'existe pas, \"/poste address list <player>\" pour obtenir la liste des adresses");
                            }
                        }
                        break;
                    default:
                        commandPlayer.sendMessage("La commande est malformée");
                        break;
                }
            }else{
                commandPlayer.sendMessage("Le joueur indiqué n'existe pas");
            }
        }
    }

    private void boxHandler(String[] args){
        if(args.length > 1){
            if(args[1].equalsIgnoreCase("list")) {
                Set<String> boxesSet = laPoste.getBoxesAddresses();
                if(boxesSet.isEmpty()){
                    commandPlayer.sendMessage("Il n'y a pas de boite postale");
                }else{
                    commandPlayer.sendMessage("Liste des " + boxesSet.size() + " Boites postales :");
                    for(String boxAddress : boxesSet){
                        commandPlayer.sendMessage(boxAddress + " : " + laPoste.getConfig().getInt("boxes." + boxAddress + ".mails"));
                    }
                }
            }else if(args.length == 4) {
                if (args[1].equalsIgnoreCase("add")) {
                    if (doBoxExist(args[2])) {
                        commandPlayer.sendMessage("Cette adresse existe déjà");
                    } else {
                        Location location = commandPlayer.getLocation();
                        setChest(location, false);

                        laPoste.getConfig().set("boxes." + args[2] + ".mails", 0);
                        laPoste.getConfig().set("boxes." + args[2] + ".description", args[3]);
                        laPoste.getConfig().set("boxes." + args[2] + ".x", location.getBlockX());
                        laPoste.getConfig().set("boxes." + args[2] + ".y", location.getBlockY());
                        laPoste.getConfig().set("boxes." + args[2] + ".z", location.getBlockZ());
                        laPoste.saveConf();

                        commandPlayer.sendMessage("La boite postale " + args[2] + " a été ajoutée avec succès");
                    }
                } else if (args[1].equalsIgnoreCase("remove")) {
                    if (doBoxExist(args[2])) {
                        laPoste.getConfig().set("boxes." + args[2], null);
                        laPoste.saveConf();
                        commandPlayer.sendMessage("La boite postale " + args[2] + " a été supprimée avec succès");
                    } else {
                        commandPlayer.sendMessage("Cette adresse n'existe pas");
                    }
                } else {
                    commandPlayer.sendMessage("La commande est malformée");
                }
            }else{
                commandPlayer.sendMessage("La commande est malformée");
            }
        }else{
            commandPlayer.sendMessage("La commande est malformée");
        }
    }

    private void opHandler(String[] args, boolean opValue) {
        if (args.length == 2) {
            if (opValue) {
                if (laPoste.getPlayerFromName(args[1]) != null) {
                    if (laPoste.isOp(commandPlayer)) {
                        commandPlayer.sendMessage(args[1] + " est déjà administrateur de la poste");
                    } else {
                        List<String> list = laPoste.getOpList();
                        list.add(args[1]);
                        laPoste.getConfig().set("ops", list);
                        laPoste.saveConf();
                        commandPlayer.sendMessage(args[1] + " est désormais administrateur de la poste");
                    }
                }
            } else {
                if (laPoste.isOp(commandPlayer)) {
                    List<String> list = laPoste.getOpList();
                    list.remove(args[1]);
                    laPoste.getConfig().set("ops", list);
                    laPoste.saveConf();
                    commandPlayer.sendMessage(args[1] + " n'est plus administrateur de la poste");
                } else {
                    commandPlayer.sendMessage(args[1] + " n'est pas administrateur de la poste");

                }
            }
        } else {
            commandPlayer.sendMessage("La commande est malformée");
        }
    }

    private void helpHandler(){
        commandPlayer.sendMessage("Utilisation du plugin de la poste :");
        commandPlayer.sendMessage(" - Créer des adresses -");
        commandPlayer.sendMessage("/poste address add <player> <name> <description> <chest>");
        commandPlayer.sendMessage("<player> : le nom du joueur");
        commandPlayer.sendMessage("<name> : le nom de l'adresse sans espace");
        commandPlayer.sendMessage("<description> : la description avec des _ affichée avec des espaces");
        commandPlayer.sendMessage("<chest> : true/false si le coffre doit être installé en même temps");
    }

    private boolean doAddressExist(Player player, String addr){
        Object object = laPoste.getConfig().get("players." + player.getName() + ".addresses." + addr);
        return object != null;
    }

    private boolean doBoxExist(String addr){
        Object object = laPoste.getConfig().get("boxes." + addr);
        return object != null;
    }

    private int getNbAddresses(Player player){
        return laPoste.getConfig().getInt("players." + player.getName() + ".nbAddresses");
    }
    private boolean addressHaveChest(Player player, String addr){
        return laPoste.getConfig().getBoolean("players."+player.getName()+".addresses."+addr+".chest.isSet");
    }

    private void setChest(Location location, boolean lamp){
        Block chestBlock = location.getBlock();
        chestBlock.setType(Material.CHEST);

        int lampx = 0;
        int lampz = 0;
        float yaw = location.getYaw();

        BlockData chestBlockData = chestBlock.getBlockData();
        if (yaw < -135 || yaw > 135) {
            ((Directional) chestBlockData).setFacing(BlockFace.SOUTH);
            lampz = -1;
        } else if (yaw >= -135 && yaw < -45) {
            ((Directional) chestBlockData).setFacing(BlockFace.WEST);
            lampx = 1;
        } else if (yaw <= 135 && yaw > 45) {
            ((Directional) chestBlockData).setFacing(BlockFace.EAST);
            lampx = -1;
        } else {
            ((Directional) chestBlockData).setFacing(BlockFace.NORTH);
            lampz = 1;
        }

        chestBlock.setBlockData(chestBlockData);

        if(lamp) {
            Block lampBlock = commandPlayer.getWorld().getBlockAt(location.getBlockX() + lampx, location.getBlockY(), location.getBlockZ() + lampz);
            lampBlock.setType(Material.REDSTONE_LAMP);
        }
    }
}
