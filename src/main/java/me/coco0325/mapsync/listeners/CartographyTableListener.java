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
                    }

                    Long uuid = MapUtils.generateUUID(player);

                    ItemStack lockedMap = e.getCurrentItem();
                    MapMeta lockedMapMeta = (MapMeta) lockedMap.getItemMeta();

                    int rawid = Bukkit.createMap(Bukkit.getWorlds().get(0)).getId();
                    lockedMapMeta.setMapId(rawid);

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
                case MAP:
                    if(plugin.copyright && !MapUtils.canCopy(map)){
                        e.setCancelled(true);
                        e.getCurrentItem().setType(Material.AIR);
                        player.sendMessage(plugin.CANNOT_COPY);
                    }
            }
        }
    }
}
