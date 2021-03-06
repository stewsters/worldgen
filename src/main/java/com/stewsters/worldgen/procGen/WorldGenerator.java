package com.stewsters.worldgen.procGen;

import com.stewsters.util.math.Facing2d;
import com.stewsters.util.math.MatUtils;
import com.stewsters.util.math.Point2i;
import com.stewsters.util.noise.OpenSimplexNoise;
import com.stewsters.util.pathing.twoDimention.heuristic.ClosestHeuristic2d;
import com.stewsters.util.pathing.twoDimention.pathfinder.AStarPathFinder2d;
import com.stewsters.worldgen.game.Settlement;
import com.stewsters.worldgen.map.BiomeType;
import com.stewsters.worldgen.map.overworld.OverWorld;
import com.stewsters.worldgen.map.overworld.OverWorldChunk;
import com.stewsters.worldgen.messageBus.Bus;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static com.stewsters.util.math.MatUtils.d;
import static com.stewsters.util.math.MatUtils.euclideanDistance;

public class WorldGenerator {

    private final static Logger log = Logger.getLogger(WorldGenerator.class.getName());
    private static final int uncalculatedRegion = -1;
    private static final int blockedRegion = -2;
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

    public void generateElevation(OverWorld overWorld) {
        Bus.bus.post("Beginning Chunk Elevation").now();

        Line2D line = new Line2D.Float(new Point2D.Float(00, overWorld.getPreciseYSize()), new Point2D.Float(overWorld.getPreciseXSize(), 0));
        ShapeMod mod = (x, y) -> (1.2 - Math.max(line.ptLineDist(x, y) / 160f, 1));
//        ShapeMod mod = (x,y)-> 1 - euclideanDistance(x,y, overWorld.xSize/2, overWorld.ySize/2)/overWorld.xSize ;

        IntStream.range(0, overWorld.xSize).parallel().forEach(x -> {
            IntStream.range(0, overWorld.ySize).parallel().forEach(y -> {
                overWorld.chunks[x][y] = generateChunkedHeightMap(overWorld, Arrays.asList(mod), x, y);
            });
        });
        Bus.bus.post("Finished Chunk Elevation").now();
    }

    // Initial step the can be done per chunk
    public OverWorldChunk generateChunkedHeightMap(OverWorld overWorld, List<ShapeMod> shapeMods, int chunkX, int chunkY) {

        OverWorldChunk overWorldChunk = new OverWorldChunk();

        for (int x = 0; x < OverWorldChunk.chunkSize; x++) {
            for (int y = 0; y < OverWorldChunk.chunkSize; y++) {

                //global coord
                int nx = chunkX * OverWorldChunk.chunkSize + x;
                int ny = chunkY * OverWorldChunk.chunkSize + y;

                double ridginess = fbm(nx, ny, 6, 1 / 320D, 1, 2D, 0.5D);
                ridginess = Math.abs(ridginess) * -1;

                double elevation = Math.max((fbm(nx, ny, 6, 1 / 200D, 1, 2D, 0.5D)), (ridginess));

                for (ShapeMod mod : shapeMods) {
                    elevation += mod.modify(nx, ny);
                }

                overWorldChunk.elevation[x][y] = (float) elevation;
            }
        }
        return overWorldChunk;
    }

    public double fbm(double x, double y, int octaves, double frequency, double amplitude, double lacunarity, double gain) {
        double total = 0.0;
        for (int i = 0; i < octaves; ++i) {
            total += el.eval(x * frequency, y * frequency) * amplitude;
            frequency *= lacunarity;
            amplitude *= gain;
        }
        return total;
    }

