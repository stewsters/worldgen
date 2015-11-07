package com.stewsters.worldgen.map;


import com.stewsters.worldgen.procGen.WorldGenerator;

import java.util.logging.Logger;


public class OverWorld {

    public final int xSize;
    public final int ySize;

    private static final Logger log = Logger.getLogger(OverWorld.class.getName());

    public final OverworldChunk[][] chunks;

    private WorldGenerator worldGenerator;

    public OverWorld(int xSize, int ySize) {

        this.xSize = xSize;
        this.ySize = ySize;

        chunks = new OverworldChunk[xSize][ySize];

        worldGenerator = new WorldGenerator();
    }

    public void update() {

    }

    private static int getPrecise(long globalCoord) {
        if (globalCoord >= 0) {
            return (int) (globalCoord % OverworldChunk.chunkSize);
        } else {
            return (int) (globalCoord % OverworldChunk.chunkSize) + OverworldChunk.chunkSize - 1;
        }
    }

    private static int getChunkCoord(int globalCoord) {
        if (globalCoord >= 0) {
            return (globalCoord / OverworldChunk.chunkSize);
        } else {
            return (globalCoord / OverworldChunk.chunkSize) - 1;
        }
    }

    private OverworldChunk loadChunk(int chunkX, int chunkY) {

        if (chunkX < 0 || chunkX >= xSize || chunkY < 0 || chunkY >= ySize) {
            return null;
        } else if (chunks[chunkX][chunkY] == null) {
            chunks[chunkX][chunkY] = worldGenerator.generate(this, chunkX, chunkY);
        }
        return chunks[chunkX][chunkY];
    }


    public TileType getTileType(int globalX, int globalY) {
        OverworldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        if (chunk == null)
            return TileType.OCEAN_ABYSSAL;

        return chunk.getTileType(getPrecise(globalX), getPrecise(globalY));
    }

    public float getElevation(int globalX, int globalY) {
        OverworldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        if (chunk == null)
            return -1;

        return chunk.elevation[getPrecise(globalX)][getPrecise(globalY)];
    }

    public float getTemp(int globalX, int globalY) {
        OverworldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        if (chunk == null)
            return -1;

        return chunk.temperature[getPrecise(globalX)][getPrecise(globalY)];
    }


    public float getLatitude(int globalY) {

        int yCenter = ySize * OverworldChunk.chunkSize / 2;
        return (float) Math.abs(globalY - yCenter) / yCenter;
    }

    public float getLongitude(int globalX) {
        int xCenter = xSize * OverworldChunk.chunkSize / 2;
        return  (float) Math.abs(globalX - xCenter) / xCenter;
    }

}
