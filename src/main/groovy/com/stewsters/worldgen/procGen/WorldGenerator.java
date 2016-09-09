package com.stewsters.worldgen.procGen;

import com.stewsters.util.math.Facing2d;
import com.stewsters.util.math.MatUtils;
import com.stewsters.util.noise.OpenSimplexNoise;
import com.stewsters.worldgen.game.Settlement;
import com.stewsters.worldgen.map.BiomeType;
import com.stewsters.worldgen.map.overworld.OverWorld;
import com.stewsters.worldgen.map.overworld.OverWorldChunk;
import com.stewsters.worldgen.messageBus.Bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Logger;

import static com.stewsters.util.math.MatUtils.limit;

public class WorldGenerator {

    private final static Logger log = Logger.getLogger(WorldGenerator.class.getName());
    OpenSimplexNoise el;
    OpenSimplexNoise mo;
    Random r;

    public WorldGenerator(long seed) {
        r = new Random(seed);
        el = new OpenSimplexNoise(r.nextLong());
        mo = new OpenSimplexNoise(r.nextLong());
    }


    public WorldGenerator() {
        r = new Random();
        el = new OpenSimplexNoise(r.nextLong());
        mo = new OpenSimplexNoise(r.nextLong());
    }

    // Initial step the can be done per chunk
    public OverWorldChunk generateChunkedHeightMap(OverWorld overWorld, int chunkX, int chunkY) {

        log.info("Generating Height " + chunkX + " : " + chunkY);

        OverWorldChunk overWorldChunk = new OverWorldChunk();

        int xCenter = overWorld.xSize * OverWorldChunk.chunkSize / 2;
        int yCenter = overWorld.ySize * OverWorldChunk.chunkSize / 2;

        for (int x = 0; x < OverWorldChunk.chunkSize; x++) {
            for (int y = 0; y < OverWorldChunk.chunkSize; y++) {

                //global coord
                int nx = chunkX * OverWorldChunk.chunkSize + x;
                int ny = chunkY * OverWorldChunk.chunkSize + y;

                float xDist = (float) Math.abs(nx - xCenter) / xCenter;
                float yDist = (float) Math.abs(ny - yCenter) / yCenter;

                // Elevation - decreases near edges
                double elevation = 0.6 * el.eval(nx / 240.0 / 64 * overWorld.xSize, ny / 240.0 / 32 * overWorld.ySize)
                        + 0.3 * el.eval(nx / 84.0 / 64 * overWorld.xSize, ny / 84.0 / 32 * overWorld.ySize)
                        + 0.1 * el.eval(nx / 20.0 / 64 * overWorld.xSize, ny / 20.0 / 32 * overWorld.ySize);

                overWorldChunk.elevation[x][y] = limit(
                        (float) (elevation - Math.pow(yDist, 4) - Math.pow(xDist, 4)),
                        -1, 1);
            }
        }
        return overWorldChunk;
    }

    public OverWorldChunk generateChunkedTemperatureMap(OverWorld overWorld, int chunkX, int chunkY) {

        OverWorldChunk overWorldChunk = overWorld.loadChunk(chunkX, chunkY);
        int yCenter = overWorld.ySize * OverWorldChunk.chunkSize / 2;

        for (int x = 0; x < OverWorldChunk.chunkSize; x++) {
            for (int y = 0; y < OverWorldChunk.chunkSize; y++) {

                int nx = chunkX * OverWorldChunk.chunkSize + x;
                int ny = chunkY * OverWorldChunk.chunkSize + y;

                float yDist = (float) Math.abs(ny - yCenter) / yCenter;

                // Temperature
                //decreases with height, decreases with closeness to poles
                overWorldChunk.temperature[x][y] = MatUtils.limit(
                        1 - yDist
                                - Math.max(0f, overWorldChunk.elevation[x][y])
                                + 0.1f * (float) el.eval(1.0 * nx / 125.0, 1.0 * ny / 125.0),
                        -1, 1);
            }
        }
        return overWorldChunk;
    }


