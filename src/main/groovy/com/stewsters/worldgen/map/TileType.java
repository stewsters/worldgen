package com.stewsters.worldgen.map;

import squidpony.squidcolor.SColor;

public enum TileType {

    RIVER(false, '~', SColor.ALICE_BLUE),

    OCEAN_ABYSSAL(false, '~', SColor.DARK_BLUE),
    OCEAN_DEEP(false, '~', SColor.BLUE),
    OCEAN_SHALLOW(false, '~', SColor.LIGHT_BLUE),


    BEACH(false, '.', SColor.YELLOW),

    SCORCHED(false, '.', SColor.BROWN),
    BARE(false, '.', SColor.DARK_BROWN),
    TUNDRA(false, ',', SColor.LIGHT_GRAY),
    SNOW(false, '.', SColor.WHITE),

    SHRUBLAND(false, ';', SColor.BURNT_SIENNA),
    TAIGA(false, 'i', SColor.DARK_GREEN),

    TEMPERATE_DESERT(false, 'd', SColor.RED_BEAN),
    TEMPERATE_DECIDUOUS_FOREST(false, 'D', SColor.GREEN_TEA_DYE),
    TEMPERATE_RAIN_FOREST(false, 'T', SColor.GREEN_BAMBOO),


    SUBTROPICAL_DESERT(false, 'd', SColor.GREEN_BAMBOO),
    GRASSLAND(false, ':', SColor.GREEN),
    TROPICAL_SEASONAL_FOREST(false, 'S', SColor.GREEN_BAMBOO),
    TROPICAL_RAIN_FOREST(false, 'T', SColor.DARK_GREEN),
    SEA_ICE(false, '_', SColor.NAVAJO_WHITE);


    public final boolean blocks;
    public final SColor color;
    public final char character;

    public static TileType[] lookup = TileType.values();

    TileType(boolean blocks, char character, SColor color) {
        this.blocks = blocks;
        this.character = character;
        this.color = color;
    }

    public byte id() {
        return (byte) ordinal();
    }
}
