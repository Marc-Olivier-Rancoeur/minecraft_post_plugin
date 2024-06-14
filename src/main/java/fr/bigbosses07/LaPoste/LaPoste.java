package fr.bigbosses07.LaPoste;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LaPoste extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("poste").setExecutor(new CommandePoste(this));
        getCommand("poste").setTabCompleter(new tabCompleterPoste(this));
        getServer().getPluginManager().registerEvents(new ListenerPoste(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("onDisable is called!");
    }

    public void saveConf() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public Set<String> getPlayerAddresses(String playerName) {
        ConfigurationSection configurationSection = getConfig().getConfigurationSection("players." + playerName + ".addresses");
        if (configurationSection == null) {
            return new HashSet<>();
        } else {
            return configurationSection.getKeys(false);
        }
    }

    public Set<String> getBoxesAddresses() {
        ConfigurationSection configurationSection = getConfig().getConfigurationSection("boxes");
        if (configurationSection == null) {
            return new HashSet<>();
        } else {
            return configurationSection.getKeys(false);
        }
    }

    public Player getPlayerFromName(String playerName) {
        return Bukkit.getPlayer(playerName);
    }

    public List<String> getOpList(){
        return getConfig().getStringList("ops");
    }
    public boolean isOp(Player player){
        return isOp(player.getName());
    }
    public boolean isOp(String playerName){
        return getConfig().getStringList("ops").contains(playerName);
    }
}
