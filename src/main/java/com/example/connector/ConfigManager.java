package com.example.connector;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final Connector plugin;
    private FileConfiguration config;

    public ConfigManager(Connector plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public int getPort() {
        return config.getInt("port", 25566);
    }

    public String getPassword() {
        return config.getString("password", "your_secure_password_here");
    }

    public int getMaxFailedAttempts() {
        return config.getInt("max_failed_attempts", 5);
    }

    public int getBanDuration() {
        return config.getInt("ban_duration", 30);
    }
} 