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

    public void initMap(ItemStack item){
        if(item != null && item.getType() == Material.FILLED_MAP && item.getItemMeta() instanceof MapMeta){
            MapUtils.renderMap(item);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e){
        for(Entity entity : e.getChunk().getEntities()){
            if(entity instanceof ItemFrame){
                initMap(((ItemFrame) entity).getItem());
            }
        }
    }

    @EventHandler
    public void onPlayerInv(PlayerItemHeldEvent e){
        ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());
        initMap(item);
    }

    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent e){
        if(!(e.getEntity() instanceof HumanEntity)){
            return;
        }
        initMap(e.getItem().getItemStack());
    }

    @EventHandler
    public void onPlayerInventoryPlace(InventoryClickEvent e){
        switch(e.getAction()){
            case PLACE_ALL:
            case PLACE_ONE:
            case PLACE_SOME:
            case SWAP_WITH_CURSOR:
                initMap(e.getCursor());
                break;
            default:

        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        initMap(e.getPlayer().getInventory().getItemInMainHand());
        initMap(e.getPlayer().getInventory().getItemInOffHand());
        e.getPlayer().updateInventory();
    }
}
