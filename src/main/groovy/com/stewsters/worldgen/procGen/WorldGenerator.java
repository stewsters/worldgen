package com.stewsters.worldgen.procGen;

import com.stewsters.util.noise.OpenSimplexNoise;
import com.stewsters.worldgen.map.OverWorld;
import com.stewsters.worldgen.map.OverworldChunk;

import java.util.Random;
import java.util.logging.Logger;

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

    public OverworldChunk generate(OverWorld overWorld, int chunkX, int chunkY) {


        log.info("generating " + chunkX + " : " + chunkY);

        OverworldChunk overworldChunk = new OverworldChunk(chunkX, chunkY);

        int xCenter = overWorld.xSize * OverworldChunk.chunkSize / 2;
        int yCenter = overWorld.ySize * OverworldChunk.chunkSize / 2;

        for (int x = 0; x < OverworldChunk.chunkSize; x++) {
            for (int y = 0; y < OverworldChunk.chunkSize; y++) {

                //global coord
                int nx = chunkX * OverworldChunk.chunkSize + x;
                int ny = chunkY * OverworldChunk.chunkSize + y;

                float xDist = (float) Math.abs(nx - xCenter) / xCenter;
                float yDist = (float) Math.abs(ny - yCenter) / yCenter;

                // Elevation - decreases near edges
                double elevation = 0.6 * el.eval(nx / 120.0, ny / 120.0)
                        + 0.3 * el.eval(nx / 42.0, ny / 42.0)
                        + 0.1 * el.eval(nx / 10.0, ny / 10.0);

                overworldChunk.elevation[x][y] = Math.max(-1, Math.min(1, (float) (elevation - Math.pow(yDist, 10) - Math.pow(xDist, 10))));


                // Precipitation
                overworldChunk.precipitation[x][y] = (float) (
                        (0.75 * mo.eval(nx / 70.0, ny / 70.0) +
                                0.25 * mo.eval(nx / 45.0, ny / 45.0)

                        ) / 2.f) + 0.5f;

                // Temperature
                //decreases with height, decreases with closeness to poles


                overworldChunk.temperature[x][y] = 1 - Math.max(yDist, overworldChunk.elevation[x][y])
                        - 0.4f * yDist
                        - 0.4f * overworldChunk.elevation[x][y]
                        + 0.2f * (float) el.eval(1.0 * nx / 125.0, 1.0 * ny / 125.0);
                ;

                // Drainage


            }
        }
        return overworldChunk;

    }


}
