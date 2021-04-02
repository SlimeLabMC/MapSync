package me.coco0325.mapsync.listeners;

import me.coco0325.mapsync.MapSync;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class CraftingCopyListener implements Listener {

    MapSync plugin;
    public CraftingCopyListener(MapSync plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onCopy(PrepareItemCraftEvent e){
        if(e.getInventory().getResult() != null && e.getInventory().getResult().getType() == Material.FILLED_MAP){
            ItemStack result = e.getInventory().getResult();
            if(!plugin.getUtils().canCopy(result)){
                e.getInventory().setResult(new ItemStack(Material.AIR));
                for(HumanEntity p : e.getViewers()){
                    p.sendMessage(plugin.CANNOT_COPY);
                }
            }
        }
    }
}
