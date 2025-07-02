package net.Alexxiconify.warputil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private final NestedWarpsPlugin plugin;
    private final Map<String, String> messages = new HashMap<>();
    private String prefix;

    public MessageManager(NestedWarpsPlugin plugin) {
        this.plugin = plugin;
        reloadMessages();
    }

    public void reloadMessages() {
        messages.clear();
        prefix = plugin.getConfigManager().getMessagePrefix();
        loadDefaultMessages();
        loadCustomMessages();
    }

    private void loadDefaultMessages() {
        messages.put("no-permission", "&cYou do not have permission to use this command.");
        messages.put("player-only", "&cThis command can only be used by players.");
        messages.put("config-reloaded", "&aConfiguration reloaded successfully!");
        messages.put("plugin-info", "&bWarpUtil v%version% - &7%warps% warps, %players% players online");
        messages.put("admin-usage", "&cUsage: /warputil <reload|info>");
        messages.put("teleport-starting", "&eTeleporting in &6%time% &eseconds...");
        messages.put("teleport-success", "&aSuccessfully teleported to %type%: &6%name%");
        messages.put("teleport-cancelled-movement", "&cTeleport cancelled - you moved too much!");
        messages.put("teleport-cancelled-damage", "&cTeleport cancelled - you took damage!");
        messages.put("cooldown", "&cYou must wait &6%time% &cseconds before teleporting again.");
        messages.put("unsafe-location", "&cThis location is unsafe for teleportation.");
        messages.put("cross-world-denied", "&cYou do not have permission to teleport across worlds.");
        messages.put("warp-usage", "&eUsage: &6/warp <name>");
        messages.put("warp-not-found", "&cWarp &6%name% &cnot found.");
        messages.put("warp-set", "&aWarp &6%name% &aset successfully!");
        messages.put("warp-deleted", "&aWarp &6%name% &adeleted successfully!");
        messages.put("warp-limit-reached", "&cYou have reached the maximum number of warps.");
        messages.put("warps-header", "&b--- Available Warps ---");
        messages.put("warp-list-item", "&7- &6%name%");
        messages.put("warps-footer", "&b---------------------");
        messages.put("no-warps", "&eNo warps have been set yet.");
        messages.put("home-usage", "&eUsage: &6/home <name>");
        messages.put("home-not-found", "&cHome &6%name% &cnot found or you do not have access to it.");
        messages.put("home-set", "&aHome &6%name% &aset successfully!");
        messages.put("home-deleted", "&aHome &6%name% &adeleted successfully!");
        messages.put("home-limit-reached", "&cYou have reached the maximum number of homes.");
        messages.put("homes-header", "&b--- Your Homes ---");
        messages.put("home-list-item", "&7- &6%name%");
        messages.put("homes-footer", "&b---------------------");
        messages.put("no-homes", "&eYou have not set or been shared any homes yet.");
        messages.put("setwarp-usage", "&eUsage: &6/setwarp <name>");
        messages.put("delwarp-usage", "&eUsage: &6/delwarp <name>");
        messages.put("sethome-usage", "&eUsage: &6/sethome <name>");
        messages.put("delhome-usage", "&eUsage: &6/delhome <name>");
        messages.put("sharehome-usage", "&eUsage: &6/sharehome <add|remove> <player> <home>");
        messages.put("player-not-found", "&cPlayer &6%name% &cnot found.");
        messages.put("home-shared", "&aSuccessfully shared your home &6%name% &awith &6%player%&a.");
        messages.put("home-already-shared", "&eYour home &6%name% &eis already shared with &6%player%&e.");
        messages.put("home-unshared", "&aSuccessfully unshared your home &6%name% &afrom &6%player%&a.");
        messages.put("home-not-shared", "&eYour home &6%name% &ewas not shared with &6%player%&e.");
        messages.put("insufficient-funds", "&cYou do not have enough money for this action.");
        messages.put("economy-error", "&cAn error occurred with the economy system.");
        messages.put("error-occurred", "&cAn error occurred. Please contact an administrator.");
        messages.put("invalid-arguments", "&cInvalid arguments provided.");
    }

    private void loadCustomMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        try {
            YamlConfiguration customMessages = YamlConfiguration.loadConfiguration(messagesFile);
            ConfigurationSection messagesSection = customMessages.getConfigurationSection("messages");
            
            if (messagesSection != null) {
                for (String key : messagesSection.getKeys(true)) {
                    String value = messagesSection.getString(key);
                    if (value != null) {
                        messages.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load custom messages: " + e.getMessage());
        }
    }

    public void sendMessage(CommandSender sender, String messageKey) {
        sendMessage(sender, messageKey, new HashMap<>());
    }

    public void sendMessage(CommandSender sender, String messageKey, Map<String, String> placeholders) {
        String message = messages.getOrDefault(messageKey, "&cMessage not found: " + messageKey);
        message = replacePlaceholders(message, placeholders);
        message = prefix + message;
        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }

    private String replacePlaceholders(String message, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "&cMessage not found: " + key);
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        return replacePlaceholders(message, placeholders);
    }

    public void broadcastMessage(String messageKey) {
        broadcastMessage(messageKey, new HashMap<>());
    }

    public void broadcastMessage(String messageKey, Map<String, String> placeholders) {
        String message = messages.getOrDefault(messageKey, "&cMessage not found: " + messageKey);
        message = replacePlaceholders(message, placeholders);
        message = prefix + message;
        Component finalMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        plugin.getServer().broadcast(finalMessage);
    }

    public void logMessage(String messageKey) {
        logMessage(messageKey, new HashMap<>());
    }

    public void logMessage(String messageKey, Map<String, String> placeholders) {
        String message = messages.getOrDefault(messageKey, "Message not found: " + messageKey);
        message = replacePlaceholders(message, placeholders);
        plugin.getLogger().info(message);
    }
} 