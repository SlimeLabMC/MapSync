package me.coco0325.mapsync.listeners;

import me.coco0325.mapsync.utils.MapUtils;
import net.craftersland.data.bridge.api.events.SyncCompleteEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

public class SyncCompleteListener implements Listener {

    @EventHandler
    public void onPlayerJoin(SyncCompleteEvent e){
        MapUtils.renderMap(e.getPlayer().getInventory().getItemInMainHand(), Optional.empty());
        MapUtils.renderMap(e.getPlayer().getInventory().getItemInOffHand(), Optional.empty());
        e.getPlayer().updateInventory();
    }
}
