package me.coco0325.mapsync.datastore;

import me.coco0325.mapsync.MapSync;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MapDataManager {

    MapSync plugin;
    HashMap<Long, Integer> maplist = new HashMap<>();

    public MapDataManager(MapSync plugin){
        this.plugin = plugin;
        for(String str : plugin.getMapdata().getStringList("data")){
            maplist.put(Long.parseLong(str.substring(0, 18)), Integer.parseInt(str.substring(18)));
        }
    }

    public void saveMapSet(){
        List<String> data = maplist.keySet().stream().map(uuid ->
                uuid.toString()+maplist.get(uuid).toString()).collect(Collectors.toList());
        plugin.getMapdata().set("data", data);
    }

    public HashMap<Long, Integer> getMapMap(){
        return maplist;
    }

    public Integer getLocalId(Long uuid){
        return maplist.get(uuid);
    }

    public boolean isLocal(Long uuid){
        return maplist.containsKey(uuid);
    }

}
