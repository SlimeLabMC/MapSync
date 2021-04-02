package me.coco0325.mapsync.utils;

import me.coco0325.mapsync.MapSync;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.logging.Level;

public class MapUtils {

    MapSync plugin;
    NamespacedKey idkey, copyright, author;

    public MapUtils(MapSync plugin){
        this.plugin = plugin;
        idkey = new NamespacedKey(plugin, "mapid");
        copyright = new NamespacedKey(plugin, "copy");
        author = new NamespacedKey(plugin, "author");
    }

    public boolean hasUUID(MapMeta map){
        return map.getPersistentDataContainer().has(idkey, PersistentDataType.LONG);
    }

    public Long getUUID(MapMeta map){
        return map.getPersistentDataContainer().get(idkey, PersistentDataType.LONG);
    }

    public void applyUUID(ItemStack itemStack, Long uuid, Player player){
        ItemMeta meta = itemStack.getItemMeta();
        ArrayList<String> lore = meta.hasLore() ? (ArrayList<String>) meta.getLore() : new ArrayList<>();
        for(String text : plugin.MAP_LORE){
            lore.add(ChatColor.translateAlternateColorCodes('&', text.replace("%UUID%", uuid.toString()).replace("%AUTHOR%", player.getName())));
        }
        lore.add(plugin.COPYRIGHT_DISABLED_LORE);
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(idkey, PersistentDataType.LONG, uuid);
        meta.getPersistentDataContainer().set(copyright, PersistentDataType.BYTE, (byte)0);
        meta.getPersistentDataContainer().set(author, PersistentDataType.STRING, player.getUniqueId().toString());
        itemStack.setItemMeta(meta);
    }

    public long generateUUID(Player player){
        return (System.currentTimeMillis() / 10) * 1000000 + (player.getUniqueId().hashCode() % 1000) * 1000 + ThreadLocalRandom.current().nextInt(0, 1000);
    }

    public void toByteArray(Long uuid, Consumer<byte[]> callback){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                callback.accept(Files.readAllBytes(Paths.get(getDataPath(uuid))));
            } catch (Exception e){
                callback.accept(null);
                e.printStackTrace();
            }
        });
    }

    public void writeFilefromByteArray(byte[] bytes, String path){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (FileOutputStream stream = new FileOutputStream(path)) {
                stream.write(bytes);
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void render(ItemStack itemStack, byte[] bytes){
        MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
        for(MapRenderer mapRenderer : mapMeta.getMapView().getRenderers()){
            mapMeta.getMapView().removeRenderer(mapRenderer);
        }
        mapMeta.getMapView().addRenderer(new MapRenderer() {
            @Override
            public void render(MapView map, MapCanvas canvas, Player player) {
                for(int i=0; i<128; i++){
                    for(int j=0; j<128; j++){
                        canvas.setPixel(i, j, bytes[j*128+i]);
                    }
                }
            }
        });
        itemStack.setItemMeta(mapMeta);
    }

    public boolean isHandled(MapMeta mapMeta){
        return mapMeta.getMapView().isVirtual();
    }

    public void renderMap(ItemStack item) {
        MapMeta mapMeta = (MapMeta) item.getItemMeta();
        if(isHandled(mapMeta)) return;
        if(plugin.getUtils().hasUUID(mapMeta)){
            Long uuid = plugin.getUtils().getUUID(mapMeta);
            if(plugin.getMapDataManager().isLocal(uuid)){
                toByteArray(uuid, (bytes) -> Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        if(bytes == null) {
                            plugin.getMapDataManager().getMapSet().remove(uuid);
                            renderMap(item);
                        }
                        plugin.getLogger().log(Level.INFO, "Loaded map from local storage.");
                        render(item, bytes);
                        //setMapPixels(bytes, mapMeta.getMapView());
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }));
            }else{
                plugin.getDatabaseManager().fetchMapData(uuid, (bytes) -> Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getMapDataManager().getMapSet().add(uuid);
                    try {
                        plugin.getLogger().log(Level.INFO, "Downloaded map from database.");
                        render(item, bytes);
                        //setMapPixels(bytes, mapMeta.getMapView());
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }));
            }
        }
    }

    public byte[] getMapPixels(MapView view) throws Exception{
        String s = "map_" + view.getId();
        Object craftworld = getCBTClass().cast(Bukkit.getServer().getWorlds().get(0));
        Object world = craftworld.getClass().getMethod("getHandle").invoke(craftworld);
        Object worldmap = world.getClass().getDeclaredMethod("a", String.class).invoke(world, s);
        return (byte[]) worldmap.getClass().getDeclaredField("colors").get(worldmap);
    }

    private Class<?> getCBTClass() throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "org.bukkit.craftbukkit." + version + "CraftWorld";
        return Class.forName(name);
    }

    public String getDataPath(Long uuid){
        return plugin.getDataFolder() +
                File.separator + "data" + File.separator + uuid + ".bin";
    }

    public void switchCopyright(ItemStack item, Player player){
        if(canCopy(item)){
            item.getItemMeta().getPersistentDataContainer().set(copyright, PersistentDataType.BYTE, (byte)0);
            replaceLore(item, plugin.COPYRIGHT_DISABLED_LORE, plugin.COPYRIGHT_ENABLED_LORE);
            player.sendMessage(plugin.COPYRIGHT_DISABLED);
        }else{
            item.getItemMeta().getPersistentDataContainer().set(copyright, PersistentDataType.BYTE, (byte)1);
            replaceLore(item, plugin.COPYRIGHT_ENABLED_LORE, plugin.COPYRIGHT_DISABLED_LORE);
            player.sendMessage(plugin.COPYRIGHT_ENABLED);
        }
    }

    public void replaceLore(ItemStack item, String toReplace, String ReplaceFor){
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = (ArrayList<String>) meta.getLore();
        for(int i=0; i<lore.size(); i++){
            if(lore.get(i).equals(toReplace)){
                lore.set(i, ReplaceFor);
                meta.setLore(lore);
                item.setItemMeta(meta);
                return;
            }
        }
        lore.add(ReplaceFor);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public boolean canCopy(ItemStack item){
        if(item.getItemMeta().getPersistentDataContainer().has(copyright, PersistentDataType.BYTE)){
            return item.getItemMeta().getPersistentDataContainer().get(copyright, PersistentDataType.BYTE) != (byte) 1;
        }
        return true;
    }

    public String getAuthor(ItemStack item){
        return item.getItemMeta().getPersistentDataContainer().get(author, PersistentDataType.STRING);
    }
}
