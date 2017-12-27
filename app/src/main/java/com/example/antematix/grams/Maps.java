package com.example.antematix.grams;

/**
 * Created by Paolo on 3/3/2017.
 */

public class Maps {
    private String mapName, mapImage;
    int mapId;

    public Maps(String mapName, String mapImage,int mapId){
        this.mapName = mapName;
        this.mapImage = mapImage;
        this.mapId = mapId;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getMapImage() {
        return mapImage;
    }

    public void setMapImage(String mapImage) {
        this.mapImage = mapImage;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }
}