    public void evenElevation(OverWorld overWorld) {

        int xSize = overWorld.getPreciseXSize();
        int ySize = overWorld.getPreciseYSize();

        float highest = -1f;
        float lowest = 1f;

        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                float elev = overWorld.getElevation(x, y);
                if (elev < lowest)
                    lowest = elev;
                if (elev > highest)
                    highest = elev;
            }
        }

        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                float elev = overWorld.getElevation(x, y);
                elev = 2 * (elev - lowest) / (highest - lowest) - 1;
                overWorld.setElevation(x, y, elev);
            }
        }
    }


    public void generateWind(OverWorld overWorld) {
        int xSize = overWorld.getPreciseXSize();
        int ySize = overWorld.getPreciseYSize();

        float periods = 4;
        for (int y = 0; y < ySize; y++) {

            float percentage = (float) y / ySize;
            float globalWindX = (float) Math.cos(periods * (2 * Math.PI) * percentage);

            for (int x = 0; x < xSize; x++) {
                float temp = overWorld.getTemp(x, y);
                float xd = temp - overWorld.getTemp(x + 1, y);
                float yd = temp - overWorld.getTemp(x, y + 1);

                // Rotate 90 degrees
                float windX = yd;
                float windY = -xd;

                windX = (50f * windX) - (0.5f * globalWindX);
                windY = (50f * windY) + (0.5f * globalWindX);

                overWorld.setWind(x, y, windX, windY);
            }
        }
    }

    // This is a hack for rain shadows.  Start at your square, then go upwind
    // warm temp on water squares boosts humidity, cold mountains limit it
    public void generatePrecipitation(OverWorld overWorld) {
        int xSize = overWorld.getPreciseXSize();
        int ySize = overWorld.getPreciseYSize();


        final float maxDistance = 1000f;
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {

                float tempX = x;
                float tempY = y;


                float distanceLeft = maxDistance;
                while (distanceLeft > 0) {

                    if (!overWorld.contains((int) tempX, (int) tempY)) break;

                    BiomeType bt = overWorld.getTileType((int) tempX, (int) tempY);
                    if (bt.water) break;

                    distanceLeft--;

                    float windX = overWorld.getWindX((int) tempX, (int) tempY);
                    float windY = overWorld.getWindY((int) tempX, (int) tempY);

//
//                    if (Math.abs(windX) > Math.abs(windY)) {
//                        nextX = (windX > 0) ? 1 : -1;
//                    } else {
//                        nextY = (windY > 0) ? 1 : -1;
//                    }

                    tempX += windX;
                    tempY += windY;
                }

                overWorld.setPrecipitation(x, y, distanceLeft / maxDistance);
            }
        }


        // rain shadow:
