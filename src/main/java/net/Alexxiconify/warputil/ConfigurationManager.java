package net.Alexxiconify.warputil;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigurationManager {
    private final NestedWarpsPlugin plugin;
    private FileConfiguration config;

    public ConfigurationManager(NestedWarpsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public boolean isDebug() {
        return config.getBoolean("general.debug", false);
    }

    public String getLanguage() {
        return config.getString("general.language", "en");
    }

    public boolean isAutoSave() {
        return config.getBoolean("general.auto-save", true);
    }

    public int getTeleportDelay() {
        return config.getInt("teleport.delay", 3);
    }

    public int getCooldown() {
        return config.getInt("teleport.cooldown", 5);
    }

    public boolean isCancelOnDamage() {
        return config.getBoolean("teleport.cancel-on-damage", true);
    }

    public boolean isCancelOnMovement() {
        return config.getBoolean("teleport.cancel-on-movement", true);
    }

    public double getMovementThreshold() {
        return config.getDouble("teleport.movement-threshold", 0.5);
    }

    public boolean isAllowCrossWorld() {
        return config.getBoolean("teleport.allow-cross-world", true);
    }

    public boolean isRequireCrossWorldPermission() {
        return config.getBoolean("teleport.require-cross-world-permission", true);
    }

    public String getCrossWorldPermission() {
        return config.getString("teleport.cross-world-permission", "nestedwarps.crossworld");
    }

    public int getMaxWarps() {
        return config.getInt("warps.max-warps", 50);
    }

    public boolean isPublicByDefault() {
        return config.getBoolean("warps.public-by-default", true);
    }

    public boolean isAllowNestedWarps() {
        return config.getBoolean("warps.allow-nested", true);
    }

    public int getMaxWarpDepth() {
        return config.getInt("warps.max-depth", 5);
    }

    public boolean isRequireSpecificPermissions() {
        return config.getBoolean("warps.require-specific-permissions", false);
    }

    public boolean isShowWarpCount() {
        return config.getBoolean("warps.show-count", true);
    }

    public int getMaxHomes() {
        return config.getInt("homes.max-homes", 10);
    }

    public boolean isAllowNestedHomes() {
        return config.getBoolean("homes.allow-nested", true);
    }

    public int getMaxHomeDepth() {
        return config.getInt("homes.max-depth", 3);
    }

    public boolean isAllowCrossWorldHomes() {
        return config.getBoolean("homes.allow-cross-world", true);
    }

    public boolean isRequireCrossWorldHomePermission() {
        return config.getBoolean("homes.require-cross-world-permission", true);
    }

    public String getCrossWorldHomePermission() {
        return config.getString("homes.cross-world-permission", "nestedhomes.crossworld");
    }

    public boolean isEconomyEnabled() {
        return config.getBoolean("economy.enabled", false);
    }

    public double getWarpCost() {
        return config.getDouble("economy.warp-cost", 100.0);
    }

    public double getHomeCost() {
        return config.getDouble("economy.home-cost", 50.0);
    }

    public double getWarpTeleportCost() {
        return config.getDouble("economy.warp-teleport-cost", 10.0);
    }

    public double getHomeTeleportCost() {
        return config.getDouble("economy.home-teleport-cost", 5.0);
    }

    public boolean isRefundOnDelete() {
        return config.getBoolean("economy.refund-on-delete", true);
    }

    public double getRefundPercentage() {
        return config.getDouble("economy.refund-percentage", 0.5);
    }

    public boolean isEffectsEnabled() {
        return config.getBoolean("effects.enabled", true);
    }

    public String getStartEffect() {
        return config.getString("effects.start-effect", "SMOKE");
    }

    public String getEndEffect() {
        return config.getString("effects.end-effect", "PORTAL");
    }

    public String getStartSound() {
        return config.getString("effects.start-sound", "ENTITY_ENDERMAN_TELEPORT");
    }

    public String getEndSound() {
        return config.getString("effects.end-sound", "ENTITY_ENDERMAN_TELEPORT");
    }

    public double getSoundVolume() {
        return config.getDouble("effects.sound-volume", 0.5);
    }

    public double getSoundPitch() {
        return config.getDouble("effects.sound-pitch", 1.0);
    }

    public String getMessagePrefix() {
        return config.getString("messages.prefix", "&8[&bWarpUtil&8] ");
    }

    public boolean isUseActionBar() {
        return config.getBoolean("messages.use-action-bar", true);
    }

    public boolean isUseTitles() {
        return config.getBoolean("messages.use-titles", false);
    }

    public int getTitleDuration() {
        return config.getInt("messages.title-duration", 60);
    }

    public boolean isCheckSafeLocation() {
        return config.getBoolean("safety.check-safe-location", true);
    }

    public int getMaxFallDistance() {
        return config.getInt("safety.max-fall-distance", 10);
    }

    public boolean isPreventBlockTeleport() {
        return config.getBoolean("safety.prevent-block-teleport", true);
    }

    public boolean isPreventLavaTeleport() {
        return config.getBoolean("safety.prevent-lava-teleport", true);
    }

    public boolean isPreventWaterTeleport() {
        return config.getBoolean("safety.prevent-water-teleport", false);
    }

    public boolean isPreventVoidTeleport() {
        return config.getBoolean("safety.prevent-void-teleport", true);
    }

    public boolean isWorldGuardEnabled() {
        return config.getBoolean("integrations.worldguard.enabled", false);
    }

    public boolean isWorldGuardCheckRegions() {
        return config.getBoolean("integrations.worldguard.check-regions", true);
    }

    public boolean isWorldGuardAllowInProtected() {
        return config.getBoolean("integrations.worldguard.allow-in-protected", false);
    }

    public boolean isWorldEditEnabled() {
        return config.getBoolean("integrations.worldedit.enabled", false);
    }

    public boolean isWorldEditAllowSelection() {
        return config.getBoolean("integrations.worldedit.allow-selection", true);
    }

    public boolean isPlaceholderAPIEnabled() {
        return config.getBoolean("integrations.placeholderapi.enabled", false);
    }

    public ConfigurationSection getSection(String path) {
        return config.getConfigurationSection(path);
    }

    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }
} 