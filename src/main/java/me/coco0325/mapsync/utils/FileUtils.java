package me.coco0325.mapsync.utils;

import me.coco0325.mapsync.MapSync;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FileUtils {

    static MapSync plugin = MapSync.instance;

    public static byte[] compress(byte[] bytes){
        try{
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(bytes.length);
            try (byteStream) {
                try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream)) {
                    zipStream.write(bytes);
                }
            }

            return byteStream.toByteArray();
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decompress(byte[] bytes){
        try {
            int nRead;
            byte[] data = new byte[2048];
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            GZIPInputStream gzip = new GZIPInputStream(bis);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            while ((nRead = gzip.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            bis.close();
            gzip.close();
            return buffer.toByteArray();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static void toByteArray(Long uuid, Consumer<byte[]> callback){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                callback.accept(decompress(Files.readAllBytes(Paths.get(getDataPath(uuid)))));
            } catch (Exception e){
                callback.accept(null);
                e.printStackTrace();
            }
        });
    }

    public static void writeFilefromByteArray(byte[] bytes, Long uuid, Integer rawid){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (FileOutputStream stream = new FileOutputStream(FileUtils.getDataPath(uuid))) {
                stream.write(Objects.requireNonNull(bytes));
                stream.flush();
                Bukkit.getScheduler().runTask(plugin, () -> plugin.getMapDataManager().getMapMap().put(uuid, rawid));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static String getDataPath(Long uuid){
        return plugin.getDataFolder() +
                File.separator + "data" + File.separator + uuid + ".bin";
    }
}
