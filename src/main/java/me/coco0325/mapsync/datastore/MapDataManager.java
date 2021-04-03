package me.coco0325.mapsync.datastore;

import me.coco0325.mapsync.MapSync;

import java.util.ArrayList;
import java.util.HashSet;

public class MapDataManager {

    MapSync plugin;
    HashSet<Long> maplist;

    public MapDataManager(MapSync plugin){
        this.plugin = plugin;
        maplist = new HashSet<>(plugin.getMapdata().getLongList("data"));
    }

    public void saveMapSet(){
        plugin.getMapdata().set("data", new ArrayList<>(maplist));
    }

    public HashSet<Long> getMapSet(){
        return maplist;
    }

    public boolean isLocal(Long uuid){
        return maplist.contains(uuid);
    }

}
