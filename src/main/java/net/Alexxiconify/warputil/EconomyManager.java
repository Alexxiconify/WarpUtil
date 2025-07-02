package net.Alexxiconify.warputil;

import org.bukkit.entity.Player;

public class EconomyManager {
    private final NestedWarpsPlugin plugin;
    private boolean enabled = false;

    public EconomyManager(NestedWarpsPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private void setupEconomy() {
        if (!plugin.getConfigManager().isEconomyEnabled()) {
            enabled = false;
            return;
        }
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Economy is enabled but Vault is not installed!");
            enabled = false;
            return;
        }
        try {
            enabled = true;
            plugin.getLogger().info("Economy integration enabled!");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into economy: " + e.getMessage());
            enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean canAffordTeleport(Player player, String type) {
        if (!enabled) return true;
        double cost = getTeleportCost(type);
        return getBalance(player) >= cost;
    }

    public boolean canAffordWarp(Player player) {
        if (!enabled) return true;
        double cost = plugin.getConfigManager().getWarpCost();
        return getBalance(player) >= cost;
    }

    public boolean canAffordHome(Player player) {
        if (!enabled) return true;
        double cost = plugin.getConfigManager().getHomeCost();
        return getBalance(player) >= cost;
    }

    public boolean chargeTeleport(Player player, String type) {
        if (!enabled) return true;
        double cost = getTeleportCost(type);
        return withdraw(player, cost);
    }

    public boolean chargeWarp(Player player) {
        if (!enabled) return true;
        double cost = plugin.getConfigManager().getWarpCost();
        return withdraw(player, cost);
    }

    public boolean chargeHome(Player player) {
        if (!enabled) return true;
        double cost = plugin.getConfigManager().getHomeCost();
        return withdraw(player, cost);
    }

    public void refundWarp(Player player) {
        if (!enabled) return;
        double cost = plugin.getConfigManager().getWarpCost();
        double refund = cost * plugin.getConfigManager().getRefundPercentage();
        deposit(player, refund);
    }

    public void refundHome(Player player) {
        if (!enabled) return;
        double cost = plugin.getConfigManager().getHomeCost();
        double refund = cost * plugin.getConfigManager().getRefundPercentage();
        deposit(player, refund);
    }

    private double getTeleportCost(String type) {
        switch (type.toLowerCase()) {
            case "warp":
                return plugin.getConfigManager().getWarpTeleportCost();
            case "home":
                return plugin.getConfigManager().getHomeTeleportCost();
            default:
                return 0.0;
        }
    }

    private double getBalance(Player player) {
        if (!enabled) return Double.MAX_VALUE;
        try {
            return 1000.0; // Placeholder
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get balance for " + player.getName() + ": " + e.getMessage());
            return 0.0;
        }
    }

    private boolean withdraw(Player player, double amount) {
        if (!enabled) return true;
        try {
            return true; // Placeholder
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to withdraw " + amount + " from " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }

    private boolean deposit(Player player, double amount) {
        if (!enabled) return true;
        try {
            return true; // Placeholder
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deposit " + amount + " to " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }

    public String formatMoney(double amount) {
        if (!enabled) return "0";
        try {
            return String.format("%.2f", amount); // Placeholder
        } catch (Exception e) {
            return String.valueOf(amount);
        }
    }
} 