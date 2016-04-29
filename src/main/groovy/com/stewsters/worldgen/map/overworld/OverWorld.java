package com.stewsters.worldgen.map.overworld;


import com.stewsters.util.math.Facing2d;
import com.stewsters.util.math.MatUtils;
import com.stewsters.worldgen.game.Settlement;
import com.stewsters.worldgen.map.BiomeType;
import com.stewsters.worldgen.procGen.WorldGenerator;

import java.util.logging.Logger;


public class OverWorld {

    public final int xSize;
    public final int ySize;

    private static final Logger log = Logger.getLogger(OverWorld.class.getName());

    public final OverWorldChunk[][] chunks;

    private WorldGenerator worldGenerator;

    public OverWorld(int xSize, int ySize) {

        this.xSize = xSize;
        this.ySize = ySize;

        worldGenerator = new WorldGenerator();
        chunks = new OverWorldChunk[xSize][ySize];

        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                loadChunk(x, y);
            }
        }

        // Run rivers

        for (int i = 0; i < 50; i++) {
            int x = MatUtils.getIntInRange(0, xSize * OverWorldChunk.chunkSize - 1);
            int y = MatUtils.getIntInRange(0, xSize * OverWorldChunk.chunkSize - 1);
            boolean done = false;

            while (!done) {

                BiomeType existingType = getTileType(x, y);
                if (existingType == BiomeType.OCEAN_ABYSSAL || existingType == BiomeType.OCEAN_DEEP || existingType == BiomeType.OCEAN_SHALLOW) {
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

        // Build Settlements
        for (int i = 0; i < 100; i++) {
            int x = MatUtils.getIntInRange(0, xSize * OverWorldChunk.chunkSize - 1);
            int y = MatUtils.getIntInRange(0, xSize * OverWorldChunk.chunkSize - 1);

            if (!getTileType(x, y).name().startsWith("OCEAN")) {
                buildSettlement(x, y);
            }

        }


    }

    public void update() {

    }


    private static int getPrecise(int globalCoord) {
        if (globalCoord >= 0) {
            return (globalCoord % OverWorldChunk.chunkSize);
        } else {
            return (globalCoord % OverWorldChunk.chunkSize) + OverWorldChunk.chunkSize - 1;
        }
    }

    private static int getChunkCoord(int globalCoord) {
        if (globalCoord >= 0) {
            return (globalCoord / OverWorldChunk.chunkSize);
        } else {
            return (globalCoord / OverWorldChunk.chunkSize) - 1;
        }
    }


    public BiomeType getTileType(int globalX, int globalY) {
        OverWorldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        if (chunk == null)
            return BiomeType.OCEAN_ABYSSAL;

        return chunk.getTileType(getPrecise(globalX), getPrecise(globalY));
    }

    public float getElevation(int globalX, int globalY) {
        OverWorldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        if (chunk == null)
            return -1;

        return chunk.elevation[getPrecise(globalX)][getPrecise(globalY)];
    }

    public float getTemp(int globalX, int globalY) {
        OverWorldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        if (chunk == null)
            return -1;

        return chunk.temperature[getPrecise(globalX)][getPrecise(globalY)];
    }

    public float getPrecipitation(int globalX, int globalY) {
        OverWorldChunk chunk = loadGlobalChunk(globalX, globalY);
        if (chunk == null)
            return -1;

        return chunk.precipitation[getPrecise(globalX)][getPrecise(globalY)];
    }


    private void setRiver(int globalX, int globalY) {
        OverWorldChunk chunk = loadGlobalChunk(globalX, globalY);
        if (chunk != null)
            chunk.river[getPrecise(globalX)][getPrecise(globalY)] = true;
    }

    public float getLatitude(int globalY) {
        int yCenter = ySize * OverWorldChunk.chunkSize / 2;
        return (float) (globalY - yCenter) / yCenter;
    }

    public float getLongitude(int globalX) {
        int xCenter = xSize * OverWorldChunk.chunkSize / 2;
        return (float) (globalX - xCenter) / xCenter;
    }

    private void buildSettlement(int globalX, int globalY) {

        Settlement settlement = Settlement.build(globalX, globalY);

        OverWorldChunk chunk = loadGlobalChunk(globalX, globalY);

        chunk.settlement[getPrecise(globalX)][getPrecise(globalY)] = settlement;
    }


    private OverWorldChunk loadChunk(int chunkX, int chunkY) {

        if (chunkX < 0 || chunkX >= xSize || chunkY < 0 || chunkY >= ySize) {
            return null;
        } else if (chunks[chunkX][chunkY] == null) {
            chunks[chunkX][chunkY] = worldGenerator.generate(this, chunkX, chunkY);
        }
        return chunks[chunkX][chunkY];
    }

    public OverWorldChunk loadGlobalChunk(int globalX, int globalY) {
        return loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
    }

    public Settlement getSettlement(int globalX, int globalY) {
        OverWorldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        if (chunk == null)
            return null;

        return chunk.settlement[getPrecise(globalX)][getPrecise(globalY)];
    }
}
