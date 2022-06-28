package me.coco0325.mapsync.listeners;

import me.coco0325.mapsync.MapSync;
import me.coco0325.mapsync.utils.MapUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.CartographyInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;

public class CartographyTableListener implements Listener {

    MapSync plugin;
    public CartographyTableListener(MapSync plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLock(InventoryClickEvent e){
        if(e.getClickedInventory() instanceof CartographyInventory && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.FILLED_MAP && e.getSlot() == 2 &&
                e.getClickedInventory().getItem(1) != null){
            Player player = (Player) e.getWhoClicked();
            ItemStack map = e.getClickedInventory().getItem(0);
            MapMeta mapMeta = (MapMeta) map.getItemMeta();
            MapView mapView = mapMeta.getMapView();

            switch (e.getClickedInventory().getItem(1).getType()) {
                case GLASS_PANE:
                    if(!player.hasPermission("mapsync.use")) return;

                    if(MapUtils.hasUUID(mapMeta)){
                        e.setCancelled(true);
                        e.getCurrentItem().setType(Material.AIR);
                        return;
                    }else if(mapMeta.getPersistentDataContainer().has(MapUtils.server, PersistentDataType.STRING) && !plugin.getServername().equals(mapMeta.getPersistentDataContainer().get(MapUtils.server, PersistentDataType.STRING))){
                        e.setCancelled(true);
                        e.getCurrentItem().setType(Material.AIR);
                        return;
                    }

                    Long uuid = MapUtils.generateUUID(player);

                    ItemStack lockedMap = e.getCurrentItem();
                    MapMeta lockedMapMeta = (MapMeta) lockedMap.getItemMeta();

                    MapView mapView1 = Bukkit.createMap(Bukkit.getWorlds().get(0));
                    int rawid = mapView1.getId();
                    lockedMapMeta.setMapView(mapView1);

                    lockedMapMeta.getMapView().addRenderer(new MapRenderer() {
                        @Override
                        public void render(MapView map, MapCanvas canvas, Player player) {
                            for(int i=0; i<128; i++){
                                for(int j=0; j<128; j++){
                                    canvas.setPixel(i, j, MapPalette.TRANSPARENT);
                                }
                            }
                        }
                    }); // Avoid being rendered
                    lockedMap.setItemMeta(lockedMapMeta);
                    MapUtils.applyUUID(lockedMap, uuid, player);

                    try{
                        plugin.getDatabaseManager().storeMapData(uuid, rawid, MapUtils.getMapPixels(mapView));
                        player.sendMessage(plugin.SUCCESS_SYNC);
                    }catch (Exception exception){
                        exception.printStackTrace();
                    }
                    break;
                case MAP:
                    if(plugin.copyright && !MapUtils.canCopy(map)){
                        e.setCancelled(true);
                        e.getCurrentItem().setType(Material.AIR);
                        player.sendMessage(plugin.CANNOT_COPY);
                    }
                    break;
                case PAPER:
                    MapMeta meta = (MapMeta) e.getCurrentItem().getItemMeta();
                    if(meta.getPersistentDataContainer().has(MapUtils.server, PersistentDataType.STRING)){
                        if(plugin.getServername().equals(meta.getPersistentDataContainer().get(MapUtils.server, PersistentDataType.STRING)) && meta.getPersistentDataContainer().has(MapUtils.rawid, PersistentDataType.INTEGER)){
                            meta.getPersistentDataContainer().remove(MapUtils.rawid);
                        }else{
                            e.setCancelled(true);
                            e.getCurrentItem().setType(Material.AIR);
                        }
                    }else{
                        meta.getPersistentDataContainer().set(MapUtils.server, PersistentDataType.STRING, plugin.getServername());
                    }
                    e.getCurrentItem().setItemMeta(meta);
                    break;
                default:
            }
        }
    }
}
