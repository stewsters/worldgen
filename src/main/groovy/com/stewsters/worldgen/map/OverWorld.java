package com.stewsters.worldgen.map;


import com.stewsters.util.math.Facing2d;
import com.stewsters.util.math.MatUtils;
import com.stewsters.worldgen.procGen.WorldGenerator;

import java.util.Random;
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

        worldGenerator = new WorldGenerator();
        chunks = new OverworldChunk[xSize][ySize];

        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                loadChunk(x, y);
            }
        }

        // Run rivers

        Random r = new Random();

        for (int i = 0; i < 50; i++) {
            int x = MatUtils.getIntInRange(0, xSize * OverworldChunk.chunkSize - 1);
            int y = MatUtils.getIntInRange(0, xSize * OverworldChunk.chunkSize - 1);
            boolean done = false;

            while (!done) {

                TileType existingType = getTileType(x, y);
                if (existingType == TileType.OCEAN_ABYSSAL || existingType == TileType.OCEAN_DEEP || existingType == TileType.OCEAN_SHALLOW) {
                    break;
                }

                // if the biome is ocean or frozen then end.
                Facing2d facing = null;
                float height = getElevation(x, y);

                if (height > getElevation(x, y + 1)) {
                    facing = Facing2d.NORTH;
                    height = getElevation(x, y + 1);
                }

                if (height > getElevation(x, y - 1)) {
                    facing = Facing2d.SOUTH;
                    height = getElevation(x, y - 1);
                }

                if (height > getElevation(x + 1, y)) {
                    facing = Facing2d.EAST;
                    height = getElevation(x + 1, y);
                }

                if (height > getElevation(x - 1, y)) {
                    facing = Facing2d.WEST;
                    height = getElevation(x - 1, y);
                }

                if (facing == null) {
                    done = true;
                } else {
                    setRiver(x, y);

                    x = x + facing.x;
                    y = y + facing.y;
                }

            }


        }


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

    public float getPrecipitation(int globalX, int globalY) {
        OverworldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        if (chunk == null)
            return -1;

        return chunk.precipitation[getPrecise(globalX)][getPrecise(globalY)];
    }


    private void setRiver(int globalX, int globalY) {
        OverworldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        if (chunk != null)
            chunk.river[getPrecise(globalX)][getPrecise(globalY)] = true;
    }

    public float getLatitude(int globalY) {

        int yCenter = ySize * OverworldChunk.chunkSize / 2;
        return (float) (globalY - yCenter) / yCenter;
    }

    public float getLongitude(int globalX) {
        int xCenter = xSize * OverworldChunk.chunkSize / 2;
        return (float) (globalX - xCenter) / xCenter;
    }

}
