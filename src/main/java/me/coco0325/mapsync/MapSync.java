package me.coco0325.mapsync;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin;
import io.github.thebusybiscuit.slimefun4.implementation.setup.DefaultCategories;
import io.github.thebusybiscuit.slimefun4.implementation.setup.SlimefunItemSetup;
import io.github.thebusybiscuit.slimefun4.utils.itemstack.ColoredFireworkStar;
import me.coco0325.mapsync.datastore.DatabaseManager;
import me.coco0325.mapsync.datastore.MapDataManager;
import me.coco0325.mapsync.hook.GriefPreventionHook;
import me.coco0325.mapsync.items.MapRune;
import me.coco0325.mapsync.listeners.CartographyTableListener;
import me.coco0325.mapsync.listeners.CraftingCopyListener;
import me.coco0325.mapsync.listeners.MapInitListener;
import me.coco0325.mapsync.listeners.MapRenderListener;
import me.mrCookieSlime.Slimefun.Lists.RecipeType;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.Slimefun;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import me.mrCookieSlime.Slimefun.cscorelib2.item.CustomItem;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public final class MapSync extends JavaPlugin implements SlimefunAddon {

    public DatabaseManager databaseManager;
    public MapDataManager mapDataManager;
    public FileConfiguration dbconfig, config, mapdata;
    public ArrayList<String> MAP_LORE;
    public String COPYRIGHT_ENABLED_LORE, COPYRIGHT_DISABLED_LORE, ALREADY_SYNC, SUCCESS_SYNC,
            COPYRIGHT_ENABLED, COPYRIGHT_DISABLED, CANNOT_COPY, CANNOT_ZOOM, NOT_A_SYNCMAP, NOT_AUTHOR,
            NO_PERMISSION, HOLD_A_MAP;
    public GriefPreventionHook griefPreventionHook = null;
    public String servername;
    public boolean copyright;
    public static MapSync instance;
    public final SlimefunItemStack MAP_RUNE = new SlimefunItemStack("ANCIENT_RUNE_MAP", new ColoredFireworkStar(Color.fromRGB(111, 83, 77), "&7遠古魔法符文 &8&l[&6&l圖&8&l]", "&e將此符文丟至一掉落的跨分流地圖旁", "&6封印該地圖 使其無法再被拷印"));

    @Override
    public void onEnable() {
        instance = this;
        setup();
        createTable();
    }

    @Override
    public void onDisable() {
        try {
            saveAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setup() {

        try{
            Properties props = new Properties();
            props.load(new FileInputStream(this.getServer().getWorldContainer().getAbsolutePath() + File.separator + "server.properties"));
            servername = props.getProperty("server-name");
        }catch (Exception e){
            e.printStackTrace();
            this.getPluginLoader().disablePlugin(this);
        }

        saveResource("config.yml", false);
        saveResource("database.yml", false);
        saveResource("mapdata.yml", false);
        File datafolder = new File(getDataFolder(), "data");
        if(!datafolder.exists()) datafolder.mkdirs();

        config = YamlConfiguration.loadConfiguration(new File(getDataFolder()+File.separator+"config.yml"));
        dbconfig = YamlConfiguration.loadConfiguration(new File(getDataFolder()+File.separator+"database.yml"));
        mapdata = YamlConfiguration.loadConfiguration(new File(getDataFolder()+File.separator+"mapdata.yml"));
        MAP_LORE = (ArrayList<String>) getConfig().getStringList("mapitem.lore");
        COPYRIGHT_DISABLED_LORE = ChatColor.translateAlternateColorCodes('&', getConfig().getString("mapitem.copyright.disabled"));
        COPYRIGHT_ENABLED_LORE = ChatColor.translateAlternateColorCodes('&', getConfig().getString("mapitem.copyright.enabled"));
        ALREADY_SYNC = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message.already-sync"));
        SUCCESS_SYNC = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message.success-sync"));
        COPYRIGHT_ENABLED = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message.enable-copyright"));
        COPYRIGHT_DISABLED = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message.disable-copyright"));
        CANNOT_COPY = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message.can-not-copy"));
        CANNOT_ZOOM = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message.can-not-zoom"));
        NOT_A_SYNCMAP = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message.not-a-syncmap"));
        NOT_AUTHOR = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message.not-author"));
        NO_PERMISSION = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message.no-permission"));
        HOLD_A_MAP = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message.hold-a-map"));

        databaseManager = new DatabaseManager(this);
        mapDataManager = new MapDataManager(this);

        if(getConfig().getBoolean("auto-sync")){
            Bukkit.getPluginManager().registerEvents(new CartographyTableListener(this), this);
        }

        copyright = getConfig().getBoolean("copyright");
        if(copyright){
            Bukkit.getPluginManager().registerEvents(new CraftingCopyListener(this), this);
        }

        Bukkit.getPluginManager().registerEvents(new MapRenderListener(this), this);

        if(getConfig().getBoolean("hooks.griefprevention") && getServer().getPluginManager().isPluginEnabled("GriefPrevention")){
            griefPreventionHook = new GriefPreventionHook();
            Bukkit.getPluginManager().registerEvents(new MapInitListener(this), this);
        }
    }

    public void slimefunAddonSetup() {
        Category category = SlimefunItemSetup.categories.magicalResources;
        new MapRune(category, MAP_RUNE, RecipeType.ANCIENT_ALTAR,
                new ItemStack[]{new ItemStack(Material.IRON_BARS), SlimefunItems.MAGIC_LUMP_2, new ItemStack(Material.IRON_BARS), new ItemStack(Material.MAP), SlimefunItems.BLANK_RUNE, new ItemStack(Material.MAP), new ItemStack(Material.IRON_BARS), SlimefunItems.MAGIC_LUMP_2, new ItemStack(Material.IRON_BARS)},
                new SlimefunItemStack(MAP_RUNE, 4))
                .register(this);
    }

    public void saveAll() throws IOException {
        mapDataManager.saveMapSet();
        mapdata.save(new File(getDataFolder()+File.separator+"mapdata.yml"));
    }

    public void createTable(){
        try {
            Connection connection = databaseManager.getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS MapSync (" +
                    "uuid BIGINT, map BLOB, primary key(uuid))");
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMapdata() {
        return mapdata;
    }

    public MapDataManager getMapDataManager(){
        return mapDataManager;
    }

    public DatabaseManager getDatabaseManager(){
        return databaseManager;
    }

    public String getServername(){
        return servername;
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        return null;
    }
}
