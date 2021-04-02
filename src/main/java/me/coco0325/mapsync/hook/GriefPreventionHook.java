package me.coco0325.mapsync.hook;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GriefPreventionHook {

    public boolean canCreateMap(Player player){
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);
        if(claim == null) return true;
        return claim.allowBuild(player, Material.AIR) == null;
    }

}
