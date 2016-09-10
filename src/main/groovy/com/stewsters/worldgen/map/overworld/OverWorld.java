package com.stewsters.worldgen.map.overworld;


import com.stewsters.util.math.Point2i;
import com.stewsters.worldgen.game.Settlement;
import com.stewsters.worldgen.map.BiomeType;
import com.stewsters.worldgen.messageBus.Bus;
import com.stewsters.worldgen.procGen.WorldGenerator;

import java.util.ArrayList;
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

        ArrayList<Point2i> coords = new ArrayList<Point2i>();
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                coords.add(new Point2i(x, y));
            }
        }

        // Generate elevation
        coords.parallelStream().forEach(coord ->
                chunks[coord.x][coord.y] = worldGenerator.generateChunkedHeightMap(this, coord.x, coord.y)
        );
        worldGenerator.evenElevation(this);

        // Generate temperature based on that elevation
        coords.parallelStream().forEach(coord ->
                chunks[coord.x][coord.y] = worldGenerator.generateChunkedTemperatureMap(this, coord.x, coord.y)
        );
        Bus.bus.post("Finished Elevation").now();

        worldGenerator.generateWind(this);
        Bus.bus.post("Finished Wind").now();

        worldGenerator.generatePrecipitation(this);
        Bus.bus.post("Finished Precipitation").now();

        worldGenerator.generateRivers(this);
        Bus.bus.post("Finished Rivers").now();

        // Regeneration based on the new rivers
        worldGenerator.generateWind(this);
        Bus.bus.post("Finished Wind 2").now();

        worldGenerator.generatePrecipitation(this);
        Bus.bus.post("Finished Precipitation 2").now();


        worldGenerator.populateSettlements(this);
        Bus.bus.post("Finished Populating Settlements").now();

        worldGenerator.createRoadNetwork(this);
        Bus.bus.post("Finished Creating Roads").now();

        Bus.bus.post("Finished Generation").now();
    }


    public int getPreciseXSize() {
        return xSize * OverWorldChunk.chunkSize;
    }

    public int getPreciseYSize() {
        return ySize * OverWorldChunk.chunkSize;
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

    public void buildSettlement(int globalX, int globalY) {

        Settlement settlement = Settlement.build(globalX, globalY);

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

    public boolean contains(int globalX, int globalY) {
        return !(globalX < 0 || globalY < 0 || globalX > xSize * OverWorldChunk.chunkSize || globalY > ySize * OverWorldChunk.chunkSize);
    }
}
