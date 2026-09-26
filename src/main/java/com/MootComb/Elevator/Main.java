package com.MootComb.Elevator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Main extends JavaPlugin implements Listener {

    private static Main instance;

    public enum MessageType { CHAT, TITLE, SUBTITLE }
    public enum AccessLevel { OWNER, MEMBERS, OWNERS, ALL }
    public enum ClickType { LEFT, RIGHT }

    private static final int DOUBLE_CLICK_DELAY_TICKS = 5;
    private static final int SNEAK_CHECK_DELAY_TICKS = 1;
    private static final int DEFAULT_END_ROD_COUNT = 5;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private Set<Material> elevatorBlocks = new HashSet<>();
    private int blockDistance;
    private boolean enableParticle;
    private Particle particleType;
    private int particleCount;
    private String usageSound;
    private String activateSound;
    private boolean allowUnsafe;
    private Set<String> disabledWorlds = new HashSet<>();

    private boolean teleporterEnableParticle;
    private String teleporterUsageSound;
    private boolean teleporterAllowUnsafe;
    private boolean allowCrossWorlds;

    private boolean teleporterAllowAllBlocks;
    private Set<Material> teleporterBlockTypes = new HashSet<>();
    private String teleporterBlockTypesPermission;

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

    private boolean checkPermission;
    private String permUse;
    private String permBypass;
    private String permTeleport;
    private String permManage;
    private String permBreakBypass;

    private boolean featureTeleportEnabled;
    private boolean featureTeleportCheckPermission;
    private String featureTeleportPermission;
    private AccessLevel featureTeleportDefaultAccess;

    private boolean featureManageEnabled;
    private boolean featureManageCheckPermission;
    private String featureManagePermission;
    private AccessLevel featureManageDefaultAccess;

    private boolean featureBreakEnabled;
    private boolean featureBreakCheckPermission;
    private String featureBreakPermission;
    private AccessLevel featureBreakDefaultAccess;

    private boolean debug;

    private File blocksFile;
    private FileConfiguration blocksConfig;

    private final Map<UUID, Long> cooldownMap = new HashMap<>();
    private final Set<UUID> recentInteractions = ConcurrentHashMap.newKeySet();

    private final Map<String, BlockData> blockDataMap = new ConcurrentHashMap<>();
    private final Map<Integer, Set<String>> idIndex = new ConcurrentHashMap<>();
    private final Map<UUID, List<String>> playerBlocks = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> globalMembers = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> globalOwners = new ConcurrentHashMap<>();

    private final Map<UUID, ChatInputSession> chatSessions = new HashMap<>();
    private final Map<UUID, Inventory> openGuis = new HashMap<>();
    private final Map<UUID, GuiContext> guiContexts = new HashMap<>();

    private final Map<UUID, Long> lastSneakTime = new HashMap<>();

    public static class BlockData {
        public int id;
        public UUID owner;
        public String world;
        public int x;
        public int y;
        public int z;
        public ClickType clickType = ClickType.RIGHT;
        public boolean requireSneak = false;
        public boolean requireItem = false;
        public String requiredItemName = "";
        public AccessLevel teleportAccess = AccessLevel.ALL;
        public AccessLevel manageAccess = AccessLevel.OWNER;
        public AccessLevel breakAccess = AccessLevel.ALL;
        public Set<UUID> members = new HashSet<>();
        public Set<UUID> owners = new HashSet<>();

        public String key() {
            return world + ":" + x + ":" + y + ":" + z;
        }
    }

    public static class ChatInputSession {
        public String type;
        public String blockKey;
        public long expireTime;

        public ChatInputSession(String type, String blockKey) {
            this.type = type;
            this.blockKey = blockKey;
            this.expireTime = System.currentTimeMillis() + 60000L;
        }
    }

    public static class GuiContext {
        public String blockKey;
        public String guiType;
        public int page = 0;

        public GuiContext(String blockKey, String guiType) {
            this.blockKey = blockKey;
            this.guiType = guiType;
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        loadConfig();
        loadBlocks();
        getServer().getPluginManager().registerEvents(this, this);

        if (getCommand("elevator") != null) {
            getCommand("elevator").setExecutor(this);
        }

        getLogger().info("==================================================");
        getLogger().info("   Elevator Plugin Enabled!");
        getLogger().info("   Elevator blocks: " + elevatorBlocks.size());
        getLogger().info("   Registered blocks: " + blockDataMap.size());
        getLogger().info("   Max distance: " + blockDistance);
        getLogger().info("==================================================");
    }

    @Override
    public void onDisable() {
        saveBlocks();
        getLogger().info("Elevator Disabled!");
    }

    private String translateHexColors(String message) {
        if (message == null) return "";
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    private void debug(String message) {
        if (debug) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    private void loadConfig() {
        saveDefaultConfig();
        reloadConfig();

        elevatorBlocks.clear();
        for (String blockName : getConfig().getStringList("Elevator.BlockTypes")) {
            Material mat = Material.getMaterial(blockName);
            if (mat != null) elevatorBlocks.add(mat);
            else getLogger().warning("Unknown elevator block: " + blockName);
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

        teleporterEnableParticle = getConfig().getBoolean("Teleporter.EnableParticle", true);
        teleporterUsageSound = getConfig().getString("Teleporter.UsageSound", "entity.enderman.teleport");
        teleporterAllowUnsafe = getConfig().getBoolean("Teleporter.AllowUnsafe", true);
        allowCrossWorlds = getConfig().getBoolean("Teleporter.AllowCrossWorlds", true);
        teleporterAllowAllBlocks = getConfig().getBoolean("Teleporter.AllowAllBlocks", false);
        teleporterBlockTypesPermission = getConfig().getString("Teleporter.BlockTypesPermission", "");

        teleporterBlockTypes.clear();
        List<String> teleporterBlockNames = getConfig().getStringList("Teleporter.BlockTypes");
        if (teleporterBlockNames.isEmpty()) {
            teleporterBlockNames.add("SEA_LANTERN");
        }
        for (String blockName : teleporterBlockNames) {
            Material mat = Material.getMaterial(blockName);
            if (mat != null) teleporterBlockTypes.add(mat);
            else getLogger().warning("Unknown teleporter block: " + blockName);
        }

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

        elevatorUpMessage = getConfig().getString("ElevatorLocale.ElevatorUp", "&#55FF55Going up");
        elevatorDownMessage = getConfig().getString("ElevatorLocale.ElevatorDown", "&#FFAA00Going down");
        elevatorDangerMessage = getConfig().getString("ElevatorLocale.ElevatorDanger", "&#FF5555Danger! Unsafe location!");

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

        disabledWorlds.clear();
        disabledWorlds.addAll(getConfig().getStringList("DisabledWorlds"));

        checkPermission = getConfig().getBoolean("Permissions.CheckPermission", false);
        permUse = getConfig().getString("Permissions.Use", "elevator.use");
        permBypass = getConfig().getString("Permissions.BypassCooldown", "elevator.bypass");
        permTeleport = getConfig().getString("Permissions.Teleport", "elevator.teleport");
        permManage = getConfig().getString("Permissions.Manage", "elevator.manage");
        permBreakBypass = getConfig().getString("Permissions.BreakBypass", "elevator.break.bypass");

        featureTeleportEnabled = getConfig().getBoolean("TeleporterBlock.Teleport.Enabled", true);
        featureTeleportCheckPermission = getConfig().getBoolean("TeleporterBlock.Teleport.CheckPermission", false);
        featureTeleportPermission = getConfig().getString("TeleporterBlock.Teleport.Permission", "elevator.teleport");
        featureTeleportDefaultAccess = getAccessLevel(getConfig().getString("TeleporterBlock.Teleport.DefaultAccess", "ALL"));

        featureManageEnabled = getConfig().getBoolean("TeleporterBlock.Manage.Enabled", true);
        featureManageCheckPermission = getConfig().getBoolean("TeleporterBlock.Manage.CheckPermission", false);
        featureManagePermission = getConfig().getString("TeleporterBlock.Manage.Permission", "elevator.manage");
        featureManageDefaultAccess = getAccessLevel(getConfig().getString("TeleporterBlock.Manage.DefaultAccess", "OWNER"));

        featureBreakEnabled = getConfig().getBoolean("TeleporterBlock.Break.Enabled", true);
        featureBreakCheckPermission = getConfig().getBoolean("TeleporterBlock.Break.CheckPermission", false);
        featureBreakPermission = getConfig().getString("TeleporterBlock.Break.Permission", "elevator.break");
        featureBreakDefaultAccess = getAccessLevel(getConfig().getString("TeleporterBlock.Break.DefaultAccess", "ALL"));

        debug = getConfig().getBoolean("Debug", false);
    }

    private MessageType getMessageType(String type) {
        try {
            return MessageType.valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return MessageType.CHAT;
        }
    }

    private AccessLevel getAccessLevel(String level) {
        try {
            return AccessLevel.valueOf(level.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return AccessLevel.ALL;
        }
    }

    private ClickType getClickType(String type) {
        try {
            return ClickType.valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ClickType.RIGHT;
        }
    }

    private void loadBlocks() {
        blockDataMap.clear();
        idIndex.clear();
        playerBlocks.clear();
        globalMembers.clear();
        globalOwners.clear();

        blocksFile = new File(getDataFolder(), "blocks.yml");
        if (!blocksFile.exists()) {
            try {
                getDataFolder().mkdirs();
                blocksFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create blocks.yml: " + e.getMessage());
                return;
            }
        }
        blocksConfig = YamlConfiguration.loadConfiguration(blocksFile);

        ConfigurationSection blocksSection = blocksConfig.getConfigurationSection("Blocks");
        if (blocksSection != null) {
            for (String key : blocksSection.getKeys(false)) {
                ConfigurationSection sec = blocksSection.getConfigurationSection(key);
                if (sec == null) continue;

                BlockData data = new BlockData();
                data.id = sec.getInt("Id", 0);
                String ownerStr = sec.getString("Owner", "");
                try {
                    data.owner = UUID.fromString(ownerStr);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                data.world = sec.getString("World", "world");
                data.x = sec.getInt("X", 0);
                data.y = sec.getInt("Y", 0);
                data.z = sec.getInt("Z", 0);
                data.clickType = getClickType(sec.getString("ClickType", "RIGHT"));
                data.requireSneak = sec.getBoolean("RequireSneak", false);
                data.requireItem = sec.getBoolean("RequireItem", false);
                data.requiredItemName = sec.getString("RequiredItemName", "");
                data.teleportAccess = getAccessLevel(sec.getString("TeleportAccess", "ALL"));
                data.manageAccess = getAccessLevel(sec.getString("ManageAccess", "OWNER"));
                data.breakAccess = getAccessLevel(sec.getString("BreakAccess", "ALL"));

                for (String member : sec.getStringList("Members")) {
                    try {
                        data.members.add(UUID.fromString(member));
                    } catch (IllegalArgumentException ignored) {}
                }
                for (String owner : sec.getStringList("Owners")) {
                    try {
                        data.owners.add(UUID.fromString(owner));
                    } catch (IllegalArgumentException ignored) {}
                }

                blockDataMap.put(data.key(), data);
                idIndex.computeIfAbsent(data.id, k -> ConcurrentHashMap.newKeySet()).add(data.key());
                playerBlocks.computeIfAbsent(data.owner, k -> new ArrayList<>()).add(data.key());
            }
        }

        ConfigurationSection playersSection = blocksConfig.getConfigurationSection("Players");
        if (playersSection != null) {
            for (String uuidStr : playersSection.getKeys(false)) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(uuidStr);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                ConfigurationSection pSec = playersSection.getConfigurationSection(uuidStr);
                if (pSec == null) continue;

                Set<UUID> members = ConcurrentHashMap.newKeySet();
                for (String m : pSec.getStringList("GlobalMembers")) {
                    try {
                        members.add(UUID.fromString(m));
                    } catch (IllegalArgumentException ignored) {}
                }
                Set<UUID> owners = ConcurrentHashMap.newKeySet();
                for (String o : pSec.getStringList("GlobalOwners")) {
                    try {
                        owners.add(UUID.fromString(o));
                    } catch (IllegalArgumentException ignored) {}
                }
                if (!members.isEmpty()) globalMembers.put(uuid, members);
                if (!owners.isEmpty()) globalOwners.put(uuid, owners);
            }
        }
    }

    private void saveBlocks() {
        if (blocksConfig == null || blocksFile == null) return;

        blocksConfig.set("Blocks", null);
        for (Map.Entry<String, BlockData> entry : blockDataMap.entrySet()) {
            BlockData data = entry.getValue();
            String path = "Blocks." + entry.getKey().replace(".", "_");
            blocksConfig.set(path + ".Id", data.id);
            blocksConfig.set(path + ".Owner", data.owner.toString());
            blocksConfig.set(path + ".World", data.world);
            blocksConfig.set(path + ".X", data.x);
            blocksConfig.set(path + ".Y", data.y);
            blocksConfig.set(path + ".Z", data.z);
            blocksConfig.set(path + ".ClickType", data.clickType.name());
            blocksConfig.set(path + ".RequireSneak", data.requireSneak);
            blocksConfig.set(path + ".RequireItem", data.requireItem);
            blocksConfig.set(path + ".RequiredItemName", data.requiredItemName);
            blocksConfig.set(path + ".TeleportAccess", data.teleportAccess.name());
            blocksConfig.set(path + ".ManageAccess", data.manageAccess.name());
            blocksConfig.set(path + ".BreakAccess", data.breakAccess.name());

            List<String> members = new ArrayList<>();
            for (UUID u : data.members) members.add(u.toString());
            blocksConfig.set(path + ".Members", members);

            List<String> owners = new ArrayList<>();
            for (UUID u : data.owners) owners.add(u.toString());
            blocksConfig.set(path + ".Owners", owners);
        }

        blocksConfig.set("Players", null);
        Set<UUID> allPlayers = new HashSet<>();
        allPlayers.addAll(globalMembers.keySet());
        allPlayers.addAll(globalOwners.keySet());
        for (UUID uuid : allPlayers) {
            String path = "Players." + uuid.toString();
            List<String> members = new ArrayList<>();
            Set<UUID> m = globalMembers.get(uuid);
            if (m != null) for (UUID u : m) members.add(u.toString());
            blocksConfig.set(path + ".GlobalMembers", members);

            List<String> owners = new ArrayList<>();
            Set<UUID> o = globalOwners.get(uuid);
            if (o != null) for (UUID u : o) owners.add(u.toString());
            blocksConfig.set(path + ".GlobalOwners", owners);
        }

        try {
            blocksConfig.save(blocksFile);
        } catch (IOException e) {
            getLogger().severe("Could not save blocks.yml: " + e.getMessage());
        }
    }

    private void sendMessage(Player player, String message, MessageType type) {
        String formatted = translateHexColors(message);
        switch (type) {
            case CHAT:
                player.sendMessage(formatted);
                break;
            case TITLE:
                player.sendTitle(formatted, "", elevatorTitleFadeIn, elevatorTitleStay, elevatorTitleFadeOut);
                break;
            case SUBTITLE:
                player.sendTitle("", formatted, elevatorTitleFadeIn, elevatorTitleStay, elevatorTitleFadeOut);
                break;
        }
    }

    private void sendTeleporterMessage(Player player, String message, MessageType type) {
        String formatted = translateHexColors(message);
        switch (type) {
            case CHAT:
                player.sendMessage(formatted);
                break;
            case TITLE:
                player.sendTitle(formatted, "", teleporterTitleFadeIn, teleporterTitleStay, teleporterTitleFadeOut);
                break;
            case SUBTITLE:
                player.sendTitle("", formatted, teleporterTitleFadeIn, teleporterTitleStay, teleporterTitleFadeOut);
                break;
        }
    }

    private boolean hasPermission(Player player, String perm) {
        if (!checkPermission) return true;
        if (perm == null || perm.isEmpty()) return true;
        return player.hasPermission(perm);
    }

    private boolean isInDisabledWorld(Player player) {
        return disabledWorlds.contains(player.getWorld().getName());
    }

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

    private void setCooldown(Player player) {
        if (enableCooldown && !hasPermission(player, permBypass)) {
            cooldownMap.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    private boolean isSafeLocation(Location loc, boolean isTeleporter) {
        boolean unsafeAllowed = isTeleporter ? teleporterAllowUnsafe : allowUnsafe;
        if (unsafeAllowed) return true;
        Material type = loc.getBlock().getType();
        return !type.isSolid() && type != Material.LAVA && type != Material.FIRE;
    }

    private void spawnElevatorParticles(Location loc) {
        if (!enableParticle) return;
        World world = loc.getWorld();
        if (world == null) return;
        world.spawnParticle(particleType, loc.clone().add(0, 0.5, 0), particleCount, 0.3, 0.1, 0.3, 0.1);
        world.spawnParticle(Particle.END_ROD, loc.clone().add(0, 0.5, 0), DEFAULT_END_ROD_COUNT, 0.2, 0.2, 0.2, 0.05);
    }

    private void spawnTeleporterParticles(Location loc) {
        if (!teleporterEnableParticle) return;
        World world = loc.getWorld();
        if (world == null) return;
        world.spawnParticle(particleType, loc.clone().add(0, 0.5, 0), particleCount, 0.3, 0.1, 0.3, 0.1);
        world.spawnParticle(Particle.END_ROD, loc.clone().add(0, 0.5, 0), DEFAULT_END_ROD_COUNT, 0.2, 0.2, 0.2, 0.05);
    }

    private void playEffects(Location loc, String soundName, boolean isTeleporter) {
        if (soundName != null && !soundName.isEmpty()) {
            try {
                Sound sound = Sound.valueOf(soundName.toUpperCase(Locale.ROOT));
                World world = loc.getWorld();
                if (world != null) world.playSound(loc, sound, 0.5f, 1.2f);
            } catch (IllegalArgumentException ignored) {}
        }
        if (isTeleporter) spawnTeleporterParticles(loc);
        else spawnElevatorParticles(loc);
    }

    private void teleportDown(Player player) {
        if (isInDisabledWorld(player)) return;
        if (!hasPermission(player, permUse)) return;
        if (isOnCooldown(player)) return;

        Location feetLocation = player.getLocation().clone();
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

    private void teleportUp(Player player) {
        if (isInDisabledWorld(player)) return;
        if (!hasPermission(player, permUse)) return;
        if (isOnCooldown(player)) return;

        Location feetLocation = player.getLocation().clone();
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
                final Location finalLoc = targetLoc;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.teleport(finalLoc);
                        playEffects(finalLoc, usageSound, false);
                        sendMessage(player, elevatorUpMessage, elevatorMessageType);
                        setCooldown(player);
                    }
                }.runTask(this);
                return;
            }
        }
    }

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

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking()) return;
        final Player p = player;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!p.isOnline() || !p.isSneaking()) return;
                teleportDown(p);
            }
        }.runTaskLater(this, SNEAK_CHECK_DELAY_TICKS);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        Material blockType = block.getType();
        Action action = event.getAction();
        boolean isRight = action == Action.RIGHT_CLICK_BLOCK;
        boolean isLeft = action == Action.LEFT_CLICK_BLOCK;
        if (!isRight && !isLeft) return;

        String key = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
        BlockData data = blockDataMap.get(key);

        ItemStack handItem = player.getInventory().getItemInMainHand();
        boolean holdingEnderPearl = handItem != null && handItem.getType() == Material.ENDER_PEARL;

        if (data == null) {
            if (isRight && holdingEnderPearl) {
                tryBindBlock(player, block, blockType);
            }
            return;
        }

        if (isRight && player.isSneaking() && !holdingEnderPearl) {
            if (!featureManageEnabled) {
                debug("Manage feature disabled. Player " + player.getName());
                return;
            }
            if (!canManage(player, data)) {
                debug("Player " + player.getName() + " cannot manage block " + key);
                return;
            }
            event.setCancelled(true);
            openMainMenu(player, data);
            return;
        }

        if (isRight && player.isSneaking() && holdingEnderPearl) {
            if (!featureManageEnabled) {
                debug("Manage feature disabled. Player " + player.getName());
                return;
            }
            if (!canManage(player, data)) {
                debug("Player " + player.getName() + " cannot manage block " + key);
                return;
            }
            event.setCancelled(true);
            openMainMenu(player, data);
            return;
        }

        if (!featureTeleportEnabled) {
            debug("Teleport feature disabled. Player " + player.getName());
            return;
        }

        if (!canTeleport(player, data)) {
            debug("Player " + player.getName() + " cannot teleport using block " + key);
            return;
        }

        ClickType clicked = isRight ? ClickType.RIGHT : ClickType.LEFT;
        if (data.clickType != clicked) return;
        if (data.requireSneak && !player.isSneaking()) return;
        if (!data.requireSneak && player.isSneaking()) return;

        if (data.requireItem) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!itemMatches(item, data.requiredItemName)) {
                debug("Player " + player.getName() + " missing required item on block " + key);
                return;
            }
        }

        if (recentInteractions.contains(player.getUniqueId())) return;
        recentInteractions.add(player.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {
                recentInteractions.remove(player.getUniqueId());
            }
        }.runTaskLater(this, DOUBLE_CLICK_DELAY_TICKS);

        event.setCancelled(true);
        performTeleport(player, data);
    }

    private boolean itemMatches(ItemStack item, String requiredName) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (requiredName == null || requiredName.isEmpty()) return true;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        String display = ChatColor.stripColor(meta.getDisplayName());
        String required = ChatColor.stripColor(translateHexColors(requiredName));
        return display.equalsIgnoreCase(required);
    }

    private void tryBindBlock(Player player, Block block, Material blockType) {
        if (isInDisabledWorld(player)) {
            debug("Player " + player.getName() + " in disabled world");
            return;
        }

        if (!teleporterAllowAllBlocks) {
            if (!teleporterBlockTypes.contains(blockType)) {
                boolean allowedByPerm = false;
                if (teleporterBlockTypesPermission != null && !teleporterBlockTypesPermission.isEmpty()) {
                    allowedByPerm = player.hasPermission(teleporterBlockTypesPermission);
                }
                if (!allowedByPerm) {
                    debug("Block type " + blockType + " not allowed for binding");
                    return;
                }
            }
        } else {
            if (teleporterBlockTypesPermission != null && !teleporterBlockTypesPermission.isEmpty()
                    && !player.hasPermission(teleporterBlockTypesPermission)) {
                debug("Player " + player.getName() + " missing bind permission");
                return;
            }
        }

        if (!hasPermission(player, permTeleport)) {
            debug("Player " + player.getName() + " missing teleport permission");
            return;
        }

        String key = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
        if (blockDataMap.containsKey(key)) {
            debug("Block already bound: " + key);
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() != Material.ENDER_PEARL) return;

        if (hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }

        BlockData data = new BlockData();
        data.owner = player.getUniqueId();
        data.world = block.getWorld().getName();
        data.x = block.getX();
        data.y = block.getY();
        data.z = block.getZ();
        data.clickType = ClickType.RIGHT;
        data.requireSneak = false;
        data.requireItem = false;
        data.requiredItemName = "";
        data.teleportAccess = featureTeleportDefaultAccess;
        data.manageAccess = featureManageDefaultAccess;
        data.breakAccess = featureBreakDefaultAccess;
        data.id = 0;

        blockDataMap.put(key, data);
        playerBlocks.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(key);
        saveBlocks();

        playEffects(block.getLocation(), activateSound, true);
        sendTeleporterMessage(player, "&#55FF55Block bound! Set an ID to link it.", teleporterMessageType);
        openMainMenu(player, data);
    }

    private void performTeleport(Player player, BlockData source) {
        int id = source.id;
        if (id <= 0) {
            debug("Block " + source.key() + " has no ID set");
            return;
        }

        Set<String> keys = idIndex.get(id);
        if (keys == null || keys.size() < 2) {
            debug("No pair found for ID " + id);
            return;
        }

        BlockData destination = null;
        for (String k : keys) {
            if (!k.equals(source.key())) {
                destination = blockDataMap.get(k);
                break;
            }
        }

        if (destination == null) {
            debug("Destination block not found for ID " + id);
            return;
        }

        World destWorld = Bukkit.getWorld(destination.world);
        if (destWorld == null) {
            debug("Destination world not loaded: " + destination.world);
            return;
        }

        if (!allowCrossWorlds) {
            if (!player.getWorld().getName().equals(destination.world)) {
                sendTeleporterMessage(player, "&#FF5555Cannot teleport across worlds!", teleporterMessageType);
                return;
            }
        }

        Location destLoc = new Location(destWorld,
                destination.x + 0.5,
                destination.y + 1,
                destination.z + 0.5,
                player.getLocation().getYaw(),
                player.getLocation().getPitch());

        if (!isSafeLocation(destLoc, true)) {
            sendTeleporterMessage(player, elevatorDangerMessage, teleporterMessageType);
            return;
        }

        Location sourceLoc = player.getLocation().clone();
        playEffects(sourceLoc, teleporterUsageSound, true);
        playEffects(destLoc, teleporterUsageSound, true);

        player.teleport(destLoc);
        playEffects(destLoc, teleporterUsageSound, true);
        sendTeleporterMessage(player, "&#55FFFFTeleported!", teleporterMessageType);
        setCooldown(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String key = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
        BlockData data = blockDataMap.get(key);
        if (data == null) return;

        if (!featureBreakEnabled) {
            event.setCancelled(true);
            debug("Break feature disabled. Player " + player.getName());
            return;
        }

        if (checkPermission && permBreakBypass != null && !permBreakBypass.isEmpty()
                && player.hasPermission(permBreakBypass)) {
            removeBlock(data);
            return;
        }

        if (!canBreak(player, data)) {
            event.setCancelled(true);
            debug("Player " + player.getName() + " cannot break block " + key);
            return;
        }

        removeBlock(data);
    }

    private void removeBlock(BlockData data) {
        String key = data.key();
        blockDataMap.remove(key);
        Set<String> keys = idIndex.get(data.id);
        if (keys != null) {
            keys.remove(key);
            if (keys.isEmpty()) idIndex.remove(data.id);
        }
        List<String> list = playerBlocks.get(data.owner);
        if (list != null) {
            list.remove(key);
            if (list.isEmpty()) playerBlocks.remove(data.owner);
        }
        saveBlocks();
    }

    private boolean canTeleport(Player player, BlockData data) {
        if (featureTeleportCheckPermission && !hasPermission(player, featureTeleportPermission)) {
            return false;
        }
        AccessLevel level = data.teleportAccess;
        if (level == AccessLevel.ALL) return true;
        if (level == AccessLevel.OWNER) return player.getUniqueId().equals(data.owner);
        if (level == AccessLevel.MEMBERS) {
            if (player.getUniqueId().equals(data.owner)) return true;
            if (data.members.contains(player.getUniqueId())) return true;
            Set<UUID> global = globalMembers.get(data.owner);
            if (global != null && global.contains(player.getUniqueId())) return true;
            return false;
        }
        if (level == AccessLevel.OWNERS) {
            if (player.getUniqueId().equals(data.owner)) return true;
            if (data.owners.contains(player.getUniqueId())) return true;
            Set<UUID> global = globalOwners.get(data.owner);
            if (global != null && global.contains(player.getUniqueId())) return true;
            return false;
        }
        return false;
    }

    private boolean canManage(Player player, BlockData data) {
        if (featureManageCheckPermission && !hasPermission(player, featureManagePermission)) {
            return false;
        }
        AccessLevel level = data.manageAccess;
        if (level == AccessLevel.ALL) return true;
        if (level == AccessLevel.OWNER) return player.getUniqueId().equals(data.owner);
        if (level == AccessLevel.OWNERS) {
            if (player.getUniqueId().equals(data.owner)) return true;
            if (data.owners.contains(player.getUniqueId())) return true;
            Set<UUID> global = globalOwners.get(data.owner);
            if (global != null && global.contains(player.getUniqueId())) return true;
            return false;
        }
        return false;
    }

    private boolean canBreak(Player player, BlockData data) {
        if (featureBreakCheckPermission && !hasPermission(player, featureBreakPermission)) {
            return false;
        }
        AccessLevel level = data.breakAccess;
        if (level == AccessLevel.ALL) return true;
        if (level == AccessLevel.OWNER) return player.getUniqueId().equals(data.owner);
        if (level == AccessLevel.MEMBERS) {
            if (player.getUniqueId().equals(data.owner)) return true;
            if (data.members.contains(player.getUniqueId())) return true;
            Set<UUID> global = globalMembers.get(data.owner);
            if (global != null && global.contains(player.getUniqueId())) return true;
            return false;
        }
        if (level == AccessLevel.OWNERS) {
            if (player.getUniqueId().equals(data.owner)) return true;
            if (data.owners.contains(player.getUniqueId())) return true;
            Set<UUID> global = globalOwners.get(data.owner);
            if (global != null && global.contains(player.getUniqueId())) return true;
            return false;
        }
        return false;
    }

    private void openMainMenu(Player player, BlockData data) {
        Inventory inv = Bukkit.createInventory(null, 27, translateHexColors("&#55FFFFTeleporter Block"));

        inv.setItem(4, createItem(Material.ENDER_PEARL,
                "&#55FFFFID: " + (data.id > 0 ? data.id : "Not set"),
                "&#AAAAAAClick to change ID"));

        inv.setItem(10, createItem(Material.LEVER,
                "&#FFFF55Click Type: " + data.clickType.name(),
                "&#AAAAAALeft or Right mouse button"));

        inv.setItem(11, createItem(Material.SHIELD,
                "&#FFFF55Require Sneak: " + (data.requireSneak ? "Yes" : "No"),
                "&#AAAAAAToggle sneak requirement"));

        inv.setItem(12, createItem(Material.NAME_TAG,
                "&#FFFF55Require Item: " + (data.requireItem ? "Yes" : "No"),
                "&#AAAAAARequired name: " + (data.requiredItemName.isEmpty() ? "-" : data.requiredItemName),
                "&#AAAAAAClick to change"));

        inv.setItem(14, createItem(Material.PLAYER_HEAD,
                "&#55FF55Teleport Access: " + data.teleportAccess.name(),
                "&#AAAAAAClick to cycle",
                "&#AAAAAAOWNER / MEMBERS / OWNERS / ALL"));

        inv.setItem(15, createItem(Material.COMMAND_BLOCK,
                "&#55FF55Manage Access: " + data.manageAccess.name(),
                "&#AAAAAAClick to cycle",
                "&#AAAAAAOWNER / OWNERS / ALL"));

        inv.setItem(16, createItem(Material.DIAMOND_PICKAXE,
                "&#55FF55Break Access: " + data.breakAccess.name(),
                "&#AAAAAAClick to cycle",
                "&#AAAAAAOWNER / MEMBERS / OWNERS / ALL"));

        inv.setItem(22, createItem(Material.BOOK,
                "&#FFAA00Members & Owners",
                "&#AAAAAAManage local and global lists"));

        inv.setItem(26, createItem(Material.BARRIER, "&#FF5555Close"));

        player.openInventory(inv);
        openGuis.put(player.getUniqueId(), inv);
        guiContexts.put(player.getUniqueId(), new GuiContext(data.key(), "MAIN"));
    }

    private void openMembersMenu(Player player, BlockData data) {
        Inventory inv = Bukkit.createInventory(null, 54, translateHexColors("&#FFAA00Members & Owners"));

        inv.setItem(10, createItem(Material.PLAYER_HEAD,
                "&#55FF55Local Members: " + data.members.size(),
                "&#AAAAAAClick to manage"));

        inv.setItem(11, createItem(Material.PLAYER_HEAD,
                "&#55FF55Local Owners: " + data.owners.size(),
                "&#AAAAAAClick to manage"));

        inv.setItem(12, createItem(Material.PLAYER_HEAD,
                "&#55FF55Global Members: " + getGlobalMembers(data.owner).size(),
                "&#AAAAAAClick to manage"));

        inv.setItem(13, createItem(Material.PLAYER_HEAD,
                "&#55FF55Global Owners: " + getGlobalOwners(data.owner).size(),
                "&#AAAAAAClick to manage"));

        inv.setItem(49, createItem(Material.ARROW, "&#FFFF55Back"));

        player.openInventory(inv);
        openGuis.put(player.getUniqueId(), inv);
        guiContexts.put(player.getUniqueId(), new GuiContext(data.key(), "MEMBERS"));
    }

    private void openPlayerListView(Player player, BlockData data, String listType, int page) {
        Set<UUID> list;
        String title;
        switch (listType) {
            case "LOCAL_MEMBERS":
                list = data.members;
                title = "&#55FF55Local Members";
                break;
            case "LOCAL_OWNERS":
                list = data.owners;
                title = "&#55FF55Local Owners";
                break;
            case "GLOBAL_MEMBERS":
                list = getGlobalMembers(data.owner);
                title = "&#55FF55Global Members";
                break;
            case "GLOBAL_OWNERS":
                list = getGlobalOwners(data.owner);
                title = "&#55FF55Global Owners";
                break;
            default:
                return;
        }

        List<UUID> sorted = new ArrayList<>(list);
        int perPage = 45;
        int totalPages = Math.max(1, (int) Math.ceil(sorted.size() / (double) perPage));
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        Inventory inv = Bukkit.createInventory(null, 54, translateHexColors(title + " (" + (page + 1) + "/" + totalPages + ")"));

        int start = page * perPage;
        int end = Math.min(start + perPage, sorted.size());
        for (int i = start; i < end; i++) {
            UUID uuid = sorted.get(i);
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            if (name == null) name = uuid.toString();
            inv.setItem(i - start, createItem(Material.PLAYER_HEAD,
                    "&#55FFFF" + name,
                    "&#FF5555Click to remove"));
        }

        inv.setItem(45, createItem(Material.ARROW, "&#FFFF55Previous"));
        inv.setItem(49, createItem(Material.ARROW, "&#FFFF55Back"));
        inv.setItem(53, createItem(Material.ARROW, "&#FFFF55Next"));
        inv.setItem(48, createItem(Material.EMERALD, "&#55FF55Add player (chat input)"));

        player.openInventory(inv);
        openGuis.put(player.getUniqueId(), inv);
        GuiContext ctx = new GuiContext(data.key(), "LIST_" + listType);
        ctx.page = page;
        guiContexts.put(player.getUniqueId(), ctx);
    }

    private Set<UUID> getGlobalMembers(UUID owner) {
        return globalMembers.computeIfAbsent(owner, k -> ConcurrentHashMap.newKeySet());
    }

    private Set<UUID> getGlobalOwners(UUID owner) {
        return globalOwners.computeIfAbsent(owner, k -> ConcurrentHashMap.newKeySet());
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(translateHexColors(name));
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String l : lore) loreList.add(translateHexColors(l));
                meta.setLore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        GuiContext ctx = guiContexts.get(player.getUniqueId());
        if (ctx == null) return;
        if (event.getInventory() != openGuis.get(player.getUniqueId())) return;

        event.setCancelled(true);
        BlockData data = blockDataMap.get(ctx.blockKey);
        if (data == null) {
            player.closeInventory();
            return;
        }

        int slot = event.getRawSlot();
        String guiType = ctx.guiType;

        if (guiType.equals("MAIN")) {
            switch (slot) {
                case 4: {
                    player.closeInventory();
                    player.sendMessage(translateHexColors("&#FFFF55Type a number in chat (1-99999) to set the ID, or 'cancel' to abort."));
                    chatSessions.put(player.getUniqueId(), new ChatInputSession("SET_ID", data.key()));
                    return;
                }
                case 10: {
                    data.clickType = data.clickType == ClickType.LEFT ? ClickType.RIGHT : ClickType.LEFT;
                    saveBlocks();
                    openMainMenu(player, data);
                    return;
                }
                case 11: {
                    data.requireSneak = !data.requireSneak;
                    saveBlocks();
                    openMainMenu(player, data);
                    return;
                }
                case 12: {
                    player.closeInventory();
                    player.sendMessage(translateHexColors("&#FFFF55Type item display name in chat, or 'no' to disable."));
                    chatSessions.put(player.getUniqueId(), new ChatInputSession("SET_ITEM", data.key()));
                    return;
                }
                case 14: {
                    data.teleportAccess = cycleTeleportAccess(data.teleportAccess);
                    saveBlocks();
                    openMainMenu(player, data);
                    return;
                }
                case 15: {
                    data.manageAccess = cycleManageAccess(data.manageAccess);
                    saveBlocks();
                    openMainMenu(player, data);
                    return;
                }
                case 16: {
                    data.breakAccess = cycleBreakAccess(data.breakAccess);
                    saveBlocks();
                    openMainMenu(player, data);
                    return;
                }
                case 22: {
                    openMembersMenu(player, data);
                    return;
                }
                case 26: {
                    player.closeInventory();
                    return;
                }
                default:
                    return;
            }
        }

        if (guiType.equals("MEMBERS")) {
            switch (slot) {
                case 10: openPlayerListView(player, data, "LOCAL_MEMBERS", 0); return;
                case 11: openPlayerListView(player, data, "LOCAL_OWNERS", 0); return;
                case 12: openPlayerListView(player, data, "GLOBAL_MEMBERS", 0); return;
                case 13: openPlayerListView(player, data, "GLOBAL_OWNERS", 0); return;
                case 49: openMainMenu(player, data); return;
                default: return;
            }
        }

        if (guiType.startsWith("LIST_")) {
            String listType = guiType.substring(5);
            Set<UUID> list;
            switch (listType) {
                case "LOCAL_MEMBERS": list = data.members; break;
                case "LOCAL_OWNERS": list = data.owners; break;
                case "GLOBAL_MEMBERS": list = getGlobalMembers(data.owner); break;
                case "GLOBAL_OWNERS": list = getGlobalOwners(data.owner); break;
                default: return;
            }

            List<UUID> sorted = new ArrayList<>(list);

            if (slot == 45) {
                openPlayerListView(player, data, listType, ctx.page - 1);
                return;
            }
            if (slot == 53) {
                openPlayerListView(player, data, listType, ctx.page + 1);
                return;
            }
            if (slot == 49) {
                openMembersMenu(player, data);
                return;
            }
            if (slot == 48) {
                player.closeInventory();
                player.sendMessage(translateHexColors("&#FFFF55Type player name in chat to add, or 'cancel' to abort."));
                chatSessions.put(player.getUniqueId(), new ChatInputSession("ADD_" + listType, data.key()));
                return;
            }

            int perPage = 45;
            int index = ctx.page * perPage + slot;
            if (slot >= 0 && slot < perPage && index < sorted.size()) {
                UUID removed = sorted.get(index);
                list.remove(removed);
                saveBlocks();
                openPlayerListView(player, data, listType, ctx.page);
            }
        }
    }

    private AccessLevel cycleTeleportAccess(AccessLevel current) {
        AccessLevel[] values = {AccessLevel.OWNER, AccessLevel.MEMBERS, AccessLevel.OWNERS, AccessLevel.ALL};
        int idx = Arrays.asList(values).indexOf(current);
        return values[(idx + 1) % values.length];
    }

    private AccessLevel cycleManageAccess(AccessLevel current) {
        AccessLevel[] values = {AccessLevel.OWNER, AccessLevel.OWNERS, AccessLevel.ALL};
        int idx = Arrays.asList(values).indexOf(current);
        return values[(idx + 1) % values.length];
    }

    private AccessLevel cycleBreakAccess(AccessLevel current) {
        AccessLevel[] values = {AccessLevel.OWNER, AccessLevel.MEMBERS, AccessLevel.OWNERS, AccessLevel.ALL};
        int idx = Arrays.asList(values).indexOf(current);
        return values[(idx + 1) % values.length];
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (openGuis.get(uuid) == event.getInventory()) {
            openGuis.remove(uuid);
            guiContexts.remove(uuid);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ChatInputSession session = chatSessions.get(player.getUniqueId());
        if (session == null) return;
        if (System.currentTimeMillis() > session.expireTime) {
            chatSessions.remove(player.getUniqueId());
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage().trim();
        chatSessions.remove(player.getUniqueId());

        if (message.equalsIgnoreCase("cancel")) {
            player.sendMessage(translateHexColors("&#FF5555Cancelled."));
            return;
        }

        BlockData data = blockDataMap.get(session.blockKey);
        if (data == null) {
            player.sendMessage(translateHexColors("&#FF5555Block no longer exists."));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                handleChatInput(player, data, session, message);
            }
        }.runTask(this);
    }

    private void handleChatInput(Player player, BlockData data, ChatInputSession session, String message) {
        switch (session.type) {
            case "SET_ID": {
                int newId;
                try {
                    newId = Integer.parseInt(message);
                } catch (NumberFormatException e) {
                    player.sendMessage(translateHexColors("&#FF5555Invalid number."));
                    return;
                }
                if (newId <= 0 || newId > 99999) {
                    player.sendMessage(translateHexColors("&#FF5555Number must be between 1 and 99999."));
                    return;
                }

                Set<String> existing = idIndex.get(newId);
                if (existing != null) {
                    existing.remove(data.key());
                    if (existing.size() >= 2) {
                        existing.add(data.key());
                        player.sendMessage(translateHexColors("&#FF5555This ID is already used by 2 blocks."));
                        return;
                    }
                }

                Set<String> oldKeys = idIndex.get(data.id);
                if (oldKeys != null) {
                    oldKeys.remove(data.key());
                    if (oldKeys.isEmpty()) idIndex.remove(data.id);
                }

                data.id = newId;
                idIndex.computeIfAbsent(newId, k -> ConcurrentHashMap.newKeySet()).add(data.key());
                saveBlocks();
                player.sendMessage(translateHexColors("&#55FF55ID set to " + newId));
                openMainMenu(player, data);
                return;
            }
            case "SET_ITEM": {
                if (message.equalsIgnoreCase("no") || message.equalsIgnoreCase("нет") || message.equalsIgnoreCase("not")) {
                    data.requireItem = false;
                    data.requiredItemName = "";
                    saveBlocks();
                    player.sendMessage(translateHexColors("&#55FF55Item requirement disabled."));
                    openMainMenu(player, data);
                    return;
                }
                data.requireItem = true;
                data.requiredItemName = message;
                saveBlocks();
                player.sendMessage(translateHexColors("&#55FF55Required item name set to: " + message));
                openMainMenu(player, data);
                return;
            }
            default: {
                if (session.type.startsWith("ADD_")) {
                    String listType = session.type.substring(4);
                    Player target = Bukkit.getPlayerExact(message);
                    if (target == null) {
                        player.sendMessage(translateHexColors("&#FF5555Player not found."));
                        return;
                    }
                    Set<UUID> list;
                    switch (listType) {
                        case "LOCAL_MEMBERS": list = data.members; break;
                        case "LOCAL_OWNERS": list = data.owners; break;
                        case "GLOBAL_MEMBERS": list = getGlobalMembers(data.owner); break;
                        case "GLOBAL_OWNERS": list = getGlobalOwners(data.owner); break;
                        default: return;
                    }
                    list.add(target.getUniqueId());
                    saveBlocks();
                    player.sendMessage(translateHexColors("&#55FF55Added " + target.getName()));
                    openPlayerListView(player, data, listType, 0);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        chatSessions.remove(uuid);
        openGuis.remove(uuid);
        guiContexts.remove(uuid);
        recentInteractions.remove(uuid);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("elevator")) return false;

        if (args.length == 0) {
            sender.sendMessage(translateHexColors("&#55FFFFElevator commands:"));
            sender.sendMessage(translateHexColors("&#FFFF55/elevator reload"));
            sender.sendMessage(translateHexColors("&#FFFF55/elevator info"));
            sender.sendMessage(translateHexColors("&#FFFF55/elevator list"));
            sender.sendMessage(translateHexColors("&#FFFF55/elevator remove <id>"));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("reload")) {
            if (!sender.hasPermission("elevator.reload")) {
                sender.sendMessage(translateHexColors("&#FF5555No permission!"));
                return true;
            }
            loadConfig();
            loadBlocks();
            sender.sendMessage(translateHexColors("&#55FF55Elevator config reloaded!"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(translateHexColors("&#FF5555Players only."));
            return true;
        }

        Player player = (Player) sender;

        if (sub.equals("info")) {
            Block block = player.getTargetBlockExact(10);
            if (block == null) {
                player.sendMessage(translateHexColors("&#FF5555No block in sight."));
                return true;
            }
            String key = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
            BlockData data = blockDataMap.get(key);
            if (data == null) {
                player.sendMessage(translateHexColors("&#FF5555This block is not a teleporter."));
                return true;
            }
            player.sendMessage(translateHexColors("&#55FFFFBlock info:"));
            player.sendMessage(translateHexColors("&#FFFF55ID: " + data.id));
            player.sendMessage(translateHexColors("&#FFFF55Owner: " + Bukkit.getOfflinePlayer(data.owner).getName()));
            player.sendMessage(translateHexColors("&#FFFF55Click: " + data.clickType.name()));
            player.sendMessage(translateHexColors("&#FFFF55Require sneak: " + data.requireSneak));
            player.sendMessage(translateHexColors("&#FFFF55Require item: " + data.requireItem + " (" + data.requiredItemName + ")"));
            player.sendMessage(translateHexColors("&#FFFF55Teleport access: " + data.teleportAccess.name()));
            player.sendMessage(translateHexColors("&#FFFF55Manage access: " + data.manageAccess.name()));
            player.sendMessage(translateHexColors("&#FFFF55Break access: " + data.breakAccess.name()));
            return true;
        }

        if (sub.equals("list")) {
            List<String> keys = playerBlocks.get(player.getUniqueId());
            if (keys == null || keys.isEmpty()) {
                player.sendMessage(translateHexColors("&#FF5555You have no teleporter blocks."));
                return true;
            }
            player.sendMessage(translateHexColors("&#55FFFFYour teleporter blocks:"));
            for (String k : keys) {
                BlockData data = blockDataMap.get(k);
                if (data == null) continue;
                player.sendMessage(translateHexColors("&#FFFF55ID " + data.id + " &7-> &f" + k));
            }
            return true;
        }

        if (sub.equals("remove")) {
            if (args.length < 2) {
                player.sendMessage(translateHexColors("&#FF5555Usage: /elevator remove <id>"));
                return true;
            }
            int id;
            try {
                id = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(translateHexColors("&#FF5555Invalid ID."));
                return true;
            }
            Set<String> keys = idIndex.get(id);
            if (keys == null || keys.isEmpty()) {
                player.sendMessage(translateHexColors("&#FF5555No blocks with that ID."));
                return true;
            }
            List<String> toRemove = new ArrayList<>();
            for (String k : keys) {
                BlockData data = blockDataMap.get(k);
                if (data != null && data.owner.equals(player.getUniqueId())) {
                    toRemove.add(k);
                }
            }
            if (toRemove.isEmpty()) {
                player.sendMessage(translateHexColors("&#FF5555You do not own any block with that ID."));
                return true;
            }
            for (String k : toRemove) {
                BlockData data = blockDataMap.get(k);
                if (data != null) removeBlock(data);
            }
            player.sendMessage(translateHexColors("&#55FF55Removed " + toRemove.size() + " block(s) with ID " + id));
            return true;
        }

        player.sendMessage(translateHexColors("&#FF5555Unknown subcommand."));
        return true;
    }
}
