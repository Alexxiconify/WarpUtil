package net.Alexxiconify.warputil;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SafetyManager {
    private final NestedWarpsPlugin plugin;

    public SafetyManager(NestedWarpsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isSafeLocation(Location location) {
        if (!plugin.getConfigManager().isCheckSafeLocation()) return true;
        World world = location.getWorld();
        if (world == null) return false;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        if (plugin.getConfigManager().isPreventVoidTeleport() && y < 0) return false;

        Block teleportBlock = world.getBlockAt(x, y, z);
        Block blockAbove = world.getBlockAt(x, y + 1, z);

        if (plugin.getConfigManager().isPreventBlockTeleport() && teleportBlock.getType().isSolid()) return false;
        if (plugin.getConfigManager().isPreventLavaTeleport() && 
            (teleportBlock.getType() == Material.LAVA || blockAbove.getType() == Material.LAVA)) return false;
        if (!plugin.getConfigManager().isPreventWaterTeleport() && 
            (teleportBlock.getType() == Material.WATER || blockAbove.getType() == Material.WATER)) return false;

        return isSafeLandingSpot(world, x, y, z);
    }

    private boolean isSafeLandingSpot(World world, int x, int y, int z) {
        Block blockBelow = world.getBlockAt(x, y - 1, z);
        if (!blockBelow.getType().isSolid()) return false;

        Block blockAt = world.getBlockAt(x, y, z);
        Block blockAbove = world.getBlockAt(x, y + 1, z);
        
        if (blockAt.getType().isSolid() || blockAbove.getType().isSolid()) return false;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block nearbyBlock = world.getBlockAt(x + dx, y, z + dz);
                if (plugin.getConfigManager().isPreventLavaTeleport() && nearbyBlock.getType() == Material.LAVA) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean canTeleportCrossWorld(Player player, World fromWorld, World toWorld, String type) {
        if (fromWorld.equals(toWorld)) return true;
        if (!plugin.getConfigManager().isAllowCrossWorld()) return false;
        if (!plugin.getConfigManager().isRequireCrossWorldPermission()) return true;

        String permission = getCrossWorldPermission(type);
        return player.hasPermission(permission);
    }

    private String getCrossWorldPermission(String type) {
        switch (type.toLowerCase()) {
            case "warp":
                return plugin.getConfigManager().getCrossWorldPermission();
            case "home":
                return plugin.getConfigManager().getCrossWorldHomePermission();
            default:
                return plugin.getConfigManager().getCrossWorldPermission();
        }
    }

    public Location findSafeLocation(Location location) {
        if (isSafeLocation(location)) return location;
        World world = location.getWorld();
        if (world == null) return null;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        for (int radius = 1; radius <= 5; radius++) {
            for (int dy = -3; dy <= 3; dy++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        Location testLocation = new Location(world, x + dx, y + dy, z + dz);
                        if (isSafeLocation(testLocation)) return testLocation;
                    }
                }
            }
        }
        return null;
    }

    public boolean isInProtectedRegion(Location location) {
        if (!plugin.getConfigManager().isWorldGuardEnabled()) return false;
        return false; // Simplified implementation
    }

    public boolean canTeleportInRegion(Player player, Location location) {
        if (!plugin.getConfigManager().isWorldGuardEnabled()) return true;
        if (!plugin.getConfigManager().isWorldGuardCheckRegions()) return true;
        if (plugin.getConfigManager().isWorldGuardAllowInProtected()) return true;
        if (player.hasPermission("nestedwarps.bypass.regions")) return true;
        return !isInProtectedRegion(location);
    }

    public boolean isLocationInWorldEditSelection(Player player, Location location) {
        if (!plugin.getConfigManager().isWorldEditEnabled()) return false;
        if (!plugin.getConfigManager().isWorldEditAllowSelection()) return false;
        return false; // Simplified implementation
    }
} 