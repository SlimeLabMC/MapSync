package me.coco0325.mapsync.listeners;

import me.coco0325.mapsync.MapSync;
import me.coco0325.mapsync.utils.MapUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;

import java.util.Optional;

public class MapRenderListener_1_17 implements Listener {

    MapSync plugin;

    public MapRenderListener_1_17(MapSync plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent e){
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Entity entity : e.getEntities()) {
                if (entity instanceof ItemFrame) {
                    MapUtils.renderMap(((ItemFrame) entity).getItem(), Optional.of(((ItemFrame) entity)));
                }
            }
        }, 5L);
    }

}
