package me.coco0325.mapsync.commands;

import me.coco0325.mapsync.MapSync;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class Command implements CommandExecutor {

    MapSync plugin;

    public Command(MapSync plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        if(command.getName().equals("syncmap")){

            if(sender instanceof Player && sender.hasPermission("mapsync.use")){
                Player player = (Player) sender;
                ItemStack item = player.getInventory().getItemInMainHand();
                if(item.getType() == Material.FILLED_MAP){

                    MapMeta mapMeta = (MapMeta) item.getItemMeta();
                    if(plugin.getUtils().hasUUID(mapMeta)){
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("message.already-sync")));
                        return true;
                    }

                    Long uuid = plugin.getUtils().generateUUID(player);
                    plugin.getMapDataManager().getMapSet().add(uuid);
                    mapMeta.getMapView().addRenderer(new MapRenderer() {
                        @Override
                        public void render(MapView map, MapCanvas canvas, Player player) {
                        }
                    }); // Avoid being rendered
                    item.setItemMeta(mapMeta);
                    plugin.getUtils().applyUUID(item, uuid);
                    try{
                        plugin.getDatabaseManager().storeMapData(uuid, plugin.getUtils().getMapPixels(mapMeta.getMapView()));
                    }catch (Exception exception){
                        exception.printStackTrace();
                    }
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("message.success")));
                }
                return true;
            }
        }
        return false;
    }
}
