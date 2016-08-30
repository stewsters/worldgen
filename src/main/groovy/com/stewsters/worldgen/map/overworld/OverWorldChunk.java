package com.stewsters.worldgen.map.overworld;

import com.stewsters.worldgen.game.Settlement;
import com.stewsters.worldgen.map.BiomeType;

import static com.stewsters.worldgen.map.BiomeType.BARE;
import static com.stewsters.worldgen.map.BiomeType.BEACH;
import static com.stewsters.worldgen.map.BiomeType.GRASSLAND;
import static com.stewsters.worldgen.map.BiomeType.OCEAN_ABYSSAL;
import static com.stewsters.worldgen.map.BiomeType.OCEAN_DEEP;
import static com.stewsters.worldgen.map.BiomeType.OCEAN_SHALLOW;
import static com.stewsters.worldgen.map.BiomeType.RIVER;
import static com.stewsters.worldgen.map.BiomeType.SCORCHED;
import static com.stewsters.worldgen.map.BiomeType.SEA_ICE;
import static com.stewsters.worldgen.map.BiomeType.SHRUBLAND;
import static com.stewsters.worldgen.map.BiomeType.SNOW;
import static com.stewsters.worldgen.map.BiomeType.SUBTROPICAL_DESERT;
import static com.stewsters.worldgen.map.BiomeType.TAIGA;
import static com.stewsters.worldgen.map.BiomeType.TEMPERATE_DECIDUOUS_FOREST;
import static com.stewsters.worldgen.map.BiomeType.TEMPERATE_DESERT;
import static com.stewsters.worldgen.map.BiomeType.TEMPERATE_RAIN_FOREST;
import static com.stewsters.worldgen.map.BiomeType.TROPICAL_RAIN_FOREST;
import static com.stewsters.worldgen.map.BiomeType.TROPICAL_SEASONAL_FOREST;
import static com.stewsters.worldgen.map.BiomeType.TUNDRA;

public class OverWorldChunk {

    public static final int chunkSize = 64;

    public float[][] elevation;
    public float[][] temperature;
    public float[][] precipitation;
    public float[][] drainage;

    public float[][] windX;
    public float[][] windY;

    // TODO: sunlight - angle of terrain can reduce it.  It effects temp and plant types
    // TODO: Moisture should be generated from warm water.  It should create pressure and move to low pressure

    // The north sides of mountains may have a different biome then the south sides.

    public boolean[][] river;

    public Settlement[][] settlement;

    public OverWorldChunk() {

        elevation = new float[chunkSize][chunkSize];
        temperature = new float[chunkSize][chunkSize];
        precipitation = new float[chunkSize][chunkSize];
        drainage = new float[chunkSize][chunkSize];

        windX = new float[chunkSize][chunkSize];
        windY = new float[chunkSize][chunkSize];

        river = new boolean[chunkSize][chunkSize];
        settlement = new Settlement[chunkSize][chunkSize];

        for (int x = 0; x < chunkSize; x++) {
            for (int y = 0; y < chunkSize; y++) {
                elevation[x][y] = 0f;
                temperature[x][y] = 0f;
                precipitation[x][y] = 0f;
                drainage[x][y] = 0f;

                windX[x][y] = 0f;
                windY[x][y] = 0f;

                river[x][y] = false;
                settlement[x][y] = null;
            }
        }
    }

    public BiomeType getTileType(int pX, int pY) {

        if (river[pX][pY])
            return RIVER;
        return biome(elevation[pX][pY], temperature[pX][pY], precipitation[pX][pY]);
    }


    // Tree Line - highest survivable trees
    // 4000 near the equator, 2000 near the poles

    //timberline - Highest canopy - forest

    //   Simplified biome chart: http://imgur.com/kM8b5Zq
    private BiomeType biome(double e, double t, double p) {

        if (e < 0.0 && t < 0.0) return SEA_ICE;

        if (e < -0.75) return OCEAN_ABYSSAL;
        if (e < -0.05) return OCEAN_DEEP;
        if (e < 0.0) return OCEAN_SHALLOW;

        if (t < 0) return SNOW;
        if (e > 0.7) { // Above Treeline
            return BARE;
        }

        if (e < 0.01) {
            return BEACH;
        }

        if (t < 0) {
            if (p < 0.1) return SCORCHED;
            if (p < 0.2) return BARE;
            if (p < 0.5) return TUNDRA;
            return SNOW;
        }

        if (t < 0.20) {
            if (p < 0.33) return TEMPERATE_DESERT;
            if (p < 0.66) return SHRUBLAND;
            return TAIGA;
        }

        if (t < 0.4) {
            if (p < 0.16) return TEMPERATE_DESERT;
            if (p < 0.50) return GRASSLAND;
            if (p < 0.83) return TEMPERATE_DECIDUOUS_FOREST;
            return TEMPERATE_RAIN_FOREST;
        }

        if (p < 0.16) return SUBTROPICAL_DESERT;
        if (p < 0.33) return GRASSLAND;
        if (p < 0.66) return TROPICAL_SEASONAL_FOREST;
        return TROPICAL_RAIN_FOREST;


    }
}
