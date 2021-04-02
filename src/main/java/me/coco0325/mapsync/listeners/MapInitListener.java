package me.coco0325.mapsync.listeners;

import me.coco0325.mapsync.MapSync;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class MapInitListener implements Listener {

    MapSync plugin;

    public MapInitListener(MapSync plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMapOpen(PlayerInteractEvent e){
        if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(e.getItem() != null && e.getItem().getType() == Material.MAP && !plugin.griefPreventionHook.canCreateMap(e.getPlayer())) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(plugin.NO_PERMISSION);
            }
        }
    }
}
