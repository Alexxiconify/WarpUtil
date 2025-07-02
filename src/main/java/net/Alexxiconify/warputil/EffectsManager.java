package net.Alexxiconify.warputil;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class EffectsManager {
    private final NestedWarpsPlugin plugin;

    public EffectsManager(NestedWarpsPlugin plugin) {
        this.plugin = plugin;
    }

    public void playStartEffect(Player player) {
        if (!plugin.getConfigManager().isEffectsEnabled()) {
            return;
        }

        Location location = player.getLocation();
        
        // Play particle effect
        playParticleEffect(location, plugin.getConfigManager().getStartEffect());
        
        // Play sound effect
        playSoundEffect(location, plugin.getConfigManager().getStartSound());
    }

    public void playEndEffect(Player player) {
        if (!plugin.getConfigManager().isEffectsEnabled()) {
            return;
        }

        Location location = player.getLocation();
        
        // Play particle effect
        playParticleEffect(location, plugin.getConfigManager().getEndEffect());
        
        // Play sound effect
        playSoundEffect(location, plugin.getConfigManager().getEndSound());
    }

    private void playParticleEffect(Location location, String effectName) {
        if (location.getWorld() == null) return;
        
        try {
            Particle particle = Particle.valueOf(effectName.toUpperCase());
            location.getWorld().spawnParticle(particle, location, 10);
        } catch (IllegalArgumentException e) {
            // Fallback to default effect
            location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 10);
        }
    }

    private void playSoundEffect(Location location, String soundName) {
        if (location.getWorld() == null) return;
        
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            float volume = (float) plugin.getConfigManager().getSoundVolume();
            float pitch = (float) plugin.getConfigManager().getSoundPitch();
            location.getWorld().playSound(location, sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            // Fallback to default sound
            float volume = (float) plugin.getConfigManager().getSoundVolume();
            float pitch = (float) plugin.getConfigManager().getSoundPitch();
            location.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, volume, pitch);
        }
    }

    public void playTeleportEffect(Location location) {
        if (!plugin.getConfigManager().isEffectsEnabled() || location.getWorld() == null) {
            return;
        }

        // Play portal effect
        location.getWorld().spawnParticle(Particle.PORTAL, location, 20);
        
        // Play enderman teleport sound
        float volume = (float) plugin.getConfigManager().getSoundVolume();
        float pitch = (float) plugin.getConfigManager().getSoundPitch();
        location.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, volume, pitch);
    }

    public void playErrorEffect(Player player) {
        if (!plugin.getConfigManager().isEffectsEnabled()) {
            return;
        }

        Location location = player.getLocation();
        if (location.getWorld() == null) return;
        
        // Play smoke effect
        location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 10);
        
        // Play error sound
        float volume = (float) plugin.getConfigManager().getSoundVolume();
        float pitch = (float) plugin.getConfigManager().getSoundPitch();
        location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_BASS, volume, pitch * 0.5f);
    }

    public void playSuccessEffect(Player player) {
        if (!plugin.getConfigManager().isEffectsEnabled()) {
            return;
        }

        Location location = player.getLocation();
        if (location.getWorld() == null) return;
        
        // Play happy villager effect
        location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 10);
        
        // Play success sound
        float volume = (float) plugin.getConfigManager().getSoundVolume();
        float pitch = (float) plugin.getConfigManager().getSoundPitch();
        location.getWorld().playSound(location, Sound.ENTITY_PLAYER_LEVELUP, volume, pitch);
    }

    public void playCooldownEffect(Player player) {
        if (!plugin.getConfigManager().isEffectsEnabled()) {
            return;
        }

        Location location = player.getLocation();
        if (location.getWorld() == null) return;
        
        // Play redstone effect
        location.getWorld().spawnParticle(Particle.REDSTONE, location, 10);
        
        // Play cooldown sound
        float volume = (float) plugin.getConfigManager().getSoundVolume();
        float pitch = (float) plugin.getConfigManager().getSoundPitch();
        location.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, volume, pitch * 0.8f);
    }

    public void playEconomyEffect(Player player) {
        if (!plugin.getConfigManager().isEffectsEnabled()) {
            return;
        }

        Location location = player.getLocation();
        if (location.getWorld() == null) return;
        
        // Play emerald effect
        location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 10);
        
        // Play money sound
        float volume = (float) plugin.getConfigManager().getSoundVolume();
        float pitch = (float) plugin.getConfigManager().getSoundPitch();
        location.getWorld().playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, pitch);
    }
} 