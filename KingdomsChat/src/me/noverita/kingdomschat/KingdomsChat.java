package me.noverita.kingdomschat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class KingdomsChat extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getLogger().info(ChatColor.GREEN + "Enabled " + this.getName());
        Bukkit.getPluginManager().registerEvents(MessageHandler.getInstance(), this);

        ChangeChannelCommand ccc = new ChangeChannelCommand();
        getCommand("ch").setExecutor(ccc);
        getCommand("ch").setTabCompleter(ccc);

        NameColorCommand ncc = new NameColorCommand();
        getCommand("namecolor").setExecutor(ncc);
        getCommand("namecolor").setExecutor(ncc);

        getCommand("em").setExecutor(new EmphasisCommand());
        getCommand("roll").setExecutor(new DiceRollCommand());
        getCommand("chleave").setExecutor(new ChannelLeaveCommand());

        try {
            ConfigLoader.loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MessageHandler.getInstance().reloadHandler();
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(ChatColor.RED + "Disabled " + this.getName());
    }
}
