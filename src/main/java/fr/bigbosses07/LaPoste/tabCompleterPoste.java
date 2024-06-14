package fr.bigbosses07.LaPoste;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class tabCompleterPoste implements TabCompleter {

    private final LaPoste laPoste;

    public tabCompleterPoste(LaPoste laPoste) {
        this.laPoste = laPoste;
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String msg, String[] args){

        int length = args.length;
        List<String> list = new ArrayList<>();

        if(length == 1) {
            list.add("address");
            list.add("box");
            list.add("op");
            list.add("deop");
        }
        else if(length == 2) {
            if(args[0].equalsIgnoreCase("address")) {

                list.add("add");
                list.add("remove");
                list.add("list");
                list.add("chest");

            } else if (args[0].equalsIgnoreCase("box")) {

                list.add("add");
                list.add("remove");
                list.add("list");
            }else if(args[0].equalsIgnoreCase("op")){
                List<String> opPlayers = laPoste.getOpList();
                for(Player player : Bukkit.getOnlinePlayers()){
                    if(!opPlayers.contains(player.getName())) {
                        list.add(player.getName());
                    }
                }
            }else if(args[0].equalsIgnoreCase("deop")){
                List<String> opPlayers = laPoste.getOpList();
                list.addAll(opPlayers);
            }
        }
        else if(length == 3) {
            if(args[0].equalsIgnoreCase("address")) {
                for(Player player : Bukkit.getOnlinePlayers()){
                    list.add(player.getName());
                }
            }
        }
        else if(length == 4) {
            if(args[0].equalsIgnoreCase("address")) {
                if(args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("chest")) {
                    if(laPoste.getPlayerFromName(args[2]) != null) {
                        Set<String> playerAddresses = laPoste.getPlayerAddresses(args[2]);
                        list.addAll(playerAddresses);
                    }
                }
            }
        }
        else if(length == 5) {}
        else if(length == 6) {
            if(args[1].equalsIgnoreCase("add")) {
                String[] completion = {"true", "false"};
                return new ArrayList<>(Arrays.asList(completion));
            }
        }
        return list;
    }

}
