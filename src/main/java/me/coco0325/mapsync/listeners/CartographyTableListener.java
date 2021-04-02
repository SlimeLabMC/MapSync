package me.coco0325.mapsync.listeners;

import me.coco0325.mapsync.MapSync;
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
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class CartographyTableListener implements Listener {

    MapSync plugin;
    public CartographyTableListener(MapSync plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLock(InventoryClickEvent e){
        if(e.getClickedInventory() instanceof CartographyInventory && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.FILLED_MAP &&
                e.getClickedInventory().getItem(1) != null && e.getClickedInventory().getItem(1).getType() == Material.GLASS_PANE){

            Player player = (Player) e.getWhoClicked();
            if(!player.hasPermission("mapsync.use")) return;

            ItemStack map = e.getClickedInventory().getItem(0);
            MapMeta mapMeta = (MapMeta) map.getItemMeta();
            MapView mapView = mapMeta.getMapView();

            Long uuid = plugin.getUtils().generateUUID(player);

            ItemStack lockedMap = e.getCurrentItem();
            MapMeta lockedMapMeta = (MapMeta) lockedMap.getItemMeta();
            plugin.getUtils().applyUUID(lockedMap, uuid);
            plugin.getMapDataManager().getMapSet().add(uuid);

            lockedMapMeta.getMapView().addRenderer(new MapRenderer() {
                @Override
                public void render(MapView map, MapCanvas canvas, Player player) {
                }
            }); // Avoid being rendered
            lockedMap.setItemMeta(lockedMapMeta);

            try{
                plugin.getDatabaseManager().storeMapData(uuid, plugin.getUtils().getMapPixels(mapView));
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }
}
