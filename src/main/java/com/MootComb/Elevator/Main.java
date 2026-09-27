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
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
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
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;
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
    public enum MessageType { CHAT, TITLE, SUBTITLE, NONE }
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
    private boolean teleporterBindConsumePearl;
    private boolean enableCooldown;
    private int cooldownTime;
    private String cooldownLocale;
    private MessageType cooldownMessageType;
    private MessageType elevatorMessageType;
    private int elevatorTitleFadeIn;
    private int elevatorTitleStay;
    private int elevatorTitleFadeOut;
    private String elevatorUpMessage;
    private MessageType elevatorUpType;
    private String elevatorDownMessage;
    private MessageType elevatorDownType;
    private String elevatorDangerMessage;
    private MessageType elevatorDangerType;
    private MessageType teleporterMessageType;
    private int teleporterTitleFadeIn;
    private int teleporterTitleStay;
    private int teleporterTitleFadeOut;
    private String teleporterSuccessMessage;
    private MessageType teleporterSuccessType;
    private String teleporterBlockBoundMessage;
    private MessageType teleporterBlockBoundType;
    private String teleporterNoPairMessage;
    private MessageType teleporterNoPairType;
    private String teleporterNoCrossWorldMessage;
    private MessageType teleporterNoCrossWorldType;
    private String teleporterMissingItemMessage;
    private MessageType teleporterMissingItemType;
    private String teleporterDistanceTooFarMessage;
    private MessageType teleporterDistanceTooFarType;
    private boolean checkPermission;
    private String permUse;
    private String permBypass;
    private String permTeleport;
    private String permManage;
    private String permBreakBypass;
    private String permBypassAccess;
    private String permBypassDistance;
    private String permBypassItem;
    private String permBypassSneak;
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
    private int viewDurationTicks;
    private int viewUpdateTicks;
    private int viewMaxDistance;
    private int viewParticleCount;
    private Particle viewParticleType;
    private String viewPermAll;
    private String viewPermOwner;
    private String viewPermMember;
    private boolean hologramsDefaultEnabled;
    private String hologramsDefaultColor;
    private int hologramsViewDistance;
    private int hologramsUpdateInterval;
    private boolean hologramsShowId;
    private boolean hologramsShowName;
    private String hologramsFormat;
    private String hologramsFormatNoName;
    private double hologramsLineHeight;
    private boolean blockNamingEnabled;
    private int blockNamingMaxLength;
    private String blockNamingDefaultName;
    private String guiMainTitle;
    private String guiMainIdItem;
    private String guiMainIdItemEmpty;
    private String guiMainIdItemLore;
    private String guiMainClickItem;
    private String guiMainClickItemLore;
    private String guiMainSneakItem;
    private String guiMainSneakItemLore;
    private String guiMainRequiredItem;
    private String guiMainRequiredItemLore;
    private String guiMainRequiredItemLoreEmpty;
    private String guiMainRequiredItemLoreClick;
    private String guiMainLocationItem;
    private String guiMainLocationItemLore;
    private String guiMainLocationItemLoreClick;
    private String guiMainNameItem;
    private String guiMainNameItemEmpty;
    private String guiMainNameItemLore;
    private String guiMainHologramItem;
    private String guiMainHologramItemLore;
    private String guiMainHologramColorItem;
    private String guiMainHologramColorItemLore;
    private String guiMainTeleportAccessItem;
    private String guiMainTeleportAccessItemLore;
    private String guiMainTeleportAccessItemLore2;
    private String guiMainManageAccessItem;
    private String guiMainManageAccessItemLore;
    private String guiMainManageAccessItemLore2;
    private String guiMainBreakAccessItem;
    private String guiMainBreakAccessItemLore;
    private String guiMainBreakAccessItemLore2;
    private String guiMainMembersItem;
    private String guiMainMembersItemLore;
    private String guiMainCloseItem;
    private String guiMembersTitle;
    private String guiMembersLocalMembers;
    private String guiMembersLocalOwners;
    private String guiMembersGlobalMembers;
    private String guiMembersGlobalOwners;
    private String guiMembersClickManage;
    private String guiMembersBack;
    private String guiListTitle;
    private String guiListPlayerItem;
    private String guiListPlayerItemLore;
    private String guiListPrevious;
    private String guiListNext;
    private String guiListBack;
    private String guiListAddPlayer;
    private String msgIdPrompt;
    private MessageType msgIdPromptType;
    private String msgIdInvalid;
    private MessageType msgIdInvalidType;
    private String msgIdRange;
    private MessageType msgIdRangeType;
    private String msgIdUsed;
    private MessageType msgIdUsedType;
    private String msgIdSet;
    private MessageType msgIdSetType;
    private String msgItemPrompt;
    private MessageType msgItemPromptType;
    private String msgItemDisabled;
    private MessageType msgItemDisabledType;
    private String msgItemSet;
    private MessageType msgItemSetType;
    private String msgNamePrompt;
    private MessageType msgNamePromptType;
    private String msgNameSet;
    private MessageType msgNameSetType;
    private String msgColorPrompt;
    private MessageType msgColorPromptType;
    private String msgColorSet;
    private MessageType msgColorSetType;
    private String msgColorInvalid;
    private MessageType msgColorInvalidType;
    private String msgCancelled;
    private MessageType msgCancelledType;
    private String msgNoPermission;
    private MessageType msgNoPermissionType;
    private String msgBlockGone;
    private MessageType msgBlockGoneType;
    private String msgPlayerNotFound;
    private MessageType msgPlayerNotFoundType;
    private String msgPlayerAdded;
    private MessageType msgPlayerAddedType;
    private String msgNoBlockInSight;
    private MessageType msgNoBlockInSightType;
    private String msgNotTeleporter;
    private MessageType msgNotTeleporterType;
    private String msgNoBlocksOwned;
    private MessageType msgNoBlocksOwnedType;
    private String msgYourBlocks;
    private MessageType msgYourBlocksType;
    private String msgRemoveUsage;
    private MessageType msgRemoveUsageType;
    private String msgRemoveInvalid;
    private MessageType msgRemoveInvalidType;
    private String msgRemoveNone;
    private MessageType msgRemoveNoneType;
    private String msgRemoveNotOwner;
    private MessageType msgRemoveNotOwnerType;
    private String msgRemoveSuccess;
    private MessageType msgRemoveSuccessType;
    private String msgUnknownSub;
    private MessageType msgUnknownSubType;
    private String msgPlayersOnly;
    private MessageType msgPlayersOnlyType;
    private String msgReloaded;
    private MessageType msgReloadedType;
    private String msgListPrompt;
    private MessageType msgListPromptType;
    private String msgHeaderInfo;
    private MessageType msgHeaderInfoType;
    private String msgViewDisabled;
    private MessageType msgViewDisabledType;
    private String msgViewEnabled;
    private MessageType msgViewEnabledType;
    private String msgViewUsage;
    private MessageType msgViewUsageType;
    private boolean debug;
    private File blocksFile;
    private FileConfiguration blocksConfig;
    private final Map<UUID, Long> cooldownMap = new HashMap<>();
    private final Set<UUID> recentInteractions = ConcurrentHashMap.newKeySet();
    private final Set<UUID> pendingPearlCancel = ConcurrentHashMap.newKeySet();
    private final Map<String, BlockData> blockDataMap = new ConcurrentHashMap<>();
    private final Map<Integer, Set<String>> idIndex = new ConcurrentHashMap<>();
    private final Map<UUID, List<String>> playerBlocks = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> globalMembers = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> globalOwners = new ConcurrentHashMap<>();
    private final Map<UUID, ChatInputSession> chatSessions = new HashMap<>();
    private final Map<UUID, Inventory> openGuis = new HashMap<>();
    private final Map<UUID, GuiContext> guiContexts = new HashMap<>();
    private final Map<UUID, ViewSession> viewSessions = new HashMap<>();
    private final Map<String, UUID> hologramEntities = new ConcurrentHashMap<>();

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
        public String customName = "";
        public boolean hologramEnabled = false;
        public String hologramColor = "&#FF5300";
        public Set<UUID> members = new HashSet<>();
        public Set<UUID> owners = new HashSet<>();
        public String key() { return world + ":" + x + ":" + y + ":" + z; }
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

    public static class ViewSession {
        public String mode;
        public long endTime;
        public int taskId;
        public ViewSession(String mode, long endTime, int taskId) {
            this.mode = mode;
            this.endTime = endTime;
            this.taskId = taskId;
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        loadConfig();
        loadBlocks();
        getServer().getPluginManager().registerEvents(this, this);
        if (getCommand("elevator") != null) getCommand("elevator").setExecutor(this);
        startHologramUpdater();
        getLogger().info("==================================================");
        getLogger().info("   Elevator Plugin Enabled!");
        getLogger().info("   Elevator blocks: " + elevatorBlocks.size());
        getLogger().info("   Registered blocks: " + blockDataMap.size());
        getLogger().info("   Max distance: " + blockDistance);
        getLogger().info("==================================================");
    }

    @Override
    public void onDisable() {
        for (ViewSession session : viewSessions.values()) Bukkit.getScheduler().cancelTask(session.taskId);
        viewSessions.clear();
        for (BlockData data : blockDataMap.values()) removeHologram(data);
        hologramEntities.clear();
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

    private String prefixed(String message) { return color(PREFIX + message); }

    private void debug(String message) { if (debug) getLogger().info("[DEBUG] " + message); }

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
        try { particleType = Particle.valueOf(particleName); }
        catch (IllegalArgumentException e) { particleType = Particle.SPELL_WITCH; }
        teleporterEnableParticle = getConfig().getBoolean("Teleporter.EnableParticle", true);
        teleporterUsageSound = getConfig().getString("Teleporter.UsageSound", "entity.enderman.teleport");
        teleporterAllowUnsafe = getConfig().getBoolean("Teleporter.AllowUnsafe", true);
        allowCrossWorlds = getConfig().getBoolean("Teleporter.AllowCrossWorlds", true);
        teleporterAllowAllBlocks = getConfig().getBoolean("Teleporter.AllowAllBlocks", false);
        teleporterBlockTypesPermission = getConfig().getString("Teleporter.BlockTypesPermission", "");
        distanceCheckEnabled = getConfig().getBoolean("Teleporter.DistanceCheck.Enabled", true);
        maxDistance = getConfig().getDouble("Teleporter.DistanceCheck.MaxDistance", 10000.0);
        teleporterBindConsumePearl = getConfig().getBoolean("Teleporter.Bind.ConsumePearl", true);
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
        cooldownMessageType = getMessageType(getConfig().getString("Cooldown.MessageType", "SUBTITLE"));
        ConfigurationSection elevatorTitle = getConfig().getConfigurationSection("ElevatorLocale.Title");
        if (elevatorTitle != null) {
            elevatorTitleFadeIn = elevatorTitle.getInt("FadeIn", 10);
            elevatorTitleStay = elevatorTitle.getInt("Stay", 40);
            elevatorTitleFadeOut = elevatorTitle.getInt("FadeOut", 10);
        } else { elevatorTitleFadeIn = 10; elevatorTitleStay = 40; elevatorTitleFadeOut = 10; }
        elevatorUpMessage = getConfig().getString("ElevatorLocale.ElevatorUp", "");
        elevatorUpType = msgType("ElevatorLocale.ElevatorUpType", MessageType.SUBTITLE);
        elevatorDownMessage = getConfig().getString("ElevatorLocale.ElevatorDown", "");
        elevatorDownType = msgType("ElevatorLocale.ElevatorDownType", MessageType.SUBTITLE);
        elevatorDangerMessage = getConfig().getString("ElevatorLocale.ElevatorDanger", "");
        elevatorDangerType = msgType("ElevatorLocale.ElevatorDangerType", MessageType.SUBTITLE);
        ConfigurationSection teleporterTitle = getConfig().getConfigurationSection("TeleporterLocale.Title");
        if (teleporterTitle != null) {
            teleporterTitleFadeIn = teleporterTitle.getInt("FadeIn", 10);
            teleporterTitleStay = teleporterTitle.getInt("Stay", 60);
            teleporterTitleFadeOut = teleporterTitle.getInt("FadeOut", 10);
        } else { teleporterTitleFadeIn = 10; teleporterTitleStay = 60; teleporterTitleFadeOut = 10; }
        teleporterSuccessMessage = getConfig().getString("TeleporterLocale.TeleportSuccess", "");
        teleporterSuccessType = msgType("TeleporterLocale.TeleportSuccessType", MessageType.SUBTITLE);
        teleporterBlockBoundMessage = getConfig().getString("TeleporterLocale.BlockBound", "");
        teleporterBlockBoundType = msgType("TeleporterLocale.BlockBoundType", MessageType.CHAT);
        teleporterNoPairMessage = getConfig().getString("TeleporterLocale.NoPair", "");
        teleporterNoPairType = msgType("TeleporterLocale.NoPairType", MessageType.CHAT);
        teleporterNoCrossWorldMessage = getConfig().getString("TeleporterLocale.NoCrossWorld", "");
        teleporterNoCrossWorldType = msgType("TeleporterLocale.NoCrossWorldType", MessageType.CHAT);
        teleporterMissingItemMessage = getConfig().getString("TeleporterLocale.MissingItem", "");
        teleporterMissingItemType = msgType("TeleporterLocale.MissingItemType", MessageType.CHAT);
        teleporterDistanceTooFarMessage = getConfig().getString("TeleporterLocale.DistanceTooFar", "");
        teleporterDistanceTooFarType = msgType("TeleporterLocale.DistanceTooFarType", MessageType.CHAT);
        guiMainTitle = getConfig().getString("GUI.Main.Title", "&7Teleporter Block");
        guiMainIdItem = getConfig().getString("GUI.Main.IdItem", "&#FF5300ID: %id%");
        guiMainIdItemEmpty = getConfig().getString("GUI.Main.IdItemEmpty", "&#FF5300ID: Not set");
        guiMainIdItemLore = getConfig().getString("GUI.Main.IdItemLore", "&7Click to change ID");
        guiMainClickItem = getConfig().getString("GUI.Main.ClickItem", "&#FF5300Click Type: %value%");
        guiMainClickItemLore = getConfig().getString("GUI.Main.ClickItemLore", "&7Left or Right mouse button");
        guiMainSneakItem = getConfig().getString("GUI.Main.SneakItem", "&#FF5300Require Sneak: %value%");
        guiMainSneakItemLore = getConfig().getString("GUI.Main.SneakItemLore", "&7Toggle sneak requirement");
        guiMainRequiredItem = getConfig().getString("GUI.Main.RequiredItem", "&#FF5300Require Item: %value%");
        guiMainRequiredItemLore = getConfig().getString("GUI.Main.RequiredItemLore", "&7Required: %name%");
        guiMainRequiredItemLoreEmpty = getConfig().getString("GUI.Main.RequiredItemLoreEmpty", "&7Required: -");
        guiMainRequiredItemLoreClick = getConfig().getString("GUI.Main.RequiredItemLoreClick", "&7Click to change");
        guiMainLocationItem = getConfig().getString("GUI.Main.LocationItem", "&#FF5300Teleport Location: %value%");
        guiMainLocationItemLore = getConfig().getString("GUI.Main.LocationItemLore", "&7TOP or CURRENT");
        guiMainLocationItemLoreClick = getConfig().getString("GUI.Main.LocationItemLoreClick", "&7Click to cycle");
        guiMainNameItem = getConfig().getString("GUI.Main.NameItem", "&#FF5300Name: %value%");
        guiMainNameItemEmpty = getConfig().getString("GUI.Main.NameItemEmpty", "&#FF5300Name: Not set");
        guiMainNameItemLore = getConfig().getString("GUI.Main.NameItemLore", "&7Click to rename");
        guiMainHologramItem = getConfig().getString("GUI.Main.HologramItem", "&#FF5300Hologram: %value%");
        guiMainHologramItemLore = getConfig().getString("GUI.Main.HologramItemLore", "&7Toggle hologram above block");
        guiMainHologramColorItem = getConfig().getString("GUI.Main.HologramColorItem", "&#FF5300Hologram Color: %value%");
        guiMainHologramColorItemLore = getConfig().getString("GUI.Main.HologramColorItemLore", "&7Click to change color");
        guiMainTeleportAccessItem = getConfig().getString("GUI.Main.TeleportAccessItem", "&#FF5300Teleport Access: %value%");
        guiMainTeleportAccessItemLore = getConfig().getString("GUI.Main.TeleportAccessItemLore", "&7Click to cycle");
        guiMainTeleportAccessItemLore2 = getConfig().getString("GUI.Main.TeleportAccessItemLore2", "&7OWNER / MEMBERS / OWNERS / ALL");
        guiMainManageAccessItem = getConfig().getString("GUI.Main.ManageAccessItem", "&#FF5300Manage Access: %value%");
        guiMainManageAccessItemLore = getConfig().getString("GUI.Main.ManageAccessItemLore", "&7Click to cycle");
        guiMainManageAccessItemLore2 = getConfig().getString("GUI.Main.ManageAccessItemLore2", "&7OWNER / OWNERS / ALL");
        guiMainBreakAccessItem = getConfig().getString("GUI.Main.BreakAccessItem", "&#FF5300Break Access: %value%");
        guiMainBreakAccessItemLore = getConfig().getString("GUI.Main.BreakAccessItemLore", "&7Click to cycle");
        guiMainBreakAccessItemLore2 = getConfig().getString("GUI.Main.BreakAccessItemLore2", "&7OWNER / MEMBERS / OWNERS / ALL");
        guiMainMembersItem = getConfig().getString("GUI.Main.MembersItem", "&#FF5300Members & Owners");
        guiMainMembersItemLore = getConfig().getString("GUI.Main.MembersItemLore", "&7Manage local and global lists");
        guiMainCloseItem = getConfig().getString("GUI.Main.CloseItem", "&#FF5300Close");
        guiMembersTitle = getConfig().getString("GUI.Members.Title", "&7Members & Owners");
        guiMembersLocalMembers = getConfig().getString("GUI.Members.LocalMembers", "&#FF5300Local Members: %count%");
        guiMembersLocalOwners = getConfig().getString("GUI.Members.LocalOwners", "&#FF5300Local Owners: %count%");
        guiMembersGlobalMembers = getConfig().getString("GUI.Members.GlobalMembers", "&#FF5300Global Members: %count%");
        guiMembersGlobalOwners = getConfig().getString("GUI.Members.GlobalOwners", "&#FF5300Global Owners: %count%");
        guiMembersClickManage = getConfig().getString("GUI.Members.ClickManage", "&7Click to manage");
        guiMembersBack = getConfig().getString("GUI.Members.Back", "&#FF5300Back");
        guiListTitle = getConfig().getString("GUI.List.Title", "&7%list%");
        guiListPlayerItem = getConfig().getString("GUI.List.PlayerItem", "&#FF5300%name%");
        guiListPlayerItemLore = getConfig().getString("GUI.List.PlayerItemLore", "&7Click to remove");
        guiListPrevious = getConfig().getString("GUI.List.Previous", "&#FF5300Previous");
        guiListNext = getConfig().getString("GUI.List.Next", "&#FF5300Next");
        guiListBack = getConfig().getString("GUI.List.Back", "&#FF5300Back");
        guiListAddPlayer = getConfig().getString("GUI.List.AddPlayer", "&#FF5300Add player");
        msgIdPrompt = getConfig().getString("Messages.IdPrompt", "");
        msgIdPromptType = msgType("Messages.IdPromptType", MessageType.CHAT);
        msgIdInvalid = getConfig().getString("Messages.IdInvalid", "");
        msgIdInvalidType = msgType("Messages.IdInvalidType", MessageType.CHAT);
        msgIdRange = getConfig().getString("Messages.IdRange", "");
        msgIdRangeType = msgType("Messages.IdRangeType", MessageType.CHAT);
        msgIdUsed = getConfig().getString("Messages.IdUsed", "");
        msgIdUsedType = msgType("Messages.IdUsedType", MessageType.CHAT);
        msgIdSet = getConfig().getString("Messages.IdSet", "");
        msgIdSetType = msgType("Messages.IdSetType", MessageType.CHAT);
        msgItemPrompt = getConfig().getString("Messages.ItemPrompt", "");
        msgItemPromptType = msgType("Messages.ItemPromptType", MessageType.CHAT);
        msgItemDisabled = getConfig().getString("Messages.ItemDisabled", "");
        msgItemDisabledType = msgType("Messages.ItemDisabledType", MessageType.CHAT);
        msgItemSet = getConfig().getString("Messages.ItemSet", "");
        msgItemSetType = msgType("Messages.ItemSetType", MessageType.CHAT);
        msgNamePrompt = getConfig().getString("Messages.NamePrompt", "");
        msgNamePromptType = msgType("Messages.NamePromptType", MessageType.CHAT);
        msgNameSet = getConfig().getString("Messages.NameSet", "");
        msgNameSetType = msgType("Messages.NameSetType", MessageType.CHAT);
        msgColorPrompt = getConfig().getString("Messages.ColorPrompt", "");
        msgColorPromptType = msgType("Messages.ColorPromptType", MessageType.CHAT);
        msgColorSet = getConfig().getString("Messages.ColorSet", "");
        msgColorSetType = msgType("Messages.ColorSetType", MessageType.CHAT);
        msgColorInvalid = getConfig().getString("Messages.ColorInvalid", "");
        msgColorInvalidType = msgType("Messages.ColorInvalidType", MessageType.CHAT);
        msgCancelled = getConfig().getString("Messages.Cancelled", "");
        msgCancelledType = msgType("Messages.CancelledType", MessageType.CHAT);
        msgNoPermission = getConfig().getString("Messages.NoPermission", "");
        msgNoPermissionType = msgType("Messages.NoPermissionType", MessageType.CHAT);
        msgBlockGone = getConfig().getString("Messages.BlockGone", "");
        msgBlockGoneType = msgType("Messages.BlockGoneType", MessageType.CHAT);
        msgPlayerNotFound = getConfig().getString("Messages.PlayerNotFound", "");
        msgPlayerNotFoundType = msgType("Messages.PlayerNotFoundType", MessageType.CHAT);
        msgPlayerAdded = getConfig().getString("Messages.PlayerAdded", "");
        msgPlayerAddedType = msgType("Messages.PlayerAddedType", MessageType.CHAT);
        msgNoBlockInSight = getConfig().getString("Messages.NoBlockInSight", "");
        msgNoBlockInSightType = msgType("Messages.NoBlockInSightType", MessageType.CHAT);
        msgNotTeleporter = getConfig().getString("Messages.NotTeleporter", "");
        msgNotTeleporterType = msgType("Messages.NotTeleporterType", MessageType.CHAT);
        msgNoBlocksOwned = getConfig().getString("Messages.NoBlocksOwned", "");
        msgNoBlocksOwnedType = msgType("Messages.NoBlocksOwnedType", MessageType.CHAT);
        msgYourBlocks = getConfig().getString("Messages.YourBlocks", "");
        msgYourBlocksType = msgType("Messages.YourBlocksType", MessageType.CHAT);
        msgRemoveUsage = getConfig().getString("Messages.RemoveUsage", "");
        msgRemoveUsageType = msgType("Messages.RemoveUsageType", MessageType.CHAT);
        msgRemoveInvalid = getConfig().getString("Messages.RemoveInvalid", "");
        msgRemoveInvalidType = msgType("Messages.RemoveInvalidType", MessageType.CHAT);
        msgRemoveNone = getConfig().getString("Messages.RemoveNone", "");
        msgRemoveNoneType = msgType("Messages.RemoveNoneType", MessageType.CHAT);
        msgRemoveNotOwner = getConfig().getString("Messages.RemoveNotOwner", "");
        msgRemoveNotOwnerType = msgType("Messages.RemoveNotOwnerType", MessageType.CHAT);
        msgRemoveSuccess = getConfig().getString("Messages.RemoveSuccess", "");
        msgRemoveSuccessType = msgType("Messages.RemoveSuccessType", MessageType.CHAT);
        msgUnknownSub = getConfig().getString("Messages.UnknownSub", "");
        msgUnknownSubType = msgType("Messages.UnknownSubType", MessageType.CHAT);
        msgPlayersOnly = getConfig().getString("Messages.PlayersOnly", "");
        msgPlayersOnlyType = msgType("Messages.PlayersOnlyType", MessageType.CHAT);
        msgReloaded = getConfig().getString("Messages.Reloaded", "");
        msgReloadedType = msgType("Messages.ReloadedType", MessageType.CHAT);
        msgListPrompt = getConfig().getString("Messages.ListPrompt", "");
        msgListPromptType = msgType("Messages.ListPromptType", MessageType.CHAT);
        msgHeaderInfo = getConfig().getString("Messages.HeaderInfo", "");
        msgHeaderInfoType = msgType("Messages.HeaderInfoType", MessageType.CHAT);
        msgViewDisabled = getConfig().getString("Messages.ViewDisabled", "");
        msgViewDisabledType = msgType("Messages.ViewDisabledType", MessageType.CHAT);
        msgViewEnabled = getConfig().getString("Messages.ViewEnabled", "");
        msgViewEnabledType = msgType("Messages.ViewEnabledType", MessageType.CHAT);
        msgViewUsage = getConfig().getString("Messages.ViewUsage", "");
        msgViewUsageType = msgType("Messages.ViewUsageType", MessageType.CHAT);
        viewDurationTicks = getConfig().getInt("View.DurationTicks", 200);
        viewUpdateTicks = getConfig().getInt("View.UpdateEveryTicks", 5);
        viewMaxDistance = getConfig().getInt("View.MaxDistance", 64);
        viewParticleCount = getConfig().getInt("View.ParticleCount", 3);
        try { viewParticleType = Particle.valueOf(getConfig().getString("View.ParticleType", "END_ROD")); }
        catch (IllegalArgumentException e) { viewParticleType = Particle.END_ROD; }
        viewPermAll = getConfig().getString("View.PermissionAll", "elevator.view.all");
        viewPermOwner = getConfig().getString("View.PermissionOwner", "elevator.view.owner");
        viewPermMember = getConfig().getString("View.PermissionMember", "elevator.view.member");
        hologramsDefaultEnabled = getConfig().getBoolean("Holograms.DefaultEnabled", false);
        hologramsDefaultColor = getConfig().getString("Holograms.DefaultColor", "&#FF5300");
        hologramsViewDistance = getConfig().getInt("Holograms.ViewDistance", 16);
        hologramsUpdateInterval = getConfig().getInt("Holograms.UpdateIntervalTicks", 20);
        hologramsShowId = getConfig().getBoolean("Holograms.ShowId", true);
        hologramsShowName = getConfig().getBoolean("Holograms.ShowName", true);
        hologramsFormat = getConfig().getString("Holograms.Format", "%name% &7| &fID: %id%");
        hologramsFormatNoName = getConfig().getString("Holograms.FormatNoName", "&7ID: %id%");
        hologramsLineHeight = getConfig().getDouble("Holograms.LineHeight", 0.3);
        blockNamingEnabled = getConfig().getBoolean("BlockNaming.Enabled", true);
        blockNamingMaxLength = getConfig().getInt("BlockNaming.MaxLength", 32);
        blockNamingDefaultName = getConfig().getString("BlockNaming.DefaultName", "");
        disabledWorlds.clear();
        disabledWorlds.addAll(getConfig().getStringList("DisabledWorlds"));
        checkPermission = getConfig().getBoolean("Permissions.CheckPermission", false);
        permUse = getConfig().getString("Permissions.Use", "elevator.use");
        permBypass = getConfig().getString("Permissions.BypassCooldown", "elevator.bypass");
        permTeleport = getConfig().getString("Permissions.Teleport", "elevator.teleport");
        permManage = getConfig().getString("Permissions.Manage", "elevator.manage");
        permBreakBypass = getConfig().getString("Permissions.BreakBypass", "elevator.break.bypass");
        permBypassAccess = getConfig().getString("Permissions.Bypass.Access", "elevator.bypass.access");
        permBypassDistance = getConfig().getString("Permissions.Bypass.Distance", "elevator.bypass.distance");
        permBypassItem = getConfig().getString("Permissions.Bypass.Item", "elevator.bypass.item");
        permBypassSneak = getConfig().getString("Permissions.Bypass.Sneak", "elevator.bypass.sneak");
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

    private MessageType msgType(String path, MessageType fallback) {
        String val = getConfig().getString(path);
        if (val == null) return fallback;
        return getMessageType(val);
    }

    private MessageType getMessageType(String type) {
        if (type == null) return MessageType.NONE;
        String t = type.trim().toUpperCase(Locale.ROOT);
        if (t.isEmpty() || t.equals("NONE") || t.equals("OFF") || t.equals("DISABLED")) return MessageType.NONE;
        try { return MessageType.valueOf(t); }
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
        blockDataMap.clear(); idIndex.clear(); playerBlocks.clear(); globalMembers.clear(); globalOwners.clear();
        blocksFile = new File(getDataFolder(), "blocks.yml");
        if (!blocksFile.exists()) {
            try { getDataFolder().mkdirs(); blocksFile.createNewFile(); }
            catch (IOException e) { getLogger().severe("Could not create blocks.yml: " + e.getMessage()); return; }
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
                data.customName = sec.getString("CustomName", "");
                data.hologramEnabled = sec.getBoolean("HologramEnabled", hologramsDefaultEnabled);
                data.hologramColor = sec.getString("HologramColor", hologramsDefaultColor);
                for (String member : sec.getStringList("Members")) { try { data.members.add(UUID.fromString(member)); } catch (IllegalArgumentException ignored) {} }
                for (String owner : sec.getStringList("Owners")) { try { data.owners.add(UUID.fromString(owner)); } catch (IllegalArgumentException ignored) {} }
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
                for (String m : pSec.getStringList("GlobalMembers")) { try { members.add(UUID.fromString(m)); } catch (IllegalArgumentException ignored) {} }
                Set<UUID> owners = ConcurrentHashMap.newKeySet();
                for (String o : pSec.getStringList("GlobalOwners")) { try { owners.add(UUID.fromString(o)); } catch (IllegalArgumentException ignored) {} }
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
            blocksConfig.set(path + ".CustomName", data.customName);
            blocksConfig.set(path + ".HologramEnabled", data.hologramEnabled);
            blocksConfig.set(path + ".HologramColor", data.hologramColor);
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
        if (type == null || type == MessageType.NONE) return;
        if (message == null || message.isEmpty()) return;
        String formatted = color(message);
        if (formatted.isEmpty()) return;
        switch (type) {
            case CHAT: player.sendMessage(formatted); break;
            case TITLE: player.sendTitle(formatted, "", elevatorTitleFadeIn, elevatorTitleStay, elevatorTitleFadeOut); break;
            case SUBTITLE: player.sendTitle("", formatted, elevatorTitleFadeIn, elevatorTitleStay, elevatorTitleFadeOut); break;
            default: break;
        }
    }

    private void sendTeleporterMessage(Player player, String message, MessageType type) {
        if (type == null || type == MessageType.NONE) return;
        if (message == null || message.isEmpty()) return;
        String formatted = color(message);
        if (formatted.isEmpty()) return;
        switch (type) {
            case CHAT: player.sendMessage(formatted); break;
            case TITLE: player.sendTitle(formatted, "", teleporterTitleFadeIn, teleporterTitleStay, teleporterTitleFadeOut); break;
            case SUBTITLE: player.sendTitle("", formatted, teleporterTitleFadeIn, teleporterTitleStay, teleporterTitleFadeOut); break;
            default: break;
        }
    }

    private boolean hasPermission(Player player, String perm) {
        if (!checkPermission) return true;
        if (perm == null || perm.isEmpty()) return true;
        return player.hasPermission(perm);
    }

    private boolean isInDisabledWorld(Player player) { return disabledWorlds.contains(player.getWorld().getName()); }

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
        if (enableCooldown && !hasPermission(player, permBypass)) cooldownMap.put(player.getUniqueId(), System.currentTimeMillis());
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
                if (!isSafeLocation(targetLoc, false)) { sendMessage(player, elevatorDangerMessage, elevatorDangerType); return; }
                player.teleport(targetLoc);
                playEffects(targetLoc, usageSound, false);
                sendMessage(player, elevatorDownMessage, elevatorDownType);
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
                if (!isSafeLocation(targetLoc, false)) { sendMessage(player, elevatorDangerMessage, elevatorDangerType); return; }
                player.setVelocity(player.getVelocity().setY(0));
                final Location finalLoc = targetLoc;
                new BukkitRunnable() {
                    @Override public void run() {
                        player.teleport(finalLoc);
                        playEffects(finalLoc, usageSound, false);
                        sendMessage(player, elevatorUpMessage, elevatorUpType);
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
                    if (!isSafeLocation(targetLoc, false)) { sendMessage(player, elevatorDangerMessage, elevatorDangerType); return; }
                    player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                    player.setFallDistance(0);
                    player.teleport(targetLoc);
                    playEffects(targetLoc, usageSound, false);
                    sendMessage(player, elevatorUpMessage, elevatorUpType);
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
            @Override public void run() {
                if (!p.isOnline() || !p.isSneaking()) return;
                teleportDown(p);
            }
        }.runTaskLater(this, SNEAK_CHECK_DELAY_TICKS);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerInteractLowest(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() != Material.ENDER_PEARL) return;
        Block block = event.getClickedBlock();
        Material blockType = block.getType();
        String key = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
        BlockData data = blockDataMap.get(key);
        boolean shouldCancel = false;
        if (data != null) shouldCancel = true;
        else {
            if (teleporterAllowAllBlocks) shouldCancel = true;
            else {
                if (teleporterBlockTypes.contains(blockType)) shouldCancel = true;
                else if (teleporterBlockTypesPermission != null && !teleporterBlockTypesPermission.isEmpty() && player.hasPermission(teleporterBlockTypesPermission)) shouldCancel = true;
            }
        }
        if (shouldCancel) {
            event.setCancelled(true);
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
            pendingPearlCancel.add(player.getUniqueId());
            new BukkitRunnable() {
                @Override public void run() { pendingPearlCancel.remove(player.getUniqueId()); }
            }.runTaskLater(this, 2L);
        }
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
            if (isRight && holdingEnderPearl) { event.setCancelled(true); tryBindBlock(player, block, blockType); }
            return;
        }
        if (isRight && player.isSneaking()) {
            event.setCancelled(true);
            if (!featureManageEnabled) { debug("Manage disabled"); return; }
            if (!canManage(player, data)) { debug("No manage rights for " + player.getName()); return; }
            openMainMenu(player, data);
            return;
        }
        if (!featureTeleportEnabled) { debug("Teleport disabled"); return; }
        if (!canTeleport(player, data)) { debug("No teleport rights for " + player.getName()); return; }
        ClickType clicked = isRight ? ClickType.RIGHT : ClickType.LEFT;
        if (data.clickType != clicked) return;
        boolean bypassSneak = checkPermission && player.hasPermission(permBypassSneak);
        if (!bypassSneak) {
            if (data.requireSneak && !player.isSneaking()) return;
            if (!data.requireSneak && player.isSneaking()) return;
        }
        boolean bypassItem = checkPermission && player.hasPermission(permBypassItem);
        if (data.requireItem && !bypassItem) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!itemMatches(item, data.requiredItemName)) { sendTeleporterMessage(player, teleporterMissingItemMessage, teleporterMissingItemType); return; }
        }
        if (recentInteractions.contains(player.getUniqueId())) return;
        recentInteractions.add(player.getUniqueId());
        new BukkitRunnable() {
            @Override public void run() { recentInteractions.remove(player.getUniqueId()); }
        }.runTaskLater(this, DOUBLE_CLICK_DELAY_TICKS);
        event.setCancelled(true);
        performTeleport(player, data);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) return;
        EnderPearl pearl = (EnderPearl) event.getEntity();
        ProjectileSource source = pearl.getShooter();
        if (!(source instanceof Player)) return;
        Player player = (Player) source;
        if (pendingPearlCancel.contains(player.getUniqueId())) {
            event.setCancelled(true);
            pearl.remove();
            pendingPearlCancel.remove(player.getUniqueId());
        }
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
                if (teleporterBlockTypesPermission != null && !teleporterBlockTypesPermission.isEmpty()) allowedByPerm = player.hasPermission(teleporterBlockTypesPermission);
                if (!allowedByPerm) { debug("Block type not allowed"); return; }
            }
        } else {
            if (teleporterBlockTypesPermission != null && !teleporterBlockTypesPermission.isEmpty() && !player.hasPermission(teleporterBlockTypesPermission)) { debug("Missing bind permission"); return; }
        }
        if (!hasPermission(player, permTeleport)) { debug("Missing teleport permission"); return; }
        String key = block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();
        if (blockDataMap.containsKey(key)) { debug("Already bound"); return; }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() != Material.ENDER_PEARL) return;
        if (teleporterBindConsumePearl) {
            if (hand.getAmount() > 1) hand.setAmount(hand.getAmount() - 1);
            else player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
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
        data.teleportLocation = TeleportLocation.TOP;
        data.customName = blockNamingDefaultName;
        data.hologramEnabled = hologramsDefaultEnabled;
        data.hologramColor = hologramsDefaultColor;
        data.id = 0;
        blockDataMap.put(key, data);
        playerBlocks.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>()).add(key);
        saveBlocks();
        playEffects(block.getLocation(), activateSound, true);
        sendTeleporterMessage(player, teleporterBlockBoundMessage, teleporterBlockBoundType);
        openMainMenu(player, data);
    }

    private void performTeleport(Player player, BlockData source) {
        int id = source.id;
        if (id <= 0) { debug("No ID"); return; }
        Set<String> keys = idIndex.get(id);
        if (keys == null || keys.size() < 2) { sendTeleporterMessage(player, teleporterNoPairMessage, teleporterNoPairType); return; }
        BlockData destination = null;
        for (String k : keys) if (!k.equals(source.key())) { destination = blockDataMap.get(k); break; }
        if (destination == null) { sendTeleporterMessage(player, teleporterNoPairMessage, teleporterNoPairType); return; }
        World destWorld = Bukkit.getWorld(destination.world);
        if (destWorld == null) { debug("Dest world missing"); return; }
        if (!allowCrossWorlds && !player.getWorld().getName().equals(destination.world)) { sendTeleporterMessage(player, teleporterNoCrossWorldMessage, teleporterNoCrossWorldType); return; }
        boolean bypassDistance = checkPermission && player.hasPermission(permBypassDistance);
        if (!bypassDistance && distanceCheckEnabled) {
            Location a = new Location(player.getWorld(), source.x, source.y, source.z);
            Location b = new Location(destWorld, destination.x, destination.y, destination.z);
            if (a.getWorld() != null && a.getWorld().equals(b.getWorld())) {
                double dist = a.distance(b);
                if (dist > maxDistance) { sendTeleporterMessage(player, teleporterDistanceTooFarMessage, teleporterDistanceTooFarType); return; }
            }
        }
        Location destLoc;
        if (destination.teleportLocation == TeleportLocation.CURRENT && !destination.customDestinationWorld.isEmpty()) {
            World w = Bukkit.getWorld(destination.customDestinationWorld);
            if (w == null) w = destWorld;
            destLoc = new Location(w, destination.customDestinationX, destination.customDestinationY, destination.customDestinationZ, destination.customDestinationYaw, destination.customDestinationPitch);
        } else {
            destLoc = new Location(destWorld, destination.x + 0.5, destination.y + 1, destination.z + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch());
        }
        if (!isSafeLocation(destLoc, true)) { sendTeleporterMessage(player, elevatorDangerMessage, elevatorDangerType); return; }
        Location sourceLoc = player.getLocation().clone();
        playEffects(sourceLoc, teleporterUsageSound, true);
        playEffects(destLoc, teleporterUsageSound, true);
        player.teleport(destLoc);
        playEffects(destLoc, teleporterUsageSound, true);
        sendTeleporterMessage(player, teleporterSuccessMessage, teleporterSuccessType);
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
        if (checkPermission && permBreakBypass != null && !permBreakBypass.isEmpty() && player.hasPermission(permBreakBypass)) { removeBlock(data); return; }
        if (!canBreak(player, data)) { event.setCancelled(true); debug("No break rights"); return; }
        removeBlock(data);
    }

    private void removeBlock(BlockData data) {
        String key = data.key();
        removeHologram(data);
        blockDataMap.remove(key);
        Set<String> keys = idIndex.get(data.id);
        if (keys != null) { keys.remove(key); if (keys.isEmpty()) idIndex.remove(data.id); }
        List<String> list = playerBlocks.get(data.owner);
        if (list != null) { list.remove(key); if (list.isEmpty()) playerBlocks.remove(data.owner); }
        saveBlocks();
    }

    private boolean canTeleport(Player player, BlockData data) {
        if (checkPermission && player.hasPermission(permBypassAccess)) return true;
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
        if (checkPermission && player.hasPermission(permBypassAccess)) return true;
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
        if (checkPermission && player.hasPermission(permBypassAccess)) return true;
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
        inv.setItem(4, createItem(Material.ENDER_PEARL, data.id > 0 ? guiMainIdItem.replace("%id%", String.valueOf(data.id)) : guiMainIdItemEmpty, guiMainIdItemLore));
        inv.setItem(10, createItem(Material.LEVER, guiMainClickItem.replace("%value%", data.clickType.name()), guiMainClickItemLore));
        inv.setItem(11, createItem(Material.SHIELD, guiMainSneakItem.replace("%value%", data.requireSneak ? "Yes" : "No"), guiMainSneakItemLore));
        inv.setItem(12, createItem(Material.NAME_TAG, guiMainRequiredItem.replace("%value%", data.requireItem ? "Yes" : "No"), data.requiredItemName.isEmpty() ? guiMainRequiredItemLoreEmpty : guiMainRequiredItemLore.replace("%name%", data.requiredItemName), guiMainRequiredItemLoreClick));
        inv.setItem(13, createItem(Material.ENDER_EYE, guiMainLocationItem.replace("%value%", data.teleportLocation.name()), guiMainLocationItemLore, guiMainLocationItemLoreClick));
        inv.setItem(14, createItem(Material.PLAYER_HEAD, guiMainTeleportAccessItem.replace("%value%", data.teleportAccess.name()), guiMainTeleportAccessItemLore, guiMainTeleportAccessItemLore2));
        inv.setItem(15, createItem(Material.COMMAND_BLOCK, guiMainManageAccessItem.replace("%value%", data.manageAccess.name()), guiMainManageAccessItemLore, guiMainManageAccessItemLore2));
        inv.setItem(16, createItem(Material.DIAMOND_PICKAXE, guiMainBreakAccessItem.replace("%value%", data.breakAccess.name()), guiMainBreakAccessItemLore, guiMainBreakAccessItemLore2));
        inv.setItem(19, createItem(Material.BOOK, data.customName.isEmpty() ? guiMainNameItemEmpty : guiMainNameItem.replace("%value%", data.customName), guiMainNameItemLore));
        inv.setItem(20, createItem(Material.ARMOR_STAND, guiMainHologramItem.replace("%value%", data.hologramEnabled ? "Yes" : "No"), guiMainHologramItemLore));
        inv.setItem(21, createItem(Material.GLOWSTONE_DUST, guiMainHologramColorItem.replace("%value%", data.hologramColor), guiMainHologramColorItemLore));
        inv.setItem(22, createItem(Material.CHEST, guiMainMembersItem, guiMainMembersItemLore));
        inv.setItem(26, createItem(Material.BARRIER, guiMainCloseItem));
        player.openInventory(inv);
        openGuis.put(player.getUniqueId(), inv);
        guiContexts.put(player.getUniqueId(), new GuiContext(data.key(), "MAIN"));
    }

    private void openMembersMenu(Player player, BlockData data) {
        Inventory inv = Bukkit.createInventory(null, 54, color(guiMembersTitle));
        inv.setItem(10, createItem(Material.PLAYER_HEAD, guiMembersLocalMembers.replace("%count%", String.valueOf(data.members.size())), guiMembersClickManage));
        inv.setItem(11, createItem(Material.PLAYER_HEAD, guiMembersLocalOwners.replace("%count%", String.valueOf(data.owners.size())), guiMembersClickManage));
        inv.setItem(12, createItem(Material.PLAYER_HEAD, guiMembersGlobalMembers.replace("%count%", String.valueOf(getGlobalMembers(data.owner).size())), guiMembersClickManage));
        inv.setItem(13, createItem(Material.PLAYER_HEAD, guiMembersGlobalOwners.replace("%count%", String.valueOf(getGlobalOwners(data.owner).size())), guiMembersClickManage));
        inv.setItem(49, createItem(Material.ARROW, guiMembersBack));
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
            inv.setItem(i - start, createItem(Material.PLAYER_HEAD, guiListPlayerItem.replace("%name%", name), guiListPlayerItemLore));
        }
        inv.setItem(45, createItem(Material.ARROW, guiListPrevious));
        inv.setItem(49, createItem(Material.ARROW, guiListBack));
        inv.setItem(53, createItem(Material.ARROW, guiListNext));
        inv.setItem(48, createItem(Material.EMERALD, guiListAddPlayer));
        player.openInventory(inv);
        openGuis.put(player.getUniqueId(), inv);
        GuiContext ctx = new GuiContext(data.key(), "LIST_" + listType);
        ctx.page = page;
        guiContexts.put(player.getUniqueId(), ctx);
    }

    private Set<UUID> getGlobalMembers(UUID owner) { return globalMembers.computeIfAbsent(owner, k -> ConcurrentHashMap.newKeySet()); }
    private Set<UUID> getGlobalOwners(UUID owner) { return globalOwners.computeIfAbsent(owner, k -> ConcurrentHashMap.newKeySet()); }

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
                case 4: player.closeInventory(); player.sendMessage(color(msgIdPrompt)); chatSessions.put(player.getUniqueId(), new ChatInputSession("SET_ID", data.key())); return;
                case 10: data.clickType = data.clickType == ClickType.LEFT ? ClickType.RIGHT : ClickType.LEFT; saveBlocks(); openMainMenu(player, data); return;
                case 11: data.requireSneak = !data.requireSneak; saveBlocks(); openMainMenu(player, data); return;
                case 12: player.closeInventory(); player.sendMessage(color(msgItemPrompt)); chatSessions.put(player.getUniqueId(), new ChatInputSession("SET_ITEM", data.key())); return;
                case 13:
                    data.teleportLocation = data.teleportLocation == TeleportLocation.TOP ? TeleportLocation.CURRENT : TeleportLocation.TOP;
                    if (data.teleportLocation == TeleportLocation.CURRENT) {
                        Location loc = player.getLocation();
                        Block block = player.getWorld().getBlockAt(data.x, data.y, data.z);
                        if (loc.distance(block.getLocation()) <= 5.0) {
                            data.customDestinationWorld = loc.getWorld().getName();
                            data.customDestinationX = loc.getX(); data.customDestinationY = loc.getY(); data.customDestinationZ = loc.getZ();
                            data.customDestinationYaw = loc.getYaw(); data.customDestinationPitch = loc.getPitch();
                        } else data.customDestinationWorld = "";
                    }
                    saveBlocks(); openMainMenu(player, data); return;
                case 14: data.teleportAccess = cycleTeleportAccess(data.teleportAccess); saveBlocks(); openMainMenu(player, data); return;
                case 15: data.manageAccess = cycleManageAccess(data.manageAccess); saveBlocks(); openMainMenu(player, data); return;
                case 16: data.breakAccess = cycleBreakAccess(data.breakAccess); saveBlocks(); openMainMenu(player, data); return;
                case 19: player.closeInventory(); player.sendMessage(color(msgNamePrompt)); chatSessions.put(player.getUniqueId(), new ChatInputSession("SET_NAME", data.key())); return;
                case 20: data.hologramEnabled = !data.hologramEnabled; if (data.hologramEnabled) updateHologram(data); else removeHologram(data); saveBlocks(); openMainMenu(player, data); return;
                case 21: player.closeInventory(); player.sendMessage(color(msgColorPrompt)); chatSessions.put(player.getUniqueId(), new ChatInputSession("SET_COLOR", data.key())); return;
                case 22: openMembersMenu(player, data); return;
                case 26: player.closeInventory(); return;
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
            if (slot == 48) { player.closeInventory(); player.sendMessage(color(msgListPrompt)); chatSessions.put(player.getUniqueId(), new ChatInputSession("ADD_" + listType, data.key())); return; }
            int perPage = 45;
            int index = ctx.page * perPage + slot;
            if (slot >= 0 && slot < perPage && index < sorted.size()) { list.remove(sorted.get(index)); saveBlocks(); openPlayerListView(player, data, listType, ctx.page); }
        }
    }

    private AccessLevel cycleTeleportAccess(AccessLevel c) { AccessLevel[] v = {AccessLevel.OWNER, AccessLevel.MEMBERS, AccessLevel.OWNERS, AccessLevel.ALL}; return v[(Arrays.asList(v).indexOf(c) + 1) % v.length]; }
    private AccessLevel cycleManageAccess(AccessLevel c) { AccessLevel[] v = {AccessLevel.OWNER, AccessLevel.OWNERS, AccessLevel.ALL}; return v[(Arrays.asList(v).indexOf(c) + 1) % v.length]; }
    private AccessLevel cycleBreakAccess(AccessLevel c) { AccessLevel[] v = {AccessLevel.OWNER, AccessLevel.MEMBERS, AccessLevel.OWNERS, AccessLevel.ALL}; return v[(Arrays.asList(v).indexOf(c) + 1) % v.length]; }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (openGuis.get(uuid) == event.getInventory()) { openGuis.remove(uuid); guiContexts.remove(uuid); }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ChatInputSession session = chatSessions.get(player.getUniqueId());
        if (session == null) return;
        if (System.currentTimeMillis() > session.expireTime) { chatSessions.remove(player.getUniqueId()); return; }
        event.setCancelled(true);
        String message = event.getMessage().trim();
        chatSessions.remove(player.getUniqueId());
        if (message.equalsIgnoreCase("cancel")) { player.sendMessage(color(msgCancelled)); return; }
        BlockData data = blockDataMap.get(session.blockKey);
        if (data == null) { player.sendMessage(color(msgBlockGone)); return; }
        new BukkitRunnable() {
            @Override public void run() { handleChatInput(player, data, session, message); }
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
                    if (existing.size() >= 2) { existing.add(data.key()); player.sendMessage(color(msgIdUsed)); return; }
                }
                Set<String> oldKeys = idIndex.get(data.id);
                if (oldKeys != null) { oldKeys.remove(data.key()); if (oldKeys.isEmpty()) idIndex.remove(data.id); }
                data.id = newId;
                idIndex.computeIfAbsent(newId, k -> ConcurrentHashMap.newKeySet()).add(data.key());
                saveBlocks();
                player.sendMessage(color(msgIdSet.replace("%id%", String.valueOf(newId))));
                openMainMenu(player, data);
                return;
            }
            case "SET_ITEM": {
                if (message.equalsIgnoreCase("no") || message.equalsIgnoreCase("not")) {
                    data.requireItem = false; data.requiredItemName = ""; saveBlocks();
                    player.sendMessage(color(msgItemDisabled)); openMainMenu(player, data); return;
                }
                data.requireItem = true; data.requiredItemName = message; saveBlocks();
                player.sendMessage(color(msgItemSet.replace("%name%", message))); openMainMenu(player, data); return;
            }
            case "SET_NAME": {
                if (!blockNamingEnabled) { player.sendMessage(color(msgCancelled)); return; }
                String name = message.length() > blockNamingMaxLength ? message.substring(0, blockNamingMaxLength) : message;
                data.customName = name; saveBlocks();
                if (data.hologramEnabled) updateHologram(data);
                player.sendMessage(color(msgNameSet.replace("%name%", name))); openMainMenu(player, data); return;
            }
            case "SET_COLOR": {
                if (!message.matches("&#[A-Fa-f0-9]{6}")) { player.sendMessage(color(msgColorInvalid)); return; }
                data.hologramColor = message; saveBlocks();
                if (data.hologramEnabled) updateHologram(data);
                player.sendMessage(color(msgColorSet.replace("%color%", message))); openMainMenu(player, data); return;
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
                    list.add(target.getUniqueId()); saveBlocks();
                    player.sendMessage(color(msgPlayerAdded.replace("%name%", target.getName())));
                    openPlayerListView(player, data, listType, 0);
                }
            }
        }
    }

    private void startHologramUpdater() {
        new BukkitRunnable() {
            @Override public void run() {
                for (BlockData data : blockDataMap.values()) if (data.hologramEnabled) updateHologram(data);
            }
        }.runTaskTimer(this, 20L, Math.max(1, hologramsUpdateInterval));
    }

    private void updateHologram(BlockData data) {
        if (!data.hologramEnabled) { removeHologram(data); return; }
        World w = Bukkit.getWorld(data.world);
        if (w == null) return;
        Location loc = new Location(w, data.x + 0.5, data.y + 1.2 + hologramsLineHeight, data.z + 0.5);
        UUID existing = hologramEntities.get(data.key());
        ArmorStand stand = null;
        if (existing != null) {
            Entity e = Bukkit.getEntity(existing);
            if (e instanceof ArmorStand && e.isValid()) stand = (ArmorStand) e;
            else hologramEntities.remove(data.key());
        }
        if (stand == null) {
            stand = w.spawn(loc, ArmorStand.class, as -> {
                as.setVisible(false); as.setGravity(false); as.setMarker(true); as.setSmall(true);
                as.setCustomNameVisible(true); as.setInvulnerable(true); as.setBasePlate(false); as.setArms(false);
            });
            hologramEntities.put(data.key(), stand.getUniqueId());
        } else stand.teleport(loc);
        String text = data.customName.isEmpty() ? hologramsFormatNoName : hologramsFormat;
        text = text.replace("%name%", data.customName).replace("%id%", String.valueOf(data.id));
        if (!hologramsShowId) text = text.replace("ID: " + data.id, "").trim();
        if (!hologramsShowName && data.customName.isEmpty()) text = text.replace("%name%", "").trim();
        stand.setCustomName(color(data.hologramColor + ChatColor.stripColor(color(text))));
    }

    private void removeHologram(BlockData data) {
        UUID id = hologramEntities.remove(data.key());
        if (id != null) { Entity e = Bukkit.getEntity(id); if (e != null) e.remove(); }
    }

    private void startViewSession(Player player, String mode) {
        stopViewSession(player);
        long endTime = System.currentTimeMillis() + (viewDurationTicks * 50L);
        final int[] counter = {0};
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override public void run() {
                if (!player.isOnline()) { cancel(); viewSessions.remove(player.getUniqueId()); return; }
                if (System.currentTimeMillis() >= endTime) { cancel(); viewSessions.remove(player.getUniqueId()); sendTeleporterMessage(player, msgViewDisabled, msgViewDisabledType); return; }
                counter[0]++;
                if (counter[0] % Math.max(1, viewUpdateTicks) != 0) return;
                for (BlockData data : blockDataMap.values()) {
                    if (!matchesViewMode(player, data, mode)) continue;
                    World w = Bukkit.getWorld(data.world);
                    if (w == null) continue;
                    if (!w.getName().equals(player.getWorld().getName())) continue;
                    Location loc = new Location(w, data.x + 0.5, data.y + 1.2, data.z + 0.5);
                    if (loc.distanceSquared(player.getLocation()) > (double) viewMaxDistance * viewMaxDistance) continue;
                    try {
                        player.spawnParticle(viewParticleType, loc, viewParticleCount, 0.2, 0.2, 0.2, 0.01);
                        player.spawnParticle(particleType, loc, 2, 0.3, 0.3, 0.3, 0.01);
                    } catch (Exception ignored) {}
                }
            }
        };
        int taskId = runnable.runTaskTimer(this, 0L, 1L).getTaskId();
        viewSessions.put(player.getUniqueId(), new ViewSession(mode, endTime, taskId));
        sendTeleporterMessage(player, msgViewEnabled.replace("%mode%", mode), msgViewEnabledType);
    }

    private void stopViewSession(Player player) {
        ViewSession session = viewSessions.remove(player.getUniqueId());
        if (session != null) Bukkit.getScheduler().cancelTask(session.taskId);
    }

    private boolean matchesViewMode(Player player, BlockData data, String mode) {
        UUID uuid = player.getUniqueId();
        if (mode.equalsIgnoreCase("all")) {
            if (checkPermission && !player.hasPermission(viewPermAll)) return false;
            return true;
        }
        if (mode.equalsIgnoreCase("owner")) {
            if (checkPermission && !player.hasPermission(viewPermOwner)) return false;
            return data.owner.equals(uuid);
        }
        if (mode.equalsIgnoreCase("member")) {
            if (checkPermission && !player.hasPermission(viewPermMember)) return false;
            if (data.owner.equals(uuid)) return true;
            if (data.members.contains(uuid)) return true;
            if (data.owners.contains(uuid)) return true;
            Set<UUID> gm = globalMembers.get(data.owner);
            if (gm != null && gm.contains(uuid)) return true;
            Set<UUID> go = globalOwners.get(data.owner);
            if (go != null && go.contains(uuid)) return true;
            return false;
        }
        return false;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        chatSessions.remove(uuid);
        openGuis.remove(uuid);
        guiContexts.remove(uuid);
        recentInteractions.remove(uuid);
        pendingPearlCancel.remove(uuid);
        stopViewSession(event.getPlayer());
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
            sender.sendMessage(color("&#FF5300/elevator view <all|owner|member>"));
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("reload")) {
            if (!sender.hasPermission("elevator.reload")) { sender.sendMessage(color(msgNoPermission)); return true; }
            loadConfig(); loadBlocks();
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
            player.sendMessage(color("&#FF5300Name: &f" + (data.customName.isEmpty() ? "-" : data.customName)));
            player.sendMessage(color("&#FF5300Owner: &f" + Bukkit.getOfflinePlayer(data.owner).getName()));
            player.sendMessage(color("&#FF5300Click: &f" + data.clickType.name()));
            player.sendMessage(color("&#FF5300Require sneak: &f" + data.requireSneak));
            player.sendMessage(color("&#FF5300Require item: &f" + data.requireItem + " (" + data.requiredItemName + ")"));
            player.sendMessage(color("&#FF5300Location: &f" + data.teleportLocation.name()));
            player.sendMessage(color("&#FF5300Teleport access: &f" + data.teleportAccess.name()));
            player.sendMessage(color("&#FF5300Manage access: &f" + data.manageAccess.name()));
            player.sendMessage(color("&#FF5300Break access: &f" + data.breakAccess.name()));
            player.sendMessage(color("&#FF5300Hologram: &f" + (data.hologramEnabled ? "Yes" : "No") + " (" + data.hologramColor + ")"));
            return true;
        }
        if (sub.equals("list")) {
            List<String> keys = playerBlocks.get(player.getUniqueId());
            if (keys == null || keys.isEmpty()) { player.sendMessage(color(msgNoBlocksOwned)); return true; }
            player.sendMessage(color(msgYourBlocks));
            for (String k : keys) {
                BlockData data = blockDataMap.get(k);
                if (data == null) continue;
                player.sendMessage(color("&#FF5300ID " + data.id + " &7-> &f" + k + (data.customName.isEmpty() ? "" : " &7(" + data.customName + ")")));
            }
            return true;
        }
        if (sub.equals("remove")) {
            if (args.length < 2) { player.sendMessage(color(msgRemoveUsage)); return true; }
            int id;
            try { id = Integer.parseInt(args[1]); } catch (NumberFormatException e) { player.sendMessage(color(msgRemoveInvalid)); return true; }
            Set<String> keys = idIndex.get(id);
            if (keys == null || keys.isEmpty()) { player.sendMessage(color(msgRemoveNone)); return true; }
            List<String> toRemove = new ArrayList<>();
            for (String k : keys) { BlockData data = blockDataMap.get(k); if (data != null && data.owner.equals(player.getUniqueId())) toRemove.add(k); }
            if (toRemove.isEmpty()) { player.sendMessage(color(msgRemoveNotOwner)); return true; }
            for (String k : toRemove) { BlockData data = blockDataMap.get(k); if (data != null) removeBlock(data); }
            player.sendMessage(color(msgRemoveSuccess.replace("%count%", String.valueOf(toRemove.size())).replace("%id%", String.valueOf(id))));
            return true;
        }
        if (sub.equals("view")) {
            if (args.length < 2) { stopViewSession(player); sendTeleporterMessage(player, msgViewDisabled, msgViewDisabledType); return true; }
            String mode = args[1].toLowerCase(Locale.ROOT);
            if (!mode.equals("all") && !mode.equals("owner") && !mode.equals("member")) { sendTeleporterMessage(player, msgViewUsage, msgViewUsageType); return true; }
            startViewSession(player, mode);
            return true;
        }
        player.sendMessage(color(msgUnknownSub));
        return true;
    }
}
