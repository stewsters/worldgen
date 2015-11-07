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


    public TileType getTileType(int globalX, int globalY) {
        OverworldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        if (chunk == null)
            return TileType.OCEAN_ABYSSAL;

        return chunk.getTileType(getPrecise(globalX), getPrecise(globalY));
    }


    private OverworldChunk loadChunk(int chunkX, int chunkY) {

        if (chunkX < 0 || chunkX >= xSize || chunkY < 0 || chunkY >= ySize) {
            return null;
        } else if (chunks[chunkX][chunkY] == null) {
            chunks[chunkX][chunkY] = worldGenerator.generate(chunkX, chunkY);
        }
        return chunks[chunkX][chunkY];
    }
}
