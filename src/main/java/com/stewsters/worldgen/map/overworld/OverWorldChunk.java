package com.stewsters.worldgen.map.overworld;

import com.stewsters.worldgen.game.Settlement;
import com.stewsters.worldgen.map.BiomeType;

import static com.stewsters.worldgen.map.BiomeType.RIVER;

public class OverWorldChunk {

    public static final int chunkSize = 64;

    public float[][] elevation;
    public float[][] temperature;
    public float[][] precipitation;
    public float[][] drainage;

    public int[][] regionIds;

    public float[][] windX;
    public float[][] windY;

    // TODO: sunlight - angle of terrain can reduce it.  It effects temp and plant types
    // TODO: Moisture should be generated from warm water.  It should create pressure and move to low pressure

    // The north sides of mountains may have a different biome then the south sides.

    public boolean[][] river;

    public Settlement[][] settlement;
    public boolean[][] road;

    public OverWorldChunk() {

        elevation = new float[chunkSize][chunkSize];
        temperature = new float[chunkSize][chunkSize];
        precipitation = new float[chunkSize][chunkSize];
        drainage = new float[chunkSize][chunkSize];

        regionIds = new int[chunkSize][chunkSize];

        windX = new float[chunkSize][chunkSize];
        windY = new float[chunkSize][chunkSize];

        road = new boolean[chunkSize][chunkSize];
        river = new boolean[chunkSize][chunkSize];
        settlement = new Settlement[chunkSize][chunkSize];

        for (int x = 0; x < chunkSize; x++) {
            for (int y = 0; y < chunkSize; y++) {
                elevation[x][y] = 0f;
                temperature[x][y] = 0f;
                precipitation[x][y] = 0f;
                drainage[x][y] = 0f;

                regionIds[x][y] = 0;

                windX[x][y] = 0f;
                windY[x][y] = 0f;

                river[x][y] = false;
                road[x][y] = false;
                settlement[x][y] = null;
            }
        }
    }

    public BiomeType getTileType(int pX, int pY) {

        if (river[pX][pY])
            return RIVER;
        return BiomeType.biome(elevation[pX][pY], temperature[pX][pY], precipitation[pX][pY]);
    }

}
