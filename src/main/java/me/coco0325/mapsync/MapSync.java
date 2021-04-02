package me.coco0325.mapsync;

import me.coco0325.mapsync.commands.Command;
import me.coco0325.mapsync.datastore.DatabaseManager;
import me.coco0325.mapsync.datastore.MapDataManager;
import me.coco0325.mapsync.listeners.CartographyTableListener;
import me.coco0325.mapsync.listeners.MapRenderListener;
import me.coco0325.mapsync.utils.MapUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class MapSync extends JavaPlugin {

    public DatabaseManager databaseManager;
    public MapDataManager mapDataManager;
    public MapUtils mapUtils;
    public FileConfiguration dbconfig, config, mapdata;

    @Override
    public void onEnable() {
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
        saveResource("config.yml", false);
        saveResource("database.yml", false);
        saveResource("mapdata.yml", false);
        File datafolder = new File(getDataFolder(), "data");
        if(!datafolder.exists()) {
            datafolder.mkdirs();
        }
        config = YamlConfiguration.loadConfiguration(new File(getDataFolder()+File.separator+"config.yml"));
        dbconfig = YamlConfiguration.loadConfiguration(new File(getDataFolder()+File.separator+"database.yml"));
        mapdata = YamlConfiguration.loadConfiguration(new File(getDataFolder()+File.separator+"mapdata.yml"));
        this.getCommand("syncmap").setExecutor(new Command(this));
        mapUtils = new MapUtils(this);
        databaseManager = new DatabaseManager(this);
        mapDataManager = new MapDataManager(this);
        if(getConfig().getBoolean("auto-sync")){
            Bukkit.getPluginManager().registerEvents(new CartographyTableListener(this), this);
        }
        Bukkit.getPluginManager().registerEvents(new MapRenderListener(this), this);
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

    public MapUtils getUtils() {
        return mapUtils;
    }

    public MapDataManager getMapDataManager(){
        return mapDataManager;
    }

    public DatabaseManager getDatabaseManager(){
        return databaseManager;
    }

}
