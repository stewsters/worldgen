package com.stewsters.worldgen.map.overworld;


import com.stewsters.util.pathing.twoDimention.shared.TileBasedMap2d;
import com.stewsters.worldgen.game.Settlement;
import com.stewsters.worldgen.map.BiomeType;


public class OverWorld implements TileBasedMap2d {

    public final int xSize;
    public final int ySize;

    public final OverWorldChunk[][] chunks;

    public OverWorld(int xSize, int ySize) {

        this.xSize = xSize;
        this.ySize = ySize;

        chunks = new OverWorldChunk[xSize][ySize];

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

    public int getPreciseXSize() {
        return xSize * OverWorldChunk.chunkSize;
    }

    public int getPreciseYSize() {
        return ySize * OverWorldChunk.chunkSize;
    }

    @Override
    public int getXSize() {
        return getPreciseXSize();
    }

    @Override
    public int getYSize() {
        return getPreciseYSize();
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

    public float getSlopeX(int globalX, int globalY) {
        OverWorldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        OverWorldChunk chunk2 = loadChunk(getChunkCoord(globalX + 1), getChunkCoord(globalY));

        if (chunk == null || chunk2 == null)
            return 0;
        return chunk.elevation[getPrecise(globalX)][getPrecise(globalY)] - chunk2.elevation[getPrecise(globalX + 1)][getPrecise(globalY)];
    }

    public float getSlopeY(int globalX, int globalY) {
        OverWorldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        OverWorldChunk chunk2 = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY + 1));

        if (chunk == null || chunk2 == null)
            return 0;
        return chunk.elevation[getPrecise(globalX)][getPrecise(globalY)] - chunk2.elevation[getPrecise(globalX)][getPrecise(globalY + 1)];
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


    public float getLatitude(int globalY) {
        int yCenter = ySize * OverWorldChunk.chunkSize / 2;
        return (float) (globalY - yCenter) / yCenter;
    }

    public float getLongitude(int globalX) {
        int xCenter = xSize * OverWorldChunk.chunkSize / 2;
        return (float) (globalX - xCenter) / xCenter;
    }

    public OverWorldChunk loadChunk(int chunkX, int chunkY) {

        if (chunkX < 0 || chunkX >= xSize || chunkY < 0 || chunkY >= ySize) {
            return null;
        } else if (chunks[chunkX][chunkY] == null) {
            // TODO: load from disk
            throw new RuntimeException("Nope, loading not implemented yet");
        }
        return chunks[chunkX][chunkY];
    }

    public OverWorldChunk loadGlobalChunk(int globalX, int globalY) {
        return loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
    }

    public void setRiver(int globalX, int globalY) {
        OverWorldChunk chunk = loadGlobalChunk(globalX, globalY);
        if (chunk != null)
            chunk.river[getPrecise(globalX)][getPrecise(globalY)] = true;
    }

    public void buildSettlement(int globalX, int globalY, int pop) {

        Settlement settlement = Settlement.build(globalX, globalY, pop);

        OverWorldChunk chunk = loadGlobalChunk(globalX, globalY);

        chunk.settlement[getPrecise(globalX)][getPrecise(globalY)] = settlement;
    }

    public Settlement getSettlement(int globalX, int globalY) {
        OverWorldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        if (chunk == null)
            return null;

        return chunk.settlement[getPrecise(globalX)][getPrecise(globalY)];
    }

    public void setPrecipitation(int globalX, int globalY, float precip) {
        OverWorldChunk chunk = loadGlobalChunk(globalX, globalY);
        if (chunk != null)
            chunk.precipitation[getPrecise(globalX)][getPrecise(globalY)] = precip;
    }

    public void setWind(int globalX, int globalY, float xFlow, float yFlow) {
        OverWorldChunk chunk = loadGlobalChunk(globalX, globalY);
        if (chunk != null) {
            chunk.windX[getPrecise(globalX)][getPrecise(globalY)] = xFlow;
            chunk.windY[getPrecise(globalX)][getPrecise(globalY)] = yFlow;
        }
    }

    public float getWindX(int globalX, int globalY) {
        OverWorldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        if (chunk == null)
            return -1f;
        return chunk.windX[getPrecise(globalX)][getPrecise(globalY)];
    }

    public float getWindY(int globalX, int globalY) {
        OverWorldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        if (chunk == null)
            return -1f;
        return chunk.windY[getPrecise(globalX)][getPrecise(globalY)];
    }

    public void setElevation(int globalX, int globalY, float elevation) {
        OverWorldChunk chunk = loadGlobalChunk(globalX, globalY);
        if (chunk != null)
            chunk.elevation[getPrecise(globalX)][getPrecise(globalY)] = elevation;
    }

    public boolean getRoad(int globalX, int globalY) {

        OverWorldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        if (chunk == null)
            return false;

        return chunk.road[getPrecise(globalX)][getPrecise(globalY)];
    }

    public void setRoad(int globalX, int globalY) {
        OverWorldChunk chunk = loadGlobalChunk(globalX, globalY);
        if (chunk != null)
            chunk.road[getPrecise(globalX)][getPrecise(globalY)] = true;
    }

    @Override
    public boolean isOutside(int globalX, int globalY) {
        return globalX < 0 || globalY < 0 || globalX >= xSize * OverWorldChunk.chunkSize || globalY >= ySize * OverWorldChunk.chunkSize;
    }

    public void setRegion(int globalX, int globalY, int newRegion) {
        OverWorldChunk chunk = loadGlobalChunk(globalX, globalY);
        if (chunk != null)
            chunk.regionIds[getPrecise(globalX)][getPrecise(globalY)] = newRegion;
    }

    public int getRegionId(int globalX, int globalY) {
        OverWorldChunk chunk = loadChunk(getChunkCoord(globalX), getChunkCoord(globalY));
        if (chunk == null) {
            return -3;
        }
        return chunk.regionIds[getPrecise(globalX)][getPrecise(globalY)];
    }
}
