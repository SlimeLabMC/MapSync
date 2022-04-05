package me.coco0325.mapsync.listeners;

import me.coco0325.mapsync.utils.MapUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        MapUtils.renderMap(e.getPlayer().getInventory().getItemInMainHand(), Optional.empty());
        MapUtils.renderMap(e.getPlayer().getInventory().getItemInOffHand(), Optional.empty());
        e.getPlayer().updateInventory();
    }
}
