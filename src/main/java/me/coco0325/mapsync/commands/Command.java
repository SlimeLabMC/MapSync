package me.coco0325.mapsync.commands;

import me.coco0325.mapsync.MapSync;
import me.coco0325.mapsync.utils.MapUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.Objects;

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
                    if(MapUtils.hasUUID(mapMeta)){
                        player.sendMessage(plugin.ALREADY_SYNC);
                        return true;
                    }

                    Long uuid = MapUtils.generateUUID(player);
                    Objects.requireNonNull(mapMeta.getMapView()).addRenderer(new MapRenderer() {
                        @Override
                        public void render(MapView map, MapCanvas canvas, Player player) {
                        }
                    }); // Avoid being rendered
                    item.setItemMeta(mapMeta);
                    MapUtils.applyUUID(item, uuid, player);
                    try{
                        plugin.getDatabaseManager().storeMapData(uuid, MapUtils.getMapPixels(mapMeta.getMapView()));
                        player.sendMessage(plugin.SUCCESS_SYNC);
                    }catch (Exception exception){
                        exception.printStackTrace();
                    }
                }
                return true;
            }
        }else if(command.getName().equals("copyright")){

            if(sender instanceof Player && sender.hasPermission("mapsync.copyright")){
                Player player = (Player) sender;
                ItemStack item = player.getInventory().getItemInMainHand();
                if(item.getType() == Material.FILLED_MAP){

                    MapMeta mapMeta = (MapMeta) item.getItemMeta();
                    if(!MapUtils.hasUUID(mapMeta)) {
                        player.sendMessage(plugin.NOT_A_SYNCMAP);
                        return true;
                    }

                    if(!MapUtils.getAuthor(item).equals(player.getUniqueId().toString())) {
                        player.sendMessage(plugin.NOT_AUTHOR);
                        return true;
                    }

                    MapUtils.switchCopyright(item, player);
                }else{
                    player.sendMessage(plugin.HOLD_A_MAP);
                }
            }
            return true;
        }
        return false;
    }
}
