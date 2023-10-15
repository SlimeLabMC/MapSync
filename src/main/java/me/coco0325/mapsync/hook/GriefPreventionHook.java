package me.coco0325.mapsync.hook;

import me.coco0325.mapsync.MapSync;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;

public class GriefPreventionHook {

    private static final MapSync plugin = MapSync.instance;

    public boolean canCreateMap(Player player){
        Location l = player.getLocation().clone();
        l.setY(100);
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(l, true, null);
        if(claim == null) {
            return true;
        }
        if (plugin.getConfig().getString("hooks.Permission_type") == null || Objects.equals(plugin.getConfig().getString("hooks.Permission_type"), "Admin")){
            return claim.allowGrantPermission(player) == null;
        }
        return claim.hasExplicitPermission(player, ClaimPermission.valueOf(plugin.getConfig().getString("hooks.Permission_type")));
    }
}
