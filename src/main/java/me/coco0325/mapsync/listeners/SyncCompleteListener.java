package me.coco0325.mapsync.listeners;

import me.coco0325.mapsync.utils.MapUtils;
import net.craftersland.data.bridge.api.events.SyncCompleteEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SyncCompleteListener implements Listener {

    @EventHandler
    public void onPlayerJoin(SyncCompleteEvent e){
        MapUtils.initMap(e.getPlayer().getInventory().getItemInMainHand());
        MapUtils.initMap(e.getPlayer().getInventory().getItemInOffHand());
        e.getPlayer().updateInventory();
    }
}
