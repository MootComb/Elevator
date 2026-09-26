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
    public enum TeleportLocation { TOP, CURRENT }

    private static final int DOUBLE_CLICK_DELAY_TICKS = 5;
    private static final int SNEAK_CHECK_DELAY_TICKS = 1;
    private static final int DEFAULT_END_ROD_COUNT = 5;
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final String PREFIX = "&#FF5300Elevator &7| &f";

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

    private boolean distanceCheckEnabled;
    private double maxDistance;

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

    private String guiMainTitle;
    private String guiMembersTitle;
    private String guiListTitle;

    private String msgBlockBound;
    private String msgIdPrompt;
    private String msgIdInvalid;
    private String msgIdRange;
    private String msgIdUsed;
    private String msgIdSet;
    private String msgItemPrompt;
    private String msgItemDisabled;
    private String msgItemSet;
    private String msgCancelled;
    private String msgNoPermission;
    private String msgBlockGone;
    private String msgPlayerNotFound;
    private String msgPlayerAdded;
    private String msgTeleportSuccess;
    private String msgNoPair;
    private String msgNoCrossWorld;
    private String msgNoBlockInSight;
    private String msgNotTeleporter;
    private String msgNoBlocksOwned;
    private String msgYourBlocks;
    private String msgRemoveUsage;
    private String msgRemoveInvalid;
    private String msgRemoveNone;
    private String msgRemoveNotOwner;
    private String msgRemoveSuccess;
    private String msgUnknownSub;
    private String msgPlayersOnly;
    private String msgReloaded;
    private String msgListPrompt;
    private String msgHeaderInfo;

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
        public TeleportLocation teleportLocation = TeleportLocation.TOP;
        public String customDestinationWorld = "";
        public double customDestinationX;
        public double customDestinationY;
        public double customDestinationZ;
        public float customDestinationYaw;
        public float customDestinationPitch;
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

    private String color(String message) {
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

    private String prefixed(String message) {
        return color(PREFIX + message);
    }

    private String primary(String message) {
        return color("&#FF5300" + message);
    }

    private void debug(String message) {
        if (debug) getLogger().info("[DEBUG] " + message);
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
        distanceCheckEnabled = getConfig().getBoolean("Teleporter.DistanceCheck.Enabled", true);
        maxDistance = getConfig().getDouble("Teleporter.DistanceCheck.MaxDistance", 10000.0);

        teleporterBlockTypes.clear();
        List<String> teleporterBlockNames = getConfig().getStringList("Teleporter.BlockTypes");
        if (teleporterBlockNames.isEmpty()) teleporterBlockNames.add("SEA_LANTERN");
        for (String blockName : teleporterBlockNames) {
            Material mat = Material.getMaterial(blockName);
            if (mat != null) teleporterBlockTypes.add(mat);
            else getLogger().warning("Unknown teleporter block: " + blockName);
        }

        enableCooldown = getConfig().getBoolean("Cooldown.EnableCooldown", false);
        cooldownTime = getConfig().getInt("Cooldown.Time", 30);
        cooldownLocale = getConfig().getString("Cooldown.Locale", "&#FF5300Elevator &7| &fCooldown: %time%s");
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

        elevatorUpMessage = getConfig().getString("ElevatorLocale.ElevatorUp", "&#FF5300Elevator &7| &fGoing up");
        elevatorDownMessage = getConfig().getString("ElevatorLocale.ElevatorDown", "&#FF5300Elevator &7| &fGoing down");
        elevatorDangerMessage = getConfig().getString("ElevatorLocale.ElevatorDanger", "&#FF5300Elevator &7| &fDanger! Unsafe location!");

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

        guiMainTitle = getConfig().getString("GUI.Main.Title", "&7Teleporter Block");
        guiMembersTitle = getConfig().getString("GUI.Members.Title", "&7Members & Owners");
        guiListTitle = getConfig().getString("GUI.List.Title", "&7%list%");

        msgBlockBound = getConfig().getString("Messages.BlockBound", "&#FF5300Elevator &7| &fBlock bound! Set an ID to link it.");
        msgIdPrompt = getConfig().getString("Messages.IdPrompt", "&#FF5300Elevator &7| &fType a number (1-99999) or 'cancel'.");
        msgIdInvalid = getConfig().getString("Messages.IdInvalid", "&#FF5300Elevator &7| &fInvalid number.");
        msgIdRange = getConfig().getString("Messages.IdRange", "&#FF5300Elevator &7| &fNumber must be 1-99999.");
        msgIdUsed = getConfig().getString("Messages.IdUsed", "&#FF5300Elevator &7| &fThis ID is already used by 2 blocks.");
        msgIdSet = getConfig().getString("Messages.IdSet", "&#FF5300Elevator &7| &fID set to %id%");
        msgItemPrompt = getConfig().getString("Messages.ItemPrompt", "&#FF5300Elevator &7| &fType item name or 'no' to disable.");
        msgItemDisabled = getConfig().getString("Messages.ItemDisabled", "&#FF5300Elevator &7| &fItem requirement disabled.");
        msgItemSet = getConfig().getString("Messages.ItemSet", "&#FF5300Elevator &7| &fRequired item: %name%");
        msgCancelled = getConfig().getString("Messages.Cancelled", "&#FF5300Elevator &7| &fCancelled.");
        msgNoPermission = getConfig().getString("Messages.NoPermission", "&#FF5300Elevator &7| &fNo permission.");
        msgBlockGone = getConfig().getString("Messages.BlockGone", "&#FF5300Elevator &7| &fBlock no longer exists.");
        msgPlayerNotFound = getConfig().getString("Messages.PlayerNotFound", "&#FF5300Elevator &7| &fPlayer not found.");
        msgPlayerAdded = getConfig().getString("Messages.PlayerAdded", "&#FF5300Elevator &7| &fAdded %name%");
        msgTeleportSuccess = getConfig().getString("Messages.TeleportSuccess", "&#FF5300Elevator &7| &fTeleported!");
        msgNoPair = getConfig().getString("Messages.NoPair", "&#FF5300Elevator &7| &fNo paired block found.");
        msgNoCrossWorld = getConfig().getString("Messages.NoCrossWorld", "&#FF5300Elevator &7| &fCannot teleport across worlds.");
        msgNoBlockInSight = getConfig().getString("Messages.NoBlockInSight", "&#FF5300Elevator &7| &fNo block in sight.");
        msgNotTeleporter = getConfig().getString("Messages.NotTeleporter", "&#FF5300Elevator &7| &fThis block is not a teleporter.");
        msgNoBlocksOwned = getConfig().getString("Messages.NoBlocksOwned", "&#FF5300Elevator &7| &fYou have no teleporter blocks.");
        msgYourBlocks = getConfig().getString("Messages.YourBlocks", "&#FF5300Elevator &7| &fYour teleporter blocks:");
        msgRemoveUsage = getConfig().getString("Messages.RemoveUsage", "&#FF5300Elevator &7| &fUsage: /elevator remove <id>");
        msgRemoveInvalid = getConfig().getString("Messages.RemoveInvalid", "&#FF5300Elevator &7| &fInvalid ID.");
        msgRemoveNone = getConfig().getString("Messages.RemoveNone", "&#FF5300Elevator &7| &fNo blocks with that ID.");
        msgRemoveNotOwner = getConfig().getString("Messages.RemoveNotOwner", "&#FF5300Elevator &7| &fYou do not own any block with that ID.");
        msgRemoveSuccess = getConfig().getString("Messages.RemoveSuccess", "&#FF5300Elevator &7| &fRemoved %count% block(s) with ID %id%");
        msgUnknownSub = getConfig().getString("Messages.UnknownSub", "&#FF5300Elevator &7| &fUnknown subcommand.");
        msgPlayersOnly = getConfig().getString("Messages.PlayersOnly", "&#FF5300Elevator &7| &fPlayers only.");
        msgReloaded = getConfig().getString("Messages.Reloaded", "&#FF5300Elevator &7| &fConfig reloaded!");
        msgListPrompt = getConfig().getString("Messages.ListPrompt", "&#FF5300Elevator &7| &fType player name or 'cancel'.");
        msgHeaderInfo = getConfig().getString("Messages.HeaderInfo", "&#FF5300Elevator &7| &fBlock info:");

        debug = getConfig().getBoolean("Debug", false);
    }

    private MessageType getMessageType(String type) {
        try { return MessageType.valueOf(type.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { return MessageType.CHAT; }
    }

    private AccessLevel getAccessLevel(String level) {
        try { return AccessLevel.valueOf(level.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { return AccessLevel.ALL; }
    }

    private ClickType getClickType(String type) {
        try { return ClickType.valueOf(type.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { return ClickType.RIGHT; }
    }

    private TeleportLocation getTeleportLocation(String type) {
        try { return TeleportLocation.valueOf(type.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { return TeleportLocation.TOP; }
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
                try { data.owner = UUID.fromString(ownerStr); }
                catch (IllegalArgumentException e) { continue; }
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
                data.teleportLocation = getTeleportLocation(sec.getString("TeleportLocation", "TOP"));
                data.customDestinationWorld = sec.getString("CustomDestination.World", "");
                data.customDestinationX = sec.getDouble("CustomDestination.X", 0);
                data.customDestinationY = sec.getDouble("CustomDestination.Y", 0);
                data.customDestinationZ = sec.getDouble("CustomDestination.Z", 0);
                data.customDestinationYaw = (float) sec.getDouble("CustomDestination.Yaw", 0);
                data.customDestinationPitch = (float) sec.getDouble("CustomDestination.Pitch", 0);

                for (String member : sec.getStringList("Members")) {
                    try { data.members.add(UUID.fromString(member)); } catch (IllegalArgumentException ignored) {}
                }
                for (String owner : sec.getStringList("Owners")) {
                    try { data.owners.add(UUID.fromString(owner)); } catch (IllegalArgumentException ignored) {}
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
                try { uuid = UUID.fromString(uuidStr); } catch (IllegalArgumentException e) { continue; }
                ConfigurationSection pSec = playersSection.getConfigurationSection(uuidStr);
                if (pSec == null) continue;
                Set<UUID> members = ConcurrentHashMap.newKeySet();
                for (String m : pSec.getStringList("GlobalMembers")) {
                    try { members.add(UUID.fromString(m)); } catch (IllegalArgumentException ignored) {}
                }
                Set<UUID> owners = ConcurrentHashMap.newKeySet();
                for (String o : pSec.getStringList("GlobalOwners")) {
                    try { owners.add(UUID.fromString(o)); } catch (IllegalArgumentException ignored) {}
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
            String path = "Blocks." + entry.getKey().replace(".", "_").replace(":", "_");
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
            blocksConfig.set(path + ".TeleportLocation", data.teleportLocation.name());
            blocksConfig.set(path + ".CustomDestination.World", data.customDestinationWorld);
            blocksConfig.set(path + ".CustomDestination.X", data.customDestinationX);
            blocksConfig.set(path + ".CustomDestination.Y", data.customDestinationY);
            blocksConfig.set(path + ".CustomDestination.Z", data.customDestinationZ);
            blocksConfig.set(path + ".CustomDestination.Yaw", data.customDestinationYaw);
            blocksConfig.set(path + ".CustomDestination.Pitch", data.customDestinationPitch);

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

        try { blocksConfig.save(blocksFile); }
        catch (IOException e) { getLogger().severe("Could not save blocks.yml: " + e.getMessage()); }
    }

    private void sendMessage(Player player, String message, MessageType type) {
        String formatted = color(message);
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
        String formatted = color(message);
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
                event.setCancelled(true);
                tryBindBlock(player, block, blockType);
            }
            return;
        }

        if (isRight && player.isSneaking()) {
            event.setCancelled(true);
            if (holdingEnderPearl) {
                debug("Player " + player.getName() + " sneaking with pearl on bound block");
            }
            if (!featureManageEnabled) { debug("Manage disabled"); return; }
            if (!canManage(player, data)) { debug("No manage rights for " + player.getName()); return; }
            openMainMenu(player, data);
            return;
        }

        if (!featureTeleportEnabled) { debug("Teleport disabled"); return; }
        if (!canTeleport(player, data)) { debug("No teleport rights for " + player.getName()); return; }

        ClickType clicked = isRight ? ClickType.RIGHT : ClickType.LEFT;
        if (data.clickType != clicked) return;
        if (data.requireSneak && !player.isSneaking()) return;
        if (!data.requireSneak && player.isSneaking()) return;

        if (data.requireItem) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!itemMatches(item, data.requiredItemName)) { debug("Missing item"); return; }
        }

        if (recentInteractions.contains(player.getUniqueId())) return;
        recentInteractions.add(player.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() { recentInteractions.remove(player.getUniqueId()); }
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
        String required = ChatColor.stripColor(color(requiredName));
        return display.equalsIgnoreCase(required);
    }

    private void tryBindBlock(Player player, Block block, Material blockType) {
        if (isInDisabledWorld(player)) { debug("Disabled world"); return; }

        if (!teleporterAllowAllBlocks) {
            if (!teleporterBlockTypes.contains(blockType)) {
                boolean allowedByPerm = false;
                if (teleporterBlockTypesPermission != null && !teleporterBlockTypesPermission.isEmpty()) {
                    allowedByPerm = player.hasPermission(teleporterBlockTypesPermission);
                }
                if (!allowedByPerm) { debug("Block type not allowed"); return; }
            }
        } else {
            if (teleporterBlockTypesPermission != null && !teleporterBlockTypesPermission.isEmpty()
                    && !player.hasPermission(teleporterBlockTypesPermission)) {
                debug("Missing bind permission"); return;
            }
        }

        if (!hasPermission(player, permTeleport)) { debug("Missing teleport permission"); return; }

        String key = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
        if (blockDataMap.containsKey(key)) { debug("Already bound"); return; }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() != Material.ENDER_PEARL) return;

        if (hand.getAmount() > 1) hand.setAmount(hand.getAmount() - 1);
        else player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

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
        data.teleportLocation = TeleportLocation.TOP;
        data.id = 0;

        blockDataMap.put(key, data);
        playerBlocks.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(key);
        saveBlocks();

        playEffects(block.getLocation(), activateSound, true);
        sendTeleporterMessage(player, msgBlockBound, teleporterMessageType);
        openMainMenu(player, data);
    }

    private void performTeleport(Player player, BlockData source) {
        int id = source.id;
        if (id <= 0) { debug("No ID"); return; }
        Set<String> keys = idIndex.get(id);
        if (keys == null || keys.size() < 2) { debug("No pair"); sendTeleporterMessage(player, msgNoPair, teleporterMessageType); return; }

        BlockData destination = null;
        for (String k : keys) {
            if (!k.equals(source.key())) { destination = blockDataMap.get(k); break; }
        }
        if (destination == null) { sendTeleporterMessage(player, msgNoPair, teleporterMessageType); return; }

        World destWorld = Bukkit.getWorld(destination.world);
        if (destWorld == null) { debug("Dest world missing"); return; }

        if (!allowCrossWorlds && !player.getWorld().getName().equals(destination.world)) {
            sendTeleporterMessage(player, msgNoCrossWorld, teleporterMessageType);
            return;
        }

        if (distanceCheckEnabled) {
            Location a = new Location(player.getWorld(), source.x, source.y, source.z);
            Location b = new Location(destWorld, destination.x, destination.y, destination.z);
            if (a.getWorld() != null && a.getWorld().equals(b.getWorld())) {
                double dist = a.distance(b);
                if (dist > maxDistance) {
                    debug("Distance " + dist + " exceeds max " + maxDistance);
                    return;
                }
            }
        }

        Location destLoc;
        if (destination.teleportLocation == TeleportLocation.CURRENT && !destination.customDestinationWorld.isEmpty()) {
            World w = Bukkit.getWorld(destination.customDestinationWorld);
            if (w == null) w = destWorld;
            destLoc = new Location(w, destination.customDestinationX, destination.customDestinationY,
                    destination.customDestinationZ, destination.customDestinationYaw, destination.customDestinationPitch);
        } else {
            destLoc = new Location(destWorld,
                    destination.x + 0.5,
                    destination.y + 1,
                    destination.z + 0.5,
                    player.getLocation().getYaw(),
                    player.getLocation().getPitch());
        }

        if (!isSafeLocation(destLoc, true)) {
            sendTeleporterMessage(player, elevatorDangerMessage, teleporterMessageType);
            return;
        }

        Location sourceLoc = player.getLocation().clone();
        playEffects(sourceLoc, teleporterUsageSound, true);
        playEffects(destLoc, teleporterUsageSound, true);
        player.teleport(destLoc);
        playEffects(destLoc, teleporterUsageSound, true);
        sendTeleporterMessage(player, msgTeleportSuccess, teleporterMessageType);
        setCooldown(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String key = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
        BlockData data = blockDataMap.get(key);
        if (data == null) return;
        if (!featureBreakEnabled) { event.setCancelled(true); debug("Break disabled"); return; }
        if (checkPermission && permBreakBypass != null && !permBreakBypass.isEmpty()
                && player.hasPermission(permBreakBypass)) {
            removeBlock(data);
            return;
        }
        if (!canBreak(player, data)) {
            event.setCancelled(true);
            debug("No break rights");
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
        if (featureTeleportCheckPermission && !hasPermission(player, featureTeleportPermission)) return false;
        AccessLevel level = data.teleportAccess;
        if (level == AccessLevel.ALL) return true;
        if (level == AccessLevel.OWNER) return player.getUniqueId().equals(data.owner);
        if (level == AccessLevel.MEMBERS) {
            if (player.getUniqueId().equals(data.owner)) return true;
            if (data.members.contains(player.getUniqueId())) return true;
            Set<UUID> global = globalMembers.get(data.owner);
            return global != null && global.contains(player.getUniqueId());
        }
        if (level == AccessLevel.OWNERS) {
            if (player.getUniqueId().equals(data.owner)) return true;
            if (data.owners.contains(player.getUniqueId())) return true;
            Set<UUID> global = globalOwners.get(data.owner);
            return global != null && global.contains(player.getUniqueId());
        }
        return false;
    }

    private boolean canManage(Player player, BlockData data) {
        if (featureManageCheckPermission && !hasPermission(player, featureManagePermission)) return false;
        AccessLevel level = data.manageAccess;
        if (level == AccessLevel.ALL) return true;
        if (level == AccessLevel.OWNER) return player.getUniqueId().equals(data.owner);
        if (level == AccessLevel.OWNERS) {
            if (player.getUniqueId().equals(data.owner)) return true;
            if (data.owners.contains(player.getUniqueId())) return true;
            Set<UUID> global = globalOwners.get(data.owner);
            return global != null && global.contains(player.getUniqueId());
        }
        return false;
    }

    private boolean canBreak(Player player, BlockData data) {
        if (featureBreakCheckPermission && !hasPermission(player, featureBreakPermission)) return false;
        AccessLevel level = data.breakAccess;
        if (level == AccessLevel.ALL) return true;
        if (level == AccessLevel.OWNER) return player.getUniqueId().equals(data.owner);
        if (level == AccessLevel.MEMBERS) {
            if (player.getUniqueId().equals(data.owner)) return true;
            if (data.members.contains(player.getUniqueId())) return true;
            Set<UUID> global = globalMembers.get(data.owner);
            return global != null && global.contains(player.getUniqueId());
        }
        if (level == AccessLevel.OWNERS) {
            if (player.getUniqueId().equals(data.owner)) return true;
            if (data.owners.contains(player.getUniqueId())) return true;
            Set<UUID> global = globalOwners.get(data.owner);
            return global != null && global.contains(player.getUniqueId());
        }
        return false;
    }

    private void openMainMenu(Player player, BlockData data) {
        Inventory inv = Bukkit.createInventory(null, 27, color(guiMainTitle));

        inv.setItem(4, createItem(Material.ENDER_PEARL,
                "&#FF5300ID: " + (data.id > 0 ? data.id : "Not set"),
                "&7Click to change ID"));

        inv.setItem(10, createItem(Material.LEVER,
                "&#FF5300Click Type: " + data.clickType.name(),
                "&7Left or Right mouse button"));

        inv.setItem(11, createItem(Material.SHIELD,
                "&#FF5300Require Sneak: " + (data.requireSneak ? "Yes" : "No"),
                "&7Toggle sneak requirement"));

        inv.setItem(12, createItem(Material.NAME_TAG,
                "&#FF5300Require Item: " + (data.requireItem ? "Yes" : "No"),
                "&7Required: " + (data.requiredItemName.isEmpty() ? "-" : data.requiredItemName),
                "&7Click to change"));

        inv.setItem(13, createItem(Material.ENDER_EYE,
                "&#FF5300Teleport Location: " + data.teleportLocation.name(),
                "&7TOP or CURRENT",
                "&7Click to cycle"));

        inv.setItem(14, createItem(Material.PLAYER_HEAD,
                "&#FF5300Teleport Access: " + data.teleportAccess.name(),
                "&7Click to cycle",
                "&7OWNER / MEMBERS / OWNERS / ALL"));

        inv.setItem(15, createItem(Material.COMMAND_BLOCK,
                "&#FF5300Manage Access: " + data.manageAccess.name(),
                "&7Click to cycle",
                "&7OWNER / OWNERS / ALL"));

        inv.setItem(16, createItem(Material.DIAMOND_PICKAXE,
                "&#FF5300Break Access: " + data.breakAccess.name(),
                "&7Click to cycle",
                "&7OWNER / MEMBERS / OWNERS / ALL"));

        inv.setItem(22, createItem(Material.BOOK,
                "&#FF5300Members & Owners",
                "&7Manage local and global lists"));

        inv.setItem(26, createItem(Material.BARRIER, "&#FF5300Close"));

        player.openInventory(inv);
        openGuis.put(player.getUniqueId(), inv);
        guiContexts.put(player.getUniqueId(), new GuiContext(data.key(), "MAIN"));
    }

    private void openMembersMenu(Player player, BlockData data) {
        Inventory inv = Bukkit.createInventory(null, 54, color(guiMembersTitle));

        inv.setItem(10, createItem(Material.PLAYER_HEAD,
                "&#FF5300Local Members: " + data.members.size(),
                "&7Click to manage"));

        inv.setItem(11, createItem(Material.PLAYER_HEAD,
                "&#FF5300Local Owners: " + data.owners.size(),
                "&7Click to manage"));

        inv.setItem(12, createItem(Material.PLAYER_HEAD,
                "&#FF5300Global Members: " + getGlobalMembers(data.owner).size(),
                "&7Click to manage"));

        inv.setItem(13, createItem(Material.PLAYER_HEAD,
                "&#FF5300Global Owners: " + getGlobalOwners(data.owner).size(),
                "&7Click to manage"));

        inv.setItem(49, createItem(Material.ARROW, "&#FF5300Back"));

        player.openInventory(inv);
        openGuis.put(player.getUniqueId(), inv);
        guiContexts.put(player.getUniqueId(), new GuiContext(data.key(), "MEMBERS"));
    }

    private void openPlayerListView(Player player, BlockData data, String listType, int page) {
        Set<UUID> list;
        String title;
        switch (listType) {
            case "LOCAL_MEMBERS": list = data.members; title = "Local Members"; break;
            case "LOCAL_OWNERS": list = data.owners; title = "Local Owners"; break;
            case "GLOBAL_MEMBERS": list = getGlobalMembers(data.owner); title = "Global Members"; break;
            case "GLOBAL_OWNERS": list = getGlobalOwners(data.owner); title = "Global Owners"; break;
            default: return;
        }

        List<UUID> sorted = new ArrayList<>(list);
        int perPage = 45;
        int totalPages = Math.max(1, (int) Math.ceil(sorted.size() / (double) perPage));
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        String t = guiListTitle.replace("%list%", title + " (" + (page + 1) + "/" + totalPages + ")");
        Inventory inv = Bukkit.createInventory(null, 54, color(t));

        int start = page * perPage;
        int end = Math.min(start + perPage, sorted.size());
        for (int i = start; i < end; i++) {
            UUID uuid = sorted.get(i);
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            if (name == null) name = uuid.toString();
            inv.setItem(i - start, createItem(Material.PLAYER_HEAD,
                    "&#FF5300" + name,
                    "&7Click to remove"));
        }

        inv.setItem(45, createItem(Material.ARROW, "&#FF5300Previous"));
        inv.setItem(49, createItem(Material.ARROW, "&#FF5300Back"));
        inv.setItem(53, createItem(Material.ARROW, "&#FF5300Next"));
        inv.setItem(48, createItem(Material.EMERALD, "&#FF5300Add player"));

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
            meta.setDisplayName(color(name));
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String l : lore) loreList.add(color(l));
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
        if (data == null) { player.closeInventory(); return; }

        int slot = event.getRawSlot();
        String guiType = ctx.guiType;

        if (guiType.equals("MAIN")) {
            switch (slot) {
                case 4: {
                    player.closeInventory();
                    player.sendMessage(color(msgIdPrompt));
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
                    player.sendMessage(color(msgItemPrompt));
                    chatSessions.put(player.getUniqueId(), new ChatInputSession("SET_ITEM", data.key()));
                    return;
                }
                case 13: {
                    data.teleportLocation = data.teleportLocation == TeleportLocation.TOP
                            ? TeleportLocation.CURRENT : TeleportLocation.TOP;
                    if (data.teleportLocation == TeleportLocation.CURRENT) {
                        Location loc = player.getLocation();
                        Block block = player.getWorld().getBlockAt(data.x, data.y, data.z);
                        Location blockLoc = block.getLocation();
                        if (loc.distance(blockLoc) <= 5.0) {
                            data.customDestinationWorld = loc.getWorld().getName();
                            data.customDestinationX = loc.getX();
                            data.customDestinationY = loc.getY();
                            data.customDestinationZ = loc.getZ();
                            data.customDestinationYaw = loc.getYaw();
                            data.customDestinationPitch = loc.getPitch();
                        } else {
                            data.customDestinationWorld = "";
                        }
                    }
                    saveBlocks();
                    openMainMenu(player, data);
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
                default: return;
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

            if (slot == 45) { openPlayerListView(player, data, listType, ctx.page - 1); return; }
            if (slot == 53) { openPlayerListView(player, data, listType, ctx.page + 1); return; }
            if (slot == 49) { openMembersMenu(player, data); return; }
            if (slot == 48) {
                player.closeInventory();
                player.sendMessage(color(msgListPrompt));
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
            player.sendMessage(color(msgCancelled));
            return;
        }

        BlockData data = blockDataMap.get(session.blockKey);
        if (data == null) { player.sendMessage(color(msgBlockGone)); return; }

        new BukkitRunnable() {
            @Override
            public void run() { handleChatInput(player, data, session, message); }
        }.runTask(this);
    }

    private void handleChatInput(Player player, BlockData data, ChatInputSession session, String message) {
        switch (session.type) {
            case "SET_ID": {
                int newId;
                try { newId = Integer.parseInt(message); }
                catch (NumberFormatException e) { player.sendMessage(color(msgIdInvalid)); return; }
                if (newId <= 0 || newId > 99999) { player.sendMessage(color(msgIdRange)); return; }

                Set<String> existing = idIndex.get(newId);
                if (existing != null) {
                    existing.remove(data.key());
                    if (existing.size() >= 2) {
                        existing.add(data.key());
                        player.sendMessage(color(msgIdUsed));
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
                player.sendMessage(color(msgIdSet.replace("%id%", String.valueOf(newId))));
                openMainMenu(player, data);
                return;
            }
            case "SET_ITEM": {
                if (message.equalsIgnoreCase("no") || message.equalsIgnoreCase("not")) {
                    data.requireItem = false;
                    data.requiredItemName = "";
                    saveBlocks();
                    player.sendMessage(color(msgItemDisabled));
                    openMainMenu(player, data);
                    return;
                }
                data.requireItem = true;
                data.requiredItemName = message;
                saveBlocks();
                player.sendMessage(color(msgItemSet.replace("%name%", message)));
                openMainMenu(player, data);
                return;
            }
            default: {
                if (session.type.startsWith("ADD_")) {
                    String listType = session.type.substring(4);
                    Player target = Bukkit.getPlayerExact(message);
                    if (target == null) { player.sendMessage(color(msgPlayerNotFound)); return; }
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
                    player.sendMessage(color(msgPlayerAdded.replace("%name%", target.getName())));
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
            sender.sendMessage(color("&#FF5300Elevator &7| &fCommands:"));
            sender.sendMessage(color("&#FF5300/elevator reload"));
            sender.sendMessage(color("&#FF5300/elevator info"));
            sender.sendMessage(color("&#FF5300/elevator list"));
            sender.sendMessage(color("&#FF5300/elevator remove <id>"));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("reload")) {
            if (!sender.hasPermission("elevator.reload")) { sender.sendMessage(color(msgNoPermission)); return true; }
            loadConfig();
            loadBlocks();
            sender.sendMessage(color(msgReloaded));
            return true;
        }

        if (!(sender instanceof Player)) { sender.sendMessage(color(msgPlayersOnly)); return true; }
        Player player = (Player) sender;

        if (sub.equals("info")) {
            Block block = player.getTargetBlockExact(10);
            if (block == null) { player.sendMessage(color(msgNoBlockInSight)); return true; }
            String key = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
            BlockData data = blockDataMap.get(key);
            if (data == null) { player.sendMessage(color(msgNotTeleporter)); return true; }
            player.sendMessage(color(msgHeaderInfo));
            player.sendMessage(color("&#FF5300ID: &f" + data.id));
            player.sendMessage(color("&#FF5300Owner: &f" + Bukkit.getOfflinePlayer(data.owner).getName()));
            player.sendMessage(color("&#FF5300Click: &f" + data.clickType.name()));
            player.sendMessage(color("&#FF5300Require sneak: &f" + data.requireSneak));
            player.sendMessage(color("&#FF5300Require item: &f" + data.requireItem + " (" + data.requiredItemName + ")"));
            player.sendMessage(color("&#FF5300Location: &f" + data.teleportLocation.name()));
            player.sendMessage(color("&#FF5300Teleport access: &f" + data.teleportAccess.name()));
            player.sendMessage(color("&#FF5300Manage access: &f" + data.manageAccess.name()));
            player.sendMessage(color("&#FF5300Break access: &f" + data.breakAccess.name()));
            return true;
        }

        if (sub.equals("list")) {
            List<String> keys = playerBlocks.get(player.getUniqueId());
            if (keys == null || keys.isEmpty()) { player.sendMessage(color(msgNoBlocksOwned)); return true; }
            player.sendMessage(color(msgYourBlocks));
            for (String k : keys) {
                BlockData data = blockDataMap.get(k);
                if (data == null) continue;
                player.sendMessage(color("&#FF5300ID " + data.id + " &7-> &f" + k));
            }
            return true;
        }

        if (sub.equals("remove")) {
            if (args.length < 2) { player.sendMessage(color(msgRemoveUsage)); return true; }
            int id;
            try { id = Integer.parseInt(args[1]); }
            catch (NumberFormatException e) { player.sendMessage(color(msgRemoveInvalid)); return true; }
            Set<String> keys = idIndex.get(id);
            if (keys == null || keys.isEmpty()) { player.sendMessage(color(msgRemoveNone)); return true; }
            List<String> toRemove = new ArrayList<>();
            for (String k : keys) {
                BlockData data = blockDataMap.get(k);
                if (data != null && data.owner.equals(player.getUniqueId())) toRemove.add(k);
            }
            if (toRemove.isEmpty()) { player.sendMessage(color(msgRemoveNotOwner)); return true; }
            for (String k : toRemove) {
                BlockData data = blockDataMap.get(k);
                if (data != null) removeBlock(data);
            }
            player.sendMessage(color(msgRemoveSuccess.replace("%count%", String.valueOf(toRemove.size())).replace("%id%", String.valueOf(id))));
            return true;
        }

        player.sendMessage(color(msgUnknownSub));
        return true;
    }
}
