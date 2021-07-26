package me.coco0325.mapsync.listeners;

import me.coco0325.mapsync.MapSync;
import me.coco0325.mapsync.utils.MapUtils;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

public class MapRenderListener implements Listener {

    MapSync plugin;

    public MapRenderListener(MapSync plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e){
        for(Entity entity : e.getChunk().getEntities()){
            if(entity instanceof ItemFrame){
                MapUtils.initMap(((ItemFrame) entity).getItem());
            }
        }
    }

    @EventHandler
    public void onPlayerInv(PlayerItemHeldEvent e){
        ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());
        MapUtils.initMap(item);
    }

    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent e){
        if(!(e.getEntity() instanceof HumanEntity)){
            return;
        }
        MapUtils.initMap(e.getItem().getItemStack());
    }

    @EventHandler
    public void onPlayerInventoryPlace(InventoryClickEvent e){
        MapUtils.initMap(e.getCurrentItem());
        MapUtils.initMap(e.getCursor());
    }
}
