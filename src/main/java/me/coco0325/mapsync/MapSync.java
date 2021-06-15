package me.coco0325.mapsync;

import me.coco0325.mapsync.datastore.DatabaseManager;
import me.coco0325.mapsync.datastore.MapDataManager;
import me.coco0325.mapsync.hook.GriefPreventionHook;
import me.coco0325.mapsync.listeners.CartographyTableListener;
import me.coco0325.mapsync.listeners.MapInitListener;
import me.coco0325.mapsync.listeners.MapRenderListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public final class MapSync extends JavaPlugin{

    public DatabaseManager databaseManager;
    public MapDataManager mapDataManager;
    public FileConfiguration dbconfig, config, mapdata;
    public ArrayList<String> MAP_LORE;
    public String ALREADY_SYNC;
    public String SUCCESS_SYNC;
    public String CANNOT_COPY;
    public String NO_PERMISSION;
    public GriefPreventionHook griefPreventionHook = null;
    public String servername;
    public boolean copyright;
    public static MapSync instance;

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
        ALREADY_SYNC = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message.already-sync"));
        SUCCESS_SYNC = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message.success-sync"));
        NO_PERMISSION = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message.no-permission"));

        databaseManager = new DatabaseManager(this);
        mapDataManager = new MapDataManager(this);

        Bukkit.getPluginManager().registerEvents(new CartographyTableListener(this), this);

        Bukkit.getPluginManager().registerEvents(new MapRenderListener(this), this);

        if(getConfig().getBoolean("hooks.griefprevention") && getServer().getPluginManager().isPluginEnabled("GriefPrevention")){
            griefPreventionHook = new GriefPreventionHook();
            Bukkit.getPluginManager().registerEvents(new MapInitListener(this), this);
        }
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
}
