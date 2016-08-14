package com.stewsters.worldgen.map;

import squidpony.squidcolor.SColor;

public enum BiomeType {

    RIVER(false, '~', SColor.ALICE_BLUE,true),

    OCEAN_ABYSSAL(false, '~', SColor.DARK_BLUE,true),
    OCEAN_DEEP(false, '~', SColor.BLUE,true),
    OCEAN_SHALLOW(false, '~', SColor.LIGHT_BLUE,true),

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
    public final SColor darkColor;
    public final SColor brightColor;

    public final char character;
    public boolean water=false;

    BiomeType(boolean blocks, char character, SColor color){
        this(blocks,character,color,false);
    }

    BiomeType(boolean blocks, char character, SColor color, boolean water) {
        this.blocks = blocks;
        this.character = character;
        this.color = color;
        this.water=water;
        this.darkColor = new SColor(Math.max(color.getRed() - 20, 0), Math.max(color.getGreen() - 20, 0), Math.max(color.getBlue() - 20, 0));
        this.brightColor = new SColor(Math.min(color.getRed() + 20, 255), Math.min(color.getGreen() + 20, 255), Math.min(color.getBlue() + 20, 255));
    }

    public byte id() {
        return (byte) ordinal();
    }
}
