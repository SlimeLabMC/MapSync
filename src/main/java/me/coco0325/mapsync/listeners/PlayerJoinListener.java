package me.coco0325.mapsync.listeners;

import me.coco0325.mapsync.utils.MapUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        MapUtils.initMap(e.getPlayer().getInventory().getItemInMainHand());
        MapUtils.initMap(e.getPlayer().getInventory().getItemInOffHand());
        e.getPlayer().updateInventory();
    }
}
