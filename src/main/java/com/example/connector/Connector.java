package com.example.connector;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Connector extends JavaPlugin {
    private CommandServer commandServer;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();
        
        // Initialize config manager
        configManager = new ConfigManager(this);
        
        // Start command server
        commandServer = new CommandServer(this);
        commandServer.start();
        
        getLogger().info("Connector plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (commandServer != null) {
            commandServer.stop();
        }
        getLogger().info("Connector plugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("connector")) {
            if (!sender.hasPermission("connector.admin")) {
                sender.sendMessage("§cYou don't have permission to use this command!");
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                configManager.reloadConfig();
                commandServer.restart();
                sender.sendMessage("§aConnector configuration reloaded!");
                return true;
            }
        }
        return false;
    }
} 