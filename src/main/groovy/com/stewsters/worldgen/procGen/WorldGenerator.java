package com.stewsters.worldgen.procGen;

import com.stewsters.util.noise.OpenSimplexNoise;
import com.stewsters.worldgen.map.OverWorld;
import com.stewsters.worldgen.map.OverworldChunk;

import java.util.logging.Logger;

public class WorldGenerator {

    private final static Logger log = Logger.getLogger(WorldGenerator.class.getName());
    OpenSimplexNoise el = new OpenSimplexNoise();
    OpenSimplexNoise mo = new OpenSimplexNoise();

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
                double elevation = 1 * el.eval(1.0 * nx / 100.0, 1.0 * ny / 100.0)
                        + 0.5 * Math.pow(el.eval(2.0 * nx / 85.0, 2.0 * ny / 85.0), 2)
                        + 0.1 * Math.pow(el.eval(4.0 * nx / 10.0, 4.0 * ny / 10.0), 3);

                overworldChunk.elevation[x][y] = (float) (Math.pow(elevation, 2.0) - Math.pow(yDist, 10) - Math.pow(xDist, 10));


                // Precipitation
                overworldChunk.precipitation[x][y] = (float) (
                        (0.75 * mo.eval(nx / 70.0, ny / 70.0) +
                                0.25 * mo.eval(nx / 45.0, ny / 45.0)

                        ) / 2.f) + 0.5f;

                // Temperature
                //decreases with height, decreases with closeness to poles


                overworldChunk.temperature[x][y] =
                        (0.75f - (1.5f * yDist)) - Math.max(0, (overworldChunk.elevation[x][y] / 2))
                                + 0.1f * (float) el.eval(1.0 * nx / 125.0, 1.0 * ny / 125.0);
                ;

                // Drainage


            }
        }
        return overworldChunk;

    }


}
