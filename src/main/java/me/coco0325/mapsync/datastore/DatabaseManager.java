package me.coco0325.mapsync.datastore;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.coco0325.mapsync.MapSync;
import me.coco0325.mapsync.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public class DatabaseManager {
    
    private static DataSource source;
    public MapSync plugin;
    
    public DatabaseManager(MapSync plugin){
        this.plugin = plugin;
        FileConfiguration DBFile = plugin.dbconfig;
        HikariConfig config = new HikariConfig();
        String host = DBFile.getString("host");
        String port = DBFile.getString("port");
        String database = DBFile.getString("database");
        String username = DBFile.getString("username");
        String password = DBFile.getString("password");
        int minsize = DBFile.getInt("min_size");
        int maxsize = DBFile.getInt("max_size");
        boolean SSL = DBFile.getBoolean("use_SSL");
        String jdbc = "jdbc:mysql://" + host + ":" + port + "/" + database + "?" + "useSSL=" + SSL;
        config.setJdbcUrl(jdbc);
        config.setMaximumPoolSize(maxsize);
        config.setMinimumIdle(minsize);
        config.setUsername(username);
        config.setPassword(password);

        source = new HikariDataSource(config);
        try {
            source.setLoginTimeout(5);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return source.getConnection();
    }

    public void storeMapData(Long uuid, Integer rawid, byte[] data){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try{
                byte[] compressed = FileUtils.compress(data);
                FileUtils.writeFilefromByteArray(compressed, uuid, rawid);
                Connection connection = getConnection();
                PreparedStatement stmt = connection.prepareStatement("INSERT INTO MapSync (uuid, map) VALUES(?, ?)");
                stmt.setLong(1, uuid);
                stmt.setBytes(2, compressed);
                stmt.execute();
                connection.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    public void fetchMapData(Long uuid, Integer rawid,  Consumer<byte[]> callback){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT map FROM MapSync WHERE uuid = ? LIMIT 1");
                statement.setLong(1, uuid);
                ResultSet resultSet = statement.executeQuery();
                if(resultSet.next()){
                    byte[] rawmap = resultSet.getBytes("map");
                    if(rawmap != null) FileUtils.writeFilefromByteArray(rawmap, uuid, rawid);
                    callback.accept(FileUtils.decompress(rawmap));
                }
                connection.close();
            } catch (SQLException throwables) {
                callback.accept(null);
                throwables.printStackTrace();

            }
        });
    }
}
