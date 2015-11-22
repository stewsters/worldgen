package com.stewsters.worldgen.map;

import static com.stewsters.worldgen.map.BiomeType.*;

public class OverworldChunk {

    public static final int chunkSize = 64;

    public final long lowCornerX;
    public final long lowCornerY;

    public float[][] elevation;
    public float[][] temperature;
    public float[][] precipitation;
    public float[][] drainage;

    public boolean[][] river;

    public OverworldChunk(long lowCornerX, long lowCornerY) {
        this.lowCornerX = lowCornerX;
        this.lowCornerY = lowCornerY;

        elevation = new float[chunkSize][chunkSize];
        temperature = new float[chunkSize][chunkSize];
        precipitation = new float[chunkSize][chunkSize];
        drainage = new float[chunkSize][chunkSize];

        river = new boolean[chunkSize][chunkSize];

        for (int x = 0; x < chunkSize; x++) {
            for (int y = 0; y < chunkSize; y++) {
                elevation[x][y] = 0f;
                temperature[x][y] = 0f;
                precipitation[x][y] = 0f;
                drainage[x][y] = 0f;
                river[x][y] = false;
            }
        }
    }

    public BiomeType getTileType(int pX, int pY) {

        if(river[pX][pY])
            return RIVER;
        return biome(elevation[pX][pY], temperature[pX][pY], precipitation[pX][pY]);
    }

    private BiomeType biome(double e, double t, double p) {

        if (e < 0.0 && t < 0.0) return SEA_ICE;

        if (e < -0.75) return OCEAN_ABYSSAL;
        if (e < -0.05) return OCEAN_DEEP;
        if (e < 0.0) return OCEAN_SHALLOW;
        if (e < 0.01) return BEACH;

        if (t <0) {
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
