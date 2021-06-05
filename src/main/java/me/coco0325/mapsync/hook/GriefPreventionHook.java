package me.coco0325.mapsync.hook;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GriefPreventionHook {

    public boolean canCreateMap(Player player){
        Location l = player.getLocation().clone();
        l.setY(100);
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(l, true, null);
        if(claim == null) return true;
        return claim.allowGrantPermission(player) == null;
    }

}