    public void generateTemperature(OverWorld overWorld) {
        Bus.bus.post("Starting Chunk Temperature").now();
        IntStream.range(0, overWorld.xSize).parallel().forEach(x -> {
            IntStream.range(0, overWorld.ySize).parallel().forEach(y -> {
                overWorld.chunks[x][y] = generateChunkedTemperatureMap(overWorld, x, y);
            });
        });

        Bus.bus.post("Finished Chunk Temperature").now();
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
                        1.2f * (1.f - yDist)
                                - Math.max(0f, overWorldChunk.elevation[x][y])
                                + 0.1f * (float) el.eval(1.0 * nx / 125.0, 1.0 * ny / 125.0),
                        -1, 1);
            }
        }
        return overWorldChunk;
    }

    public void evenElevation(OverWorld overWorld) {

        Bus.bus.post("Start Evening").now();
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
        Bus.bus.post("Finished Evening").now();
    }

    public void dropEdges(OverWorld overWorld) {
        Bus.bus.post("Start Drop Edges").now();
        int xSize = overWorld.getPreciseXSize();
        int ySize = overWorld.getPreciseYSize();


        int xCenter = overWorld.xSize * OverWorldChunk.chunkSize / 2;
        int yCenter = overWorld.ySize * OverWorldChunk.chunkSize / 2;

        double maxDist = Math.sqrt(xCenter * xCenter + yCenter * yCenter);

        for (int y = 0; y < ySize; y++) {
            for (int x = 0; x < xSize; x++) {
                double elev = overWorld.getElevation(x, y);
                double dist = Math.sqrt(Math.pow(xCenter - x, 2) + Math.pow(yCenter - y, 2));

                // Elevation - decreases near edges
                elev = MatUtils.limit(elev - (Math.pow(dist / maxDist, 4)), -1, 1);
                overWorld.setElevation(x, y, (float) elev);
            }
        }
        Bus.bus.post("End Drop Edges").now();

    }

    // Run rivers

    public void generateWind(OverWorld overWorld) {
        Bus.bus.post("Starting Wind").now();

        int ySize = overWorld.getPreciseYSize();
        float periods = 4;

        IntStream.range(0, overWorld.xSize).parallel().forEach(xChunk -> {
            IntStream.range(0, overWorld.ySize).parallel().forEach(yChunk -> {

                OverWorldChunk overWorldChunk = overWorld.loadChunk(xChunk, yChunk);
                for (int y = 0; y < OverWorldChunk.chunkSize; y++) {
                    int ny = yChunk * OverWorldChunk.chunkSize + y;

                    float percentage = (float) ny / ySize;
                    float globalWindX = (float) Math.cos(periods * (2 * Math.PI) * percentage);
                    for (int x = 0; x < OverWorldChunk.chunkSize; x++) {

                        int nx = xChunk * OverWorldChunk.chunkSize + x;

                        float temp = overWorld.getTemp(nx, ny);
                        float xd = temp - overWorld.getTemp(nx + 1, ny);
                        float yd = temp - overWorld.getTemp(nx, ny + 1);

                        // Rotate 90 degrees
                        float windX = yd;
                        float windY = -xd;

                        windX = (50f * windX) - (0.5f * globalWindX);
                        windY = (50f * windY) + (0.5f * globalWindX);

                        overWorldChunk.windX[x][y] = windX;
                        overWorldChunk.windY[x][y] = windY;

                    }
                }

            });
        });

        Bus.bus.post("Finished Wind").now();
    }

    // This is a hack for rain shadows.  Start at your square, then go upwind
    // warm temp on water squares boosts humidity, cold mountains limit it
    public void generatePrecipitation(OverWorld overWorld) {
        Bus.bus.post("Starting Precipitation").now();
        int xSize = overWorld.getPreciseXSize();
        int ySize = overWorld.getPreciseYSize();

        final float maxDistance = 1000f;

        float[][] precip = new float[xSize][ySize];

        IntStream.range(0, xSize).parallel().forEach(x -> {
            IntStream.range(0, ySize).parallel().forEach(y -> {

                float tempX = x;
                float tempY = y;

                float distanceLeft = maxDistance;
                while (distanceLeft > 0) {

                    if (overWorld.isOutside((int) tempX, (int) tempY)) break;

                    BiomeType bt = overWorld.getTileType((int) tempX, (int) tempY);
                    if (bt.water) break;

                    distanceLeft--;

                    float windX = overWorld.getWindX((int) tempX, (int) tempY);
                    float windY = overWorld.getWindY((int) tempX, (int) tempY);

                    tempX += windX;
                    tempY += windY;
                }

                precip[x][y] = distanceLeft / maxDistance;

//                overWorld.setPrecipitation(x, y, distanceLeft / maxDistance);
            });
        });

        //Blur to get rid of tight swirls
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {

                float val = 0f;
                for (int xD = -5; xD <= 5; xD++) {
                    val += precip[MatUtils.limit(x + xD, 0, xSize - 1)][y];
                }
                precip[x][y] = val / 10f;
            }

        }
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {

                float val = 0f;
                for (int yD = -5; yD <= 5; yD++) {
                    val += precip[x][MatUtils.limit(y + yD, 0, ySize - 1)];
                }
                precip[x][y] = val / 10f;
            }
        }
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                overWorld.setPrecipitation(x, y, precip[x][y]);
            }
        }
        Bus.bus.post("Finished Precipitation").now();

    }

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
        Bus.bus.post("Starting Rivers").now();

        int xSize = overWorld.getPreciseXSize();
        int ySize = overWorld.getPreciseYSize();

        for (int i = 0; i < 5; i++) {
            float[][] riverFlux = new float[xSize][ySize];

            IntStream.range(0, xSize).parallel().forEach(x -> {
                        IntStream.range(0, ySize).parallel().forEach(y -> {
                                    float flow = overWorld.getPrecipitation(x, y) + 0.1f;

                                    int tempX = x;
                                    int tempY = y;

                                    while (true) {

                                        if (overWorld.isOutside(tempX, tempY))
                                            break;

                                        // if the biome is ocean or frozen then end.
//                        BiomeType existingType = overWorld.getTileType(tempX, tempY);

//                        if (existingType == BiomeType.OCEAN_ABYSSAL || existingType == BiomeType.OCEAN_DEEP || existingType == BiomeType.OCEAN_SHALLOW) {
//                            break;
//                        }

                                        Facing2d facing = null;
                                        float height = overWorld.getElevation(tempX, tempY);

                                        for (Facing2d facing2d : Facing2d.values()) {
                                            float potentialHeight = overWorld.getElevation(tempX + facing2d.x, tempY + facing2d.y);
                                            if (height > potentialHeight) {
                                                facing = facing2d;
                                                height = potentialHeight;
                                            }
                                        }

                                        if (facing == null) {
                                            break;//TODO: local minima lake
                                        } else {
                                            riverFlux[tempX][tempY] += flow;

                                            tempX = tempX + facing.x;
                                            tempY = tempY + facing.y;
                                        }
                                    }
                                }
                        );
                    }
            );

            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {

                    float val = 0f;
                    for (int xD = -5; xD <= 5; xD++) {
                        val += riverFlux[MatUtils.limit(x + xD, 0, xSize - 1)][y] / ((Math.abs(xD) + 1));
                    }
                    riverFlux[x][y] = val / 3.9f;
                }

            }
            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {

                    float val = 0f;
                    for (int yD = -5; yD <= 5; yD++) {
                        val += riverFlux[x][MatUtils.limit(y + yD, 0, ySize - 1)] / ((Math.abs(yD) + 1));
                    }
                    riverFlux[x][y] = val / 3.9f;
                }
            }

            float maxFlux = 0;

            //Find maxflux to get
            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {
                    maxFlux = Math.max(maxFlux, (float) Math.sqrt(riverFlux[x][y]));
                }
            }

            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {
                    float f = overWorld.getElevation(x, y);
                    overWorld.setElevation(x, y, Math.max(-1, f - ((0.2f) * (float) Math.sqrt(Math.max(riverFlux[x][y] - 0.1, 0)) / maxFlux)));
                }
            }

            for (int x = 0; x < xSize; x++) {
                for (int y = 0; y < ySize; y++) {

                    if (riverFlux[x][y] > maxFlux / 2f && !overWorld.getTileType(x, y).water) {
                        overWorld.setRiver(x, y);
                    }

                }
            }
        }

        Bus.bus.post("Finished Rivers").now();

    }

    /**
     * Build Settlements
     * <p>
     * Human settlements should be built near a source of water, preferably a river.
     * Most should be built at a low elevation, near an area with high river flux
     * Generating a good distance from other towns
     * <p>
     * Population should follow https://en.wikipedia.org/wiki/Zipf%27s_law
     *
     * @param overWorld
     */
    public void populateSettlements(OverWorld overWorld) {
        Bus.bus.post("Populating Settlements").now();
        int xSize = overWorld.getPreciseXSize();
        int ySize = overWorld.getPreciseYSize();
        int totalSettlements = 20;
        int propositions = 1000;

        // Build Settlements
        for (int i = 0; i < totalSettlements; i++) {

            ArrayList<Point2i> possibleLocations = new ArrayList<>();
            int pop = d(1000);

            // Propose new settlement locations
            for (int j = 0; j < propositions; j++) {
                int x = MatUtils.getIntInRange(0, xSize);
                int y = MatUtils.getIntInRange(0, ySize);
                possibleLocations.add(new Point2i(x, y));
            }

            float bestScore = Float.NEGATIVE_INFINITY;
            Point2i bestLocal = null;

            for (Point2i p : possibleLocations) {

                float score = 0;
                BiomeType biomeType = overWorld.getTileType(p.x, p.y);

                if (biomeType.water) {
                    score -= 1000;
                }

                for (Settlement s : Settlement.settlements) {
                    float dist = p.getChebyshevDistance(s.pos);
                    score -= s.population * pop / (dist * dist);
                }

                float percip = overWorld.getPrecipitation(p.x, p.y);

                score += percip * 10;

                if (score > bestScore) {
                    bestScore = score;
                    bestLocal = p;
                }

            }

            if (bestLocal != null) {
                Bus.bus.post(bestLocal.toString()).now();
                overWorld.buildSettlement(bestLocal.x, bestLocal.y, pop);
            }
        }
        Bus.bus.post("Finished Populating Settlements").now();
    }

    // http://roadtrees.com/creating-road-trees/
    public void createRoadNetwork(OverWorld overWorld) {
        //Step One Determine which cities to link together first. Larger closer cities should go first

        Bus.bus.post("Creating Roads").now();
        HashMap<Integer, ArrayList<Settlement>> settlementsPerContinent = new HashMap<>();

        for (Settlement settlement : Settlement.settlements) {
            int id = overWorld.getRegionId(settlement.pos.x, settlement.pos.y);

            ArrayList<Settlement> settlements = settlementsPerContinent.get(id);
            if (settlements == null) {
                settlements = new ArrayList<>();
                settlementsPerContinent.put(id, settlements);
            }
            settlements.add(settlement);
        }

        settlementsPerContinent.values().parallelStream().forEach(settlements -> {

            ArrayList<RankedSettlementPair> pairs = new ArrayList<>();

            for (int a = 0; a < settlements.size() - 1; a++) {
                for (int b = a + 1; b < settlements.size(); b++) {
                    pairs.add(new RankedSettlementPair(settlements.get(a), settlements.get(b)));
                }
            }
            Collections.sort(pairs);

            AStarPathFinder2d pathFinder2d = new AStarPathFinder2d(overWorld, overWorld.getPreciseXSize() * overWorld.getPreciseYSize());
//            Mover2d mover2d = new RoadRunnerMover(overWorld);

            final float offroadMult = 2f;
            final float hillClimb = 500f;
            for (RankedSettlementPair p : pairs) {
                Bus.bus.post("Distance " + p.distance + " a:" + p.a + " b:" + p.b).now();

                Settlement a = Settlement.settlements.get(p.a);
                Settlement b = Settlement.settlements.get(p.b);
                Optional<List<Point2i>> path = pathFinder2d.findPath(
                        (int sx, int sy, int tx, int ty) -> overWorld.getElevation(tx, ty) > 0,
                        (int tx, int ty) -> overWorld.getElevation(tx, ty) > 0,
                        (int sx, int sy, int tx, int ty) -> {
                            // The difference in elevation is absolutely tiny. so penalize any change drastically.
                            return (overWorld.getRoad(tx, ty) ? 1f : offroadMult) // existing roads are less expensive
                                    * (((tx == sx) || (ty == tx)) ? 0.7f : 1f) // diagonal
                                    * (1 + (hillClimb * Math.abs(overWorld.getElevation(tx, ty) - overWorld.getElevation(sx, sy))));
                        },
                        new ClosestHeuristic2d(),
                        true,
                        a.pos.x, a.pos.y, b.pos.x, b.pos.y);

                if (path.isPresent()) {

                    for (Point2i step : path.get()) {
                        overWorld.setRoad(step.x, step.y);
                    }

                }
            }

        });


        // Use A* to link cities.  Cost should reflect slope and terrain type.  Bridges are possible, but expensive.

        //The first cities that are linked will have busy roads, and they will shrink down as

        Bus.bus.post("Finished Creating Roads").now();
    }

    // This finds contiguous land masses
    public void generateContinents(OverWorld overWorld) {

        Bus.bus.post("Generating Continents").now();
        //Reset
        for (int x = 0; x < overWorld.getPreciseXSize(); x++) {
            for (int y = 0; y < overWorld.getPreciseYSize(); y++) {
                if (overWorld.getElevation(x, y) < 0)
                    overWorld.setRegion(x, y, blockedRegion);
                else
                    overWorld.setRegion(x, y, uncalculatedRegion);
            }
        }

        int i = 0;
        for (int x = 0; x < overWorld.getPreciseXSize(); x++) {
            for (int y = 0; y < overWorld.getPreciseYSize(); y++) {
                if (overWorld.getRegionId(x, y) == uncalculatedRegion) {
                    floodFillBFS(overWorld, x, y, uncalculatedRegion, i++);
                }
            }
        }
        Bus.bus.post("Finished Generating Continents").now();
    }

    private void floodFillBFS(OverWorld overWorld, int sx, int sy, int target, int replacement) {

        Point2i q = new Point2i(sx, sy);
        int xSize = overWorld.getPreciseXSize();
        int ySize = overWorld.getPreciseYSize();

        if (q.y < 0 || q.y >= ySize || q.x < 0 || q.x >= xSize)
            return;

        Deque<Point2i> stack = new ArrayDeque<>();
        stack.push(q);
        while (stack.size() > 0) {
            Point2i p = stack.pop();
            int x = p.x;
            int y = p.y;
            if (y < 0 || y >= ySize || x < 0 || x >= xSize)
                continue;
            int val = overWorld.getRegionId(x, y);
            if (val == target) {
                overWorld.setRegion(x, y, replacement);

                if (x + 1 < xSize && overWorld.getRegionId(x + 1, y) == target)
                    stack.push(new Point2i(x + 1, y));
                if (x - 1 > 0 && overWorld.getRegionId(x - 1, y) == target)
                    stack.push(new Point2i(x - 1, y));
                if (y + 1 < ySize && overWorld.getRegionId(x, y + 1) == target)
                    stack.push(new Point2i(x, y + 1));
                if (y - 1 > 0 && overWorld.getRegionId(x, y - 1) == target)
                    stack.push(new Point2i(x, y - 1));
            }
        }

    }

    public void expandRealms(OverWorld overWorld) {
        int[][] realm;

        // select empire starting points

        // set them as their respective


    }
}

class RankedSettlementPair implements Comparable<RankedSettlementPair> {
    int a;
    int b;
    double distance;

    public RankedSettlementPair(Settlement a, Settlement b) {
        this.a = a.id;
        this.b = b.id;

        distance = (a.population * b.population / Math.pow(a.pos.getChebyshevDistance(b.pos), 2));
    }

    @Override
    public int compareTo(RankedSettlementPair o) {
        if (distance == o.distance) {
            return 0;
        }
        return distance - o.distance > 0 ? 1 : -1;
    }
}
