package com.stewsters.worldgen.procGen;

import com.stewsters.util.math.Facing2d;
import com.stewsters.util.math.MatUtils;
import com.stewsters.util.noise.OpenSimplexNoise;
import com.stewsters.worldgen.map.BiomeType;
import com.stewsters.worldgen.map.overworld.OverWorld;
import com.stewsters.worldgen.map.overworld.OverWorldChunk;

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
    public OverWorldChunk generate(OverWorld overWorld, int chunkX, int chunkY) {


        log.info("generating " + chunkX + " : " + chunkY);

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

                overWorldChunk.elevation[x][y] = limit((float) (elevation - Math.pow(yDist, 10) - Math.pow(xDist, 10)), -1, 1);

                // Temperature
                //decreases with height, decreases with closeness to poles
                overWorldChunk.temperature[x][y] = 1 - Math.max(yDist, overWorldChunk.elevation[x][y])
                        - 0.4f * yDist
                        - 0.4f * overWorldChunk.elevation[x][y]
                        + 0.2f * (float) el.eval(1.0 * nx / 125.0, 1.0 * ny / 125.0);
            }
        }
        return overWorldChunk;

    }


    // This is a hack for rain shadows.  Start at your square, then go upwind
    // warm temp on water squares boosts humidity, cold mountains limit it
    public void postLoad(OverWorld overWorld) {

        int xSize = overWorld.getPreciseXSize();
        int ySize = overWorld.getPreciseYSize();


        // Cliffyness / Grade

        float periods = 4;
        for (int y = 0; y < ySize; y++) {

            float percentage = (float) y / ySize;
            float globalWindX = (float) Math.cos(periods * (2 * Math.PI) * percentage);
//            if (y >= ySize / 2)
//                globalWindX *= -1;

            for (int x = 0; x < xSize; x++) {
                float temp = overWorld.getTemp(x, y);
                float xd = temp - overWorld.getTemp(x + 1, y);
                float yd = temp - overWorld.getTemp(x, y + 1);

                // Rotate 90 degrees
//                overWorld.setWind(x, y, xd, yd);
                overWorld.setWind(x, y, yd + (0.005f * globalWindX), -xd + (0.005f * globalWindX));
            }
        }

        // rain shadow:
        for (int y = 0; y < ySize; y++) {

            float moist = 0;
            for (int x = 0; x < xSize; x++) {

                float temp = overWorld.getTemp(x, y);
                float maximumMoistureBasedOnTemp = MatUtils.limit(temp, 0, 1);

                // if we are over water, evaporate
                if (overWorld.getTileType(x, y).water) {
                    moist += 1;
                }


                float precip = 0f;

                // Rainfall due to temp
                if (moist > maximumMoistureBasedOnTemp) {
                    float rain = (moist - maximumMoistureBasedOnTemp);
                    moist -= rain / 15f;
                    precip += rain;
                }


                // This is a random
                precip += (float) ((0.75 * mo.eval(x / 70.0, y / 70.0) +
                        0.25 * mo.eval(x / 45.0, y / 45.0))) * 0.5;

                overWorld.setPrecipitation(x, y, precip);

            }
        }

        // Run rivers
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

        // Build Settlements
        for (int i = 0; i < 100; i++) {
            int x = MatUtils.getIntInRange(0, xSize);
            int y = MatUtils.getIntInRange(0, ySize);

            if (!overWorld.getTileType(x, y).name().startsWith("OCEAN")) {
                overWorld.buildSettlement(x, y);
            }
        }
    }
}