package com.stewsters.worldgen.procGen;

import com.stewsters.util.pathing.twoDimention.pathfinder.AStarHeuristic2d;
import com.stewsters.util.pathing.twoDimention.shared.TileBasedMap2d;

public class RoadRunnerHeuristic implements AStarHeuristic2d {

    @Override
    public float getCost(TileBasedMap2d map, int x, int y, int tx, int ty) {
        return 1;
    }
}
