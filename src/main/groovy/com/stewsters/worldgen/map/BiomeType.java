package com.stewsters.worldgen.map;

import squidpony.squidcolor.SColor;

public enum BiomeType {

    RIVER(false, '~', SColor.ALICE_BLUE, true),

    OCEAN_ABYSSAL(false, '~', SColor.DARK_BLUE, true),
    OCEAN_DEEP(false, '~', SColor.BLUE, true),
    OCEAN_SHALLOW(false, '~', SColor.LIGHT_BLUE, true),

    BEACH(false, '.', SColor.YELLOW),

    SCORCHED(false, '.', SColor.BROWN),
    BARE(false, '.', SColor.DARK_BROWN),
    TUNDRA(false, ',', SColor.LIGHT_GRAY),

    SNOW(false, '.', SColor.WHITE),
    SEA_ICE(false, '_', new SColor(0xffffee, "Sea Ice")),

    SHRUBLAND(false, ';', SColor.BURNT_SIENNA),
    TAIGA(false, 'i', SColor.DARK_GREEN),

    TEMPERATE_DESERT(false, 'd', SColor.RED_BEAN),
    TEMPERATE_DECIDUOUS_FOREST(false, 'D', SColor.GREEN_TEA_DYE),
    TEMPERATE_RAIN_FOREST(false, 'T', SColor.GREEN_BAMBOO),


    SUBTROPICAL_DESERT(false, 'd', SColor.NAVAJO_WHITE),
    GRASSLAND(false, ':', SColor.YELLOW_GREEN),
    TROPICAL_SEASONAL_FOREST(false, 'S', SColor.GREEN_BAMBOO),
    TROPICAL_RAIN_FOREST(false, 'T', SColor.DARK_GREEN);


    public final boolean blocks;
    public final SColor color;
    public final SColor darkColor;
    public final SColor brightColor;

    public final char character;
    public boolean water = false;

    BiomeType(boolean blocks, char character, SColor color) {
        this(blocks, character, color, false);
    }

    BiomeType(boolean blocks, char character, SColor color, boolean water) {
        this.blocks = blocks;
        this.character = character;
        this.color = color;
        this.water = water;
        this.darkColor = new SColor(Math.max(color.getRed() - 20, 0), Math.max(color.getGreen() - 20, 0), Math.max(color.getBlue() - 20, 0));
        this.brightColor = new SColor(Math.min(color.getRed() + 20, 255), Math.min(color.getGreen() + 20, 255), Math.min(color.getBlue() + 20, 255));
    }

    // Tree Line - highest survivable trees
    // 4000 near the equator, 2000 near the poles
    // timberline - Highest canopy - forest
    //   Simplified biome chart: http://imgur.com/kM8b5Zq
    public static BiomeType biome(double e, double t, double p) {

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

        if (t < 0.40) {
            if (p < 0.2) return TEMPERATE_DESERT;
            if (p < 0.66) return SHRUBLAND;
            return TAIGA;
        }

        if (t < 0.6) {
            if (p < 0.16) return TEMPERATE_DESERT;
            if (p < 0.50) return GRASSLAND;
            if (p < 0.83) return TEMPERATE_DECIDUOUS_FOREST;
            return TEMPERATE_RAIN_FOREST;
        }

        if (p < 0.10) return SUBTROPICAL_DESERT;
        if (p < 0.33) return GRASSLAND;
        if (p < 0.66) return TROPICAL_SEASONAL_FOREST;
        return TROPICAL_RAIN_FOREST;
    }

    public byte id() {
        return (byte) ordinal();
    }
}
