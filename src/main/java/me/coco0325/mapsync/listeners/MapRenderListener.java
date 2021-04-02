package me.coco0325.mapsync.listeners;

import me.coco0325.mapsync.MapSync;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

public class MapRenderListener implements Listener {

    MapSync plugin;

    public MapRenderListener(MapSync plugin){
        this.plugin = plugin;
    }

    public void initMap(ItemStack item){
        if(item != null && item.getType() == Material.FILLED_MAP && item.getItemMeta() instanceof MapMeta){
            plugin.getUtils().renderMap(item);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof ItemFrame) {
                initMap(((ItemFrame) entity).getItem());
            }
        }
    }

    @EventHandler
    public void onPlayerInv(PlayerItemHeldEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        initMap(item);
    }

    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof HumanEntity)) {
            return;
        }
        initMap(event.getItem().getItemStack());
    }

    @EventHandler
    public void onPlayerInventoryPlace(InventoryClickEvent event) {
        switch (event.getAction()) {
            case PLACE_ALL:
            case PLACE_ONE:
            case PLACE_SOME:
            case SWAP_WITH_CURSOR:
                initMap(event.getCursor());
                break;
            default:

        }
    }
}
