package com.stewsters.worldgen.procGen;

import com.stewsters.worldgen.map.OverworldChunk;
import com.stewsters.util.noise.OpenSimplexNoise;

import java.util.logging.Logger;

public class WorldGenerator {

    private final static Logger log = Logger.getLogger(WorldGenerator.class.getName());
    OpenSimplexNoise el = new OpenSimplexNoise();
    OpenSimplexNoise mo = new OpenSimplexNoise();

    public OverworldChunk generate(long chunkX, long chunkY) {

        log.info("generating " + chunkX + " : " + chunkY);

        OverworldChunk overworldChunk = new OverworldChunk(chunkX, chunkY);

        for (int x = 0; x < OverworldChunk.chunkSize; x++) {
            for (int y = 0; y < OverworldChunk.chunkSize; y++) {

                long nx = chunkX * OverworldChunk.chunkSize + x;
                long ny = chunkY * OverworldChunk.chunkSize + y;

                // Elevation
                double elevation = 1 * el.eval(1.0 * nx / 100.0, 1.0 * ny / 100.0)
                        + 0.5 * el.eval(2.0 * nx / 100.0, 2.0 * ny / 100.0)
                        + 0.1 * el.eval(nx * (4.0 / 10.0), 4.0 * ny / 10.0);

                overworldChunk.elevation[x][y] = (float) Math.pow(elevation, 3.0);


                // Precipitation
                overworldChunk.precipitation[x][y] = (float) (1 * mo.eval(1.0 * nx / 30.0, 1.0 * ny / 30.0)
                        + 0.5 * mo.eval(2.0 * nx / 30.0, 2.0 * ny / 30.0)
                        + 0.25 * mo.eval(nx * (4.0 / 30.0), 4.0 * ny / 30.0));

                // Temperature
                //decreases with height, decreases with closeness to poles

                // Drainage


            }
        }
        return overworldChunk;

    }


}