//        for (int y = 0; y < ySize; y++) {
//
//            float moist = 0;
//            for (int x = 0; x < xSize; x++) {
//
//                float temp = overWorld.getTemp(x, y);
//                float maximumMoistureBasedOnTemp = MatUtils.limit(temp, 0, 1);
//
//                // if we are over water, evaporate
//                if (overWorld.getTileType(x, y).water) {
//                    moist += maximumMoistureBasedOnTemp;
//                }
//
//                // Rainfall due to temp
//                if (moist > maximumMoistureBasedOnTemp) {
//                    float rain = (moist - maximumMoistureBasedOnTemp);
//                    moist -= rain;
//                }
//                overWorld.setPrecipitation(x, y, moist);
//
//            }
//        }

    }

    // Run rivers

    /**
     * Precipitation should flow down grade to the sea
     * Anywhere there is a significant flow should be marked as a river
     * <p>
     * Areas with enough flow can dig out a canyon.
     * <p>
     * May need to floodfill a lake if we get to a local minima
     *
     * @param overWorld
     */
    public void generateRivers(OverWorld overWorld) {
        int xSize = overWorld.getPreciseXSize();
        int ySize = overWorld.getPreciseYSize();

        for (int i = 0; i < 50; i++) {
            int x = MatUtils.getIntInRange(0, xSize);
            int y = MatUtils.getIntInRange(0, ySize);
            boolean done = false;

            while (!done) {

                BiomeType existingType = overWorld.getTileType(x, y);
                if (existingType == BiomeType.OCEAN_ABYSSAL || existingType == BiomeType.OCEAN_DEEP || existingType == BiomeType.OCEAN_SHALLOW) {
                    break;
                }

                // if the biome is ocean or frozen then end.
                Facing2d facing = null;
                float height = overWorld.getElevation(x, y);

                if (height > overWorld.getElevation(x, y + 1)) {
                    facing = Facing2d.NORTH;
                    height = overWorld.getElevation(x, y + 1);
                }

                if (height > overWorld.getElevation(x, y - 1)) {
                    facing = Facing2d.SOUTH;
                    height = overWorld.getElevation(x, y - 1);
                }

                if (height > overWorld.getElevation(x + 1, y)) {
                    facing = Facing2d.EAST;
                    height = overWorld.getElevation(x + 1, y);
                }

                if (height > overWorld.getElevation(x - 1, y)) {
                    facing = Facing2d.WEST;
                    height = overWorld.getElevation(x - 1, y);
                }

                if (facing == null) {
                    done = true;
                } else {
                    overWorld.setRiver(x, y);

                    x = x + facing.x;
                    y = y + facing.y;
                }

            }
        }
    }


    /**
     * Build Settlements
     * <p>
     * Human settlements should be built near a source of water, preferably a river.
     * Most should be built at a low elevation
     * Generating a good distance from other towns
     * <p>
     * Population should follow https://en.wikipedia.org/wiki/Zipf%27s_law
     *
     * @param overWorld
     */
    public void populateSettlements(OverWorld overWorld) {
        int xSize = overWorld.getPreciseXSize();
        int ySize = overWorld.getPreciseYSize();

        // Build Settlements
        for (int i = 0; i < 100; i++) {
            int x = MatUtils.getIntInRange(0, xSize);
            int y = MatUtils.getIntInRange(0, ySize);

            if (!overWorld.getTileType(x, y).name().startsWith("OCEAN")) {
                overWorld.buildSettlement(x, y);
            }
        }
    }

    // http://roadtrees.com/creating-road-trees/
    public void createRoadNetwork(OverWorld overWorld) {
        int xSize = overWorld.getPreciseXSize();
        int ySize = overWorld.getPreciseYSize();

        //Step One Determine which cities to link together first. Larger closer cities should go first

        ArrayList<RankedSettlementPair> pairs = new ArrayList<>();
        for (int a = 0; a < Settlement.settlements.size() - 1; a++) {
            for (int b = a + 1; b < Settlement.settlements.size(); b++) {
                pairs.add(new RankedSettlementPair(Settlement.settlements.get(a), Settlement.settlements.get(b)));
            }
        }
        Collections.sort(pairs);

        for (RankedSettlementPair p : pairs) {
            Bus.bus.post("Distance " + p.distance + " a:" + p.a + " b:" + p.b).now();
        }

        // Use A* to link cities.  Cost should reflect slope and terrain type.  Bridges are possible, but expensive.

        //The first cities that are linked will have busy roads, and they will shrink down as

    }

    private class RankedSettlementPair implements Comparable<RankedSettlementPair> {
        int a;
        int b;
        int distance;

        public RankedSettlementPair(Settlement a, Settlement b) {
            this.a = a.id;
            this.b = b.id;

            distance = (int) (a.population * b.population / Math.pow(a.pos.getChebyshevDistance(b.pos), 2));
        }

        @Override
        public int compareTo(RankedSettlementPair o) {
            return distance - o.distance;
        }
    }
}