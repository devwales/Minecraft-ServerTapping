package com.example.connector;

import com.google.gson.Gson;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandServer {
    private final Connector plugin;
    private final Gson gson;
    private ServerSocket serverSocket;
    private boolean running;
    private final Map<String, Integer> failedAttempts;
    private final Map<String, Long> ipBans;

    public CommandServer(Connector plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
        this.failedAttempts = new ConcurrentHashMap<>();
        this.ipBans = new ConcurrentHashMap<>();
    }

    public void start() {
        running = true;
        new Thread(this::run).start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error closing server socket: " + e.getMessage());
        }
    }

    public void restart() {
        stop();
        start();
    }

    private void run() {
        try {
            serverSocket = new ServerSocket(plugin.getConfig().getInt("port", 25566));
            plugin.getLogger().info("Command server listening on port " + serverSocket.getLocalPort());

            while (running) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (Exception e) {
            if (running) {
                plugin.getLogger().severe("Error in command server: " + e.getMessage());
            }
        }
    }

    private void handleClient(Socket socket) {
        String clientIP = socket.getInetAddress().getHostAddress();

        try {
            if (isIPBanned(clientIP)) {
                socket.close();
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String jsonInput = in.readLine();
            plugin.getLogger().info("Received input: " + jsonInput);

            CommandRequest request = gson.fromJson(jsonInput, CommandRequest.class);

            if (!request.password.equals(plugin.getConfig().getString("password"))) {
                handleFailedAuth(clientIP);
                String response = gson.toJson(new CommandResponse(false, "Invalid password"));
                plugin.getLogger().info("Sending response: " + response);
                out.println(response);
                return;
            }

            // Reset failed attempts on successful auth
            failedAttempts.remove(clientIP);

            // Execute command on the main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                boolean success = Bukkit.getServer().dispatchCommand(
                    Bukkit.getConsoleSender(), 
                    request.command
                );
                String response = gson.toJson(new CommandResponse(success, "Command executed"));
                plugin.getLogger().info("Sending response: " + response);
                out.println(response);
            });

        } catch (Exception e) {
            plugin.getLogger().warning("Error handling client: " + e.getMessage());
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(gson.toJson(new CommandResponse(false, "Server error: " + e.getMessage())));
            } catch (Exception ex) {
                plugin.getLogger().severe("Could not send error response: " + ex.getMessage());
            }
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                plugin.getLogger().warning("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private boolean isIPBanned(String ip) {
        Long banTime = ipBans.get(ip);
        if (banTime == null) return false;

        if (System.currentTimeMillis() - banTime > plugin.getConfig().getInt("ban_duration") * 60 * 1000) {
            ipBans.remove(ip);
            return false;
        }
        return true;
    }

    private void handleFailedAuth(String ip) {
        int attempts = failedAttempts.getOrDefault(ip, 0) + 1;
        failedAttempts.put(ip, attempts);

        if (attempts >= plugin.getConfig().getInt("max_failed_attempts")) {
            ipBans.put(ip, System.currentTimeMillis());
            failedAttempts.remove(ip);
        }
    }

    private static class CommandRequest {
        String password;
        String command;
    }

    private static class CommandResponse {
        boolean success;
        String message;

        CommandResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
} 