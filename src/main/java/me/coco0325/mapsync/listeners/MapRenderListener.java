package me.coco0325.mapsync.listeners;

import me.coco0325.mapsync.MapSync;
import me.coco0325.mapsync.utils.MapUtils;
import org.bukkit.Bukkit;
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

import java.util.Optional;

public class MapRenderListener implements Listener {

    MapSync plugin;

    public MapRenderListener(MapSync plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e){
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Entity entity : e.getChunk().getEntities()) {
                if (entity instanceof ItemFrame) {
                    MapUtils.renderMap(((ItemFrame) entity).getItem(), Optional.of(((ItemFrame) entity)));
                }
            }
        }, 5L);
    }

    @EventHandler
    public void onPlayerInv(PlayerItemHeldEvent e){
        ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());
        MapUtils.renderMap(item, Optional.empty());
    }

    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent e){
        if(!(e.getEntity() instanceof HumanEntity)){
            return;
        }
        MapUtils.renderMap(e.getItem().getItemStack(), Optional.empty());
    }

    @EventHandler
    public void onPlayerInventoryPlace(InventoryClickEvent e) {
        MapUtils.renderMap(e.getCurrentItem(), Optional.empty());
        MapUtils.renderMap(e.getCursor(), Optional.empty());
    }

}
