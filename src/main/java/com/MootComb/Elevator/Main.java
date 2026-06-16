package com.MootComb.Elevator;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Main extends JavaPlugin implements Listener {

    private static Main instance;

    public enum MessageType {
        CHAT, TITLE, SUBTITLE
    }

    private static final int DOUBLE_CLICK_DELAY_TICKS = 5;
    private static final int SNEAK_CHECK_DELAY_TICKS = 1;
    private static final int DEFAULT_END_ROD_COUNT = 5;

    private Set<Material> elevatorBlocks = new HashSet<>();
    private int blockDistance;
    private boolean enableParticle;
    private Particle particleType;
    private int particleCount;
    private String usageSound;
    private String activateSound;
    private boolean allowUnsafe;
    private Set<String> disabledWorlds = new HashSet<>();

    private Set<Material> teleporterBlocks = new HashSet<>();
    private boolean teleporterEnableParticle;
    private String teleporterUsageSound;
    private int warmupTime;
    private boolean teleporterAllowUnsafe;
    private boolean allowCrossWorlds;

    private boolean enableCooldown;
    private int cooldownTime;
    private String cooldownLocale;
    private MessageType cooldownMessageType;

    private MessageType elevatorMessageType;
    private int elevatorTitleFadeIn;
    private int elevatorTitleStay;
    private int elevatorTitleFadeOut;
    private String elevatorUpMessage;
    private String elevatorDownMessage;
    private String elevatorDangerMessage;

    private MessageType teleporterMessageType;
    private int teleporterTitleFadeIn;
    private int teleporterTitleStay;
    private int teleporterTitleFadeOut;
    private String teleporterWaitingMessage;
    private String teleporterMatchedMessage;
    private String teleporterSwappedMessage;
    private String teleporterTimeoutMessage;
    private String teleporterCancelledMessage;
    private String teleporterSamePlayerMessage;
    private String teleporterNoNearbyMessage;

    private boolean checkPermission;
    private String permUse;
    private String permBypass;
    private String permTeleport;

    private final Map<UUID, Long> cooldownMap = new HashMap<>();
    private final Map<String, Map<Material, TeleporterQueue>> teleporterQueues = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> timeoutTasks = new HashMap<>();
    private final Set<UUID> recentTeleporterClicks = ConcurrentHashMap.newKeySet();

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private static class TeleporterQueue {
        UUID playerId;
        long joinTime;
        Material teleporterType;

        TeleporterQueue(UUID playerId, long joinTime, Material teleporterType) {
            this.playerId = playerId;
            this.joinTime = joinTime;
            this.teleporterType = teleporterType;
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);

        if (getCommand("elevator") != null) {
            getCommand("elevator").setExecutor((sender, cmd, label, args) -> {
                if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("elevator.reload")) {
                        sender.sendMessage(translateHexColors("&#FF5555No permission!"));
                        return true;
                    }
                    loadConfig();
                    sender.sendMessage(translateHexColors("&#55FF55Elevator config reloaded!"));
                    return true;
                }
                return false;
            });
        }

        getLogger().info("==================================================");
        getLogger().info("   Elevator Plugin Enabled!");
        getLogger().info("   Elevator blocks: " + elevatorBlocks.size());
        getLogger().info("   Teleporter blocks: " + teleporterBlocks.size());
        getLogger().info("   Max distance: " + blockDistance);
        getLogger().info("   Jump = UP | Sneak = DOWN | Right-click teleporter = SWAP");
        getLogger().info("==================================================");
    }

    @Override
    public void onDisable() {
        timeoutTasks.values().forEach(BukkitTask::cancel);
        timeoutTasks.clear();
        teleporterQueues.clear();
        recentTeleporterClicks.clear();
        getLogger().info("Elevator Disabled!");
    }

    // Converts hex color codes like &#RRGGBB to Minecraft colors
    private String translateHexColors(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    // Loads all configuration values from config.yml
    private void loadConfig() {
        saveDefaultConfig();
        reloadConfig();

        elevatorBlocks.clear();
        for (String blockName : getConfig().getStringList("Elevator.BlockTypes")) {
            Material mat = Material.getMaterial(blockName);
            if (mat != null) {
                elevatorBlocks.add(mat);
            } else {
                getLogger().warning("Unknown elevator block: " + blockName);
            }
        }

        blockDistance = getConfig().getInt("Elevator.BlockDistance", 50);
        enableParticle = getConfig().getBoolean("Elevator.EnableParticle", true);
        particleCount = getConfig().getInt("Elevator.ParticleCount", 20);
        usageSound = getConfig().getString("Elevator.UsageSound", "entity.enderman.teleport");
        activateSound = getConfig().getString("Elevator.ActivateSound", "entity.player.levelup");
        allowUnsafe = getConfig().getBoolean("Elevator.AllowUnsafe", false);

        String particleName = getConfig().getString("Elevator.ParticleType", "SPELL_WITCH");
        try {
            particleType = Particle.valueOf(particleName);
        } catch (IllegalArgumentException e) {
            particleType = Particle.SPELL_WITCH;
        }

        teleporterBlocks.clear();
        List<String> teleporterBlockNames = getConfig().getStringList("Teleporter.BlockTypes");
        if (teleporterBlockNames.isEmpty()) {
            teleporterBlockNames.add("SEA_LANTERN");
        }
        for (String blockName : teleporterBlockNames) {
            Material mat = Material.getMaterial(blockName);
            if (mat != null) {
                teleporterBlocks.add(mat);
            } else {
                getLogger().warning("Unknown teleporter block: " + blockName);
            }
        }

        teleporterEnableParticle = getConfig().getBoolean("Teleporter.EnableParticle", true);
        teleporterUsageSound = getConfig().getString("Teleporter.UsageSound", "entity.enderman.teleport");
        warmupTime = getConfig().getInt("Teleporter.WarmupTime", 5);
        teleporterAllowUnsafe = getConfig().getBoolean("Teleporter.AllowUnsafe", true);
        allowCrossWorlds = getConfig().getBoolean("Teleporter.AllowCrossWorlds", true);

        enableCooldown = getConfig().getBoolean("Cooldown.EnableCooldown", false);
        cooldownTime = getConfig().getInt("Cooldown.Time", 30);
        cooldownLocale = getConfig().getString("Cooldown.Locale", "&#FF5555Elevator is on cooldown. Please wait for another %time% seconds!");
        cooldownMessageType = getMessageType(getConfig().getString("Cooldown.MessageType", "CHAT"));

        elevatorMessageType = getMessageType(getConfig().getString("ElevatorLocale.MessageType", "CHAT"));

        ConfigurationSection elevatorTitleSection = getConfig().getConfigurationSection("ElevatorLocale.Title");
        if (elevatorTitleSection != null) {
            elevatorTitleFadeIn = elevatorTitleSection.getInt("FadeIn", 10);
            elevatorTitleStay = elevatorTitleSection.getInt("Stay", 40);
            elevatorTitleFadeOut = elevatorTitleSection.getInt("FadeOut", 10);
        } else {
            elevatorTitleFadeIn = 10;
            elevatorTitleStay = 40;
            elevatorTitleFadeOut = 10;
        }

        elevatorUpMessage = getConfig().getString("ElevatorLocale.ElevatorUp", "&#55FF55⬆ Going up");
        elevatorDownMessage = getConfig().getString("ElevatorLocale.ElevatorDown", "&#FFAA00⬇ Going down");
        elevatorDangerMessage = getConfig().getString("ElevatorLocale.ElevatorDanger", "&#FF5555⚠ Danger! Unsafe location!");

        teleporterMessageType = getMessageType(getConfig().getString("TeleporterLocale.MessageType", "TITLE"));

        ConfigurationSection teleporterTitleSection = getConfig().getConfigurationSection("TeleporterLocale.Title");
        if (teleporterTitleSection != null) {
            teleporterTitleFadeIn = teleporterTitleSection.getInt("FadeIn", 10);
            teleporterTitleStay = teleporterTitleSection.getInt("Stay", 60);
            teleporterTitleFadeOut = teleporterTitleSection.getInt("FadeOut", 10);
        } else {
            teleporterTitleFadeIn = 10;
            teleporterTitleStay = 60;
            teleporterTitleFadeOut = 10;
        }

        teleporterWaitingMessage = getConfig().getString("TeleporterLocale.TeleporterWaiting", "&#FFFF55⏳ Waiting for another player... (%time%s) ⏳");
        teleporterMatchedMessage = getConfig().getString("TeleporterLocale.TeleporterMatched", "&#55FF55✓ Player found! Swapping places...");
        teleporterSwappedMessage = getConfig().getString("TeleporterLocale.TeleporterSwapped", "&#55FFFF✨ Swapped places! ✨");
        teleporterTimeoutMessage = getConfig().getString("TeleporterLocale.TeleporterTimeout", "&#FF5555❌ Teleport request timed out!");
        teleporterCancelledMessage = getConfig().getString("TeleporterLocale.TeleporterCancelled", "&#FF5555❌ Teleport cancelled!");
        teleporterSamePlayerMessage = getConfig().getString("TeleporterLocale.TeleporterSamePlayer", "&#FF5555❌ You cannot swap with yourself!");
        teleporterNoNearbyMessage = getConfig().getString("TeleporterLocale.TeleporterNoNearby", "&#FF5555❌ No nearby teleporter found!");

        disabledWorlds.clear();
        disabledWorlds.addAll(getConfig().getStringList("DisabledWorlds"));

        checkPermission = getConfig().getBoolean("Permissions.CheckPermission", false);
        permUse = getConfig().getString("Permissions.Use", "elevator.use");
        permBypass = getConfig().getString("Permissions.BypassCooldown", "elevator.bypass");
        permTeleport = getConfig().getString("Permissions.Teleport", "elevator.teleport");
    }

    // Converts string from config to MessageType enum
    private MessageType getMessageType(String type) {
        try {
            return MessageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MessageType.CHAT;
        }
    }

    // Sends a message to player in specified format (CHAT/TITLE/SUBTITLE)
    private void sendMessage(Player player, String message, MessageType type) {
        sendMessage(player, message, type, null);
    }

    private void sendMessage(Player player, String message, MessageType type, String secondaryMessage) {
        String formatted = translateHexColors(message);

        switch (type) {
            case CHAT:
                player.sendMessage(formatted);
                break;
            case TITLE:
                if (secondaryMessage != null) {
                    String subtitle = translateHexColors(secondaryMessage);
                    player.sendTitle(formatted, subtitle, elevatorTitleFadeIn, elevatorTitleStay, elevatorTitleFadeOut);
                } else {
                    player.sendTitle(formatted, "", elevatorTitleFadeIn, elevatorTitleStay, elevatorTitleFadeOut);
                }
                break;
            case SUBTITLE:
                player.sendTitle("", formatted, elevatorTitleFadeIn, elevatorTitleStay, elevatorTitleFadeOut);
                break;
        }
    }

    // Sends teleporter-specific messages with its own title settings
    private void sendTeleporterMessage(Player player, String message, MessageType type) {
        sendTeleporterMessage(player, message, type, null);
    }

    private void sendTeleporterMessage(Player player, String message, MessageType type, String secondaryMessage) {
        String formatted = translateHexColors(message);

        switch (type) {
            case CHAT:
                player.sendMessage(formatted);
                break;
            case TITLE:
                if (secondaryMessage != null) {
                    String subtitle = translateHexColors(secondaryMessage);
                    player.sendTitle(formatted, subtitle, teleporterTitleFadeIn, teleporterTitleStay, teleporterTitleFadeOut);
                } else {
                    player.sendTitle(formatted, "", teleporterTitleFadeIn, teleporterTitleStay, teleporterTitleFadeOut);
                }
                break;
            case SUBTITLE:
                player.sendTitle("", formatted, teleporterTitleFadeIn, teleporterTitleStay, teleporterTitleFadeOut);
                break;
        }
    }

    // Checks if player has required permission, respects global toggle
    private boolean hasPermission(Player player, String perm) {
        if (!checkPermission) return true;
        return player.hasPermission(perm);
    }

    // Checks if player is in a disabled world
    private boolean isInDisabledWorld(Player player) {
        return disabledWorlds.contains(player.getWorld().getName());
    }

    // Checks and handles cooldown, returns true if on cooldown
    private boolean isOnCooldown(Player player) {
        if (!enableCooldown) return false;
        if (hasPermission(player, permBypass)) return false;

        Long lastUse = cooldownMap.get(player.getUniqueId());
        if (lastUse == null) return false;

        long timeLeft = (lastUse + cooldownTime * 1000L) - System.currentTimeMillis();
        if (timeLeft > 0) {
            String msg = cooldownLocale.replace("%time%", String.valueOf(timeLeft / 1000 + 1));
            sendMessage(player, msg, cooldownMessageType);
            return true;
        }
        cooldownMap.remove(player.getUniqueId());
        return false;
    }

    // Sets cooldown for player
    private void setCooldown(Player player) {
        if (enableCooldown && !hasPermission(player, permBypass)) {
            cooldownMap.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    // Checks if destination location is safe (no solid blocks, lava, fire)
    private boolean isSafeLocation(Location loc, boolean isTeleporter) {
        boolean unsafeAllowed = isTeleporter ? teleporterAllowUnsafe : allowUnsafe;
        if (unsafeAllowed) return true;
        Material type = loc.getBlock().getType();
        return !type.isSolid() && type != Material.LAVA && type != Material.FIRE;
    }

    // Spawns particle effects for elevator
    private void spawnElevatorParticles(Location loc) {
        if (!enableParticle) return;
        loc.getWorld().spawnParticle(particleType, loc.clone().add(0, 0.5, 0),
                particleCount, 0.3, 0.1, 0.3, 0.1);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0, 0.5, 0),
                DEFAULT_END_ROD_COUNT, 0.2, 0.2, 0.2, 0.05);
    }

    // Spawns particle effects for teleporter
    private void spawnTeleporterParticles(Location loc) {
        if (!teleporterEnableParticle) return;
        loc.getWorld().spawnParticle(particleType, loc.clone().add(0, 0.5, 0),
                particleCount, 0.3, 0.1, 0.3, 0.1);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0, 0.5, 0),
                DEFAULT_END_ROD_COUNT, 0.2, 0.2, 0.2, 0.05);
    }

    // Plays sound and particle effects at location
    private void playEffects(Location loc, String soundName, boolean isTeleporter) {
        if (soundName != null && !soundName.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase());
                loc.getWorld().playSound(loc, sound, 0.5f, 1.2f);
            } catch (IllegalArgumentException ignored) {}
        }

        if (isTeleporter) {
            spawnTeleporterParticles(loc);
        } else {
            spawnElevatorParticles(loc);
        }
    }

    // Creates unique key for teleporter queue based on world and block type
    private String getQueueKey(Player player, Material teleporterType) {
        return player.getWorld().getName() + ":" + teleporterType.name();
    }

    // Main teleporter logic: handles queue matching, waiting, and swapping
    private void handleTeleporterClick(Player player, Location blockLocation, Material teleporterType) {
        if (isInDisabledWorld(player)) return;
        if (!hasPermission(player, permTeleport)) return;
        if (isOnCooldown(player)) return;

        String queueKey = getQueueKey(player, teleporterType);
        Map<Material, TeleporterQueue> worldQueues = teleporterQueues.computeIfAbsent(queueKey, k -> new ConcurrentHashMap<>());

        // Cancel existing request if player clicks same teleporter again
        if (worldQueues.containsKey(teleporterType) &&
                worldQueues.get(teleporterType).playerId.equals(player.getUniqueId())) {
            sendTeleporterMessage(player, teleporterCancelledMessage, teleporterMessageType);
            cancelTeleporterRequest(player, teleporterType);
            return;
        }

        // Check if someone is waiting in queue
        if (worldQueues.containsKey(teleporterType)) {
            TeleporterQueue waiting = worldQueues.get(teleporterType);
            Player waitingPlayer = Bukkit.getPlayer(waiting.playerId);

            // Remove offline players from queue
            if (waitingPlayer == null || !waitingPlayer.isOnline()) {
                worldQueues.remove(teleporterType);
                cancelTimeoutTask(waiting.playerId);
            } else if (!waiting.playerId.equals(player.getUniqueId())) {
                // Match found - perform swap
                cancelTimeoutTask(waiting.playerId);

                sendTeleporterMessage(waitingPlayer, teleporterMatchedMessage, teleporterMessageType);
                sendTeleporterMessage(player, teleporterMatchedMessage, teleporterMessageType);

                Location playerLoc = player.getLocation().clone();
                Location waitingLoc = waitingPlayer.getLocation().clone();

                // Safety checks
                if (!isSafeLocation(waitingLoc, true) || !isSafeLocation(playerLoc, true)) {
                    sendTeleporterMessage(player, elevatorDangerMessage, elevatorMessageType);
                    sendTeleporterMessage(waitingPlayer, elevatorDangerMessage, elevatorMessageType);
                    worldQueues.remove(teleporterType);
                    return;
                }

                if (!allowCrossWorlds && !playerLoc.getWorld().equals(waitingLoc.getWorld())) {
                    sendTeleporterMessage(player, "&#FF5555❌ Cannot swap across worlds!", teleporterMessageType);
                    sendTeleporterMessage(waitingPlayer, "&#FF5555❌ Cannot swap across worlds!", teleporterMessageType);
                    worldQueues.remove(teleporterType);
                    return;
                }

                playEffects(playerLoc, teleporterUsageSound, true);
                playEffects(waitingLoc, teleporterUsageSound, true);

                // Perform teleport swap
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.teleport(waitingLoc);
                        waitingPlayer.teleport(playerLoc);

                        playEffects(waitingLoc, teleporterUsageSound, true);
                        playEffects(playerLoc, teleporterUsageSound, true);

                        sendTeleporterMessage(player, teleporterSwappedMessage, teleporterMessageType);
                        sendTeleporterMessage(waitingPlayer, teleporterSwappedMessage, teleporterMessageType);

                        setCooldown(player);
                        setCooldown(waitingPlayer);
                    }
                }.runTask(this);

                worldQueues.remove(teleporterType);
                return;
            }
        }

        // No match found - add player to queue
        addToTeleporterQueue(player, teleporterType);
    }

    // Adds player to teleporter queue with timeout
    private void addToTeleporterQueue(Player player, Material teleporterType) {
        String queueKey = getQueueKey(player, teleporterType);
        Map<Material, TeleporterQueue> worldQueues = teleporterQueues.computeIfAbsent(queueKey, k -> new ConcurrentHashMap<>());

        worldQueues.put(teleporterType, new TeleporterQueue(player.getUniqueId(), System.currentTimeMillis(), teleporterType));

        String msg = teleporterWaitingMessage.replace("%time%", String.valueOf(warmupTime));
        sendTeleporterMessage(player, msg, teleporterMessageType);

        // Auto-cancel after warmup time
        BukkitTask timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                Map<Material, TeleporterQueue> currentQueues = teleporterQueues.get(queueKey);
                if (currentQueues != null && currentQueues.containsKey(teleporterType) &&
                        currentQueues.get(teleporterType).playerId.equals(player.getUniqueId())) {
                    sendTeleporterMessage(player, teleporterTimeoutMessage, teleporterMessageType);
                    currentQueues.remove(teleporterType);
                    timeoutTasks.remove(player.getUniqueId());

                    if (currentQueues.isEmpty()) {
                        teleporterQueues.remove(queueKey);
                    }
                }
            }
        }.runTaskLater(this, warmupTime * 20L);

        timeoutTasks.put(player.getUniqueId(), timeoutTask);
    }

    // Cancels player's pending teleporter request
    private void cancelTeleporterRequest(Player player, Material teleporterType) {
        String queueKey = getQueueKey(player, teleporterType);
        Map<Material, TeleporterQueue> worldQueues = teleporterQueues.get(queueKey);

        if (worldQueues != null && worldQueues.containsKey(teleporterType) &&
                worldQueues.get(teleporterType).playerId.equals(player.getUniqueId())) {
            worldQueues.remove(teleporterType);
            cancelTimeoutTask(player.getUniqueId());

            if (worldQueues.isEmpty()) {
                teleporterQueues.remove(queueKey);
            }
        }
    }

    // Cancels timeout task for player
    private void cancelTimeoutTask(UUID playerId) {
        if (timeoutTasks.containsKey(playerId)) {
            timeoutTasks.get(playerId).cancel();
            timeoutTasks.remove(playerId);
        }
    }

    // Teleports player down to nearest elevator below
    private void teleportDown(Player player) {
        if (isInDisabledWorld(player)) return;
        if (!hasPermission(player, permUse)) return;
        if (isOnCooldown(player)) return;

        Location feetLocation = player.getLocation().clone();
        feetLocation.setY(feetLocation.getY());

        if (!elevatorBlocks.contains(feetLocation.getBlock().getType())) return;

        for (int i = 1; i <= blockDistance; i++) {
            Location checkLoc = feetLocation.clone().subtract(0, i, 0);
            if (elevatorBlocks.contains(checkLoc.getBlock().getType())) {
                Location targetLoc = checkLoc.clone();

                if (!isSafeLocation(targetLoc, false)) {
                    sendMessage(player, elevatorDangerMessage, elevatorMessageType);
                    return;
                }

                player.teleport(targetLoc);
                playEffects(targetLoc, usageSound, false);
                sendMessage(player, elevatorDownMessage, elevatorMessageType);
                setCooldown(player);
                return;
            }
        }
    }

    // Teleports player up to nearest elevator above
    private void teleportUp(Player player) {
        if (isInDisabledWorld(player)) return;
        if (!hasPermission(player, permUse)) return;
        if (isOnCooldown(player)) return;

        Location feetLocation = player.getLocation().clone();
        feetLocation.setY(feetLocation.getY());

        if (!elevatorBlocks.contains(feetLocation.getBlock().getType())) return;

        for (int i = 1; i <= blockDistance; i++) {
            Location checkLoc = feetLocation.clone().add(0, i, 0);
            if (elevatorBlocks.contains(checkLoc.getBlock().getType())) {
                Location targetLoc = checkLoc.clone();

                if (!isSafeLocation(targetLoc, false)) {
                    sendMessage(player, elevatorDangerMessage, elevatorMessageType);
                    return;
                }

                player.setVelocity(player.getVelocity().setY(0));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.teleport(targetLoc);
                        playEffects(targetLoc, usageSound, false);
                        sendMessage(player, elevatorUpMessage, elevatorMessageType);
                        setCooldown(player);
                    }
                }.runTask(Main.this);
                return;
            }
        }
    }

    // Detects player jump and triggers upward teleport
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJump(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (event.getTo().getY() <= event.getFrom().getY()) return;
        if (event.getFrom().getBlock().getY() == event.getTo().getBlock().getY()) return;

        Location feetLocation = player.getLocation().clone();
        feetLocation.setY(feetLocation.getY() - 0.1);

        if (hasPermission(player, permUse) && elevatorBlocks.contains(feetLocation.getBlock().getType())) {
            for (int i = 1; i <= blockDistance; i++) {
                Location checkLoc = feetLocation.clone().add(0, i, 0);
                if (elevatorBlocks.contains(checkLoc.getBlock().getType())) {
                    Location targetLoc = checkLoc.clone().subtract(0, 0.65, 0);

                    if (!isSafeLocation(targetLoc, false)) {
                        sendMessage(player, elevatorDangerMessage, elevatorMessageType);
                        return;
                    }

                    player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                    player.setFallDistance(0);

                    player.teleport(targetLoc);
                    playEffects(targetLoc, usageSound, false);
                    sendMessage(player, elevatorUpMessage, elevatorMessageType);
                    setCooldown(player);
                    return;
                }
            }
        }
    }

    // Detects player sneak and triggers downward teleport after delay
    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking()) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !player.isSneaking()) return;
                teleportDown(player);
            }
        }.runTaskLater(this, SNEAK_CHECK_DELAY_TICKS);
    }

    // Detects right-click on teleporter blocks
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Material blockType = event.getClickedBlock().getType();

        if (teleporterBlocks.contains(blockType)) {
            event.setCancelled(true);

            // Prevent double-click spam
            if (recentTeleporterClicks.contains(player.getUniqueId())) {
                return;
            }

            recentTeleporterClicks.add(player.getUniqueId());

            new BukkitRunnable() {
                @Override
                public void run() {
                    recentTeleporterClicks.remove(player.getUniqueId());
                }
            }.runTaskLater(this, DOUBLE_CLICK_DELAY_TICKS);

            handleTeleporterClick(player, event.getClickedBlock().getLocation(), blockType);
        }
    }

    // Cleans up player data on quit
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        cancelTimeoutTask(player.getUniqueId());
        recentTeleporterClicks.remove(player.getUniqueId());

        for (Map<Material, TeleporterQueue> worldQueues : teleporterQueues.values()) {
            worldQueues.entrySet().removeIf(entry ->
                    entry.getValue().playerId.equals(player.getUniqueId()));
        }

        teleporterQueues.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}
