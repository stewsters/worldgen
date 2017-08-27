package com.stewsters.worldgen.procGen;

import com.stewsters.util.pathing.twoDimention.pathfinder.AStarHeuristic2d;
import com.stewsters.util.pathing.twoDimention.pathfinder.ClosestHeuristic2d;
import com.stewsters.util.pathing.twoDimention.shared.Mover2d;
import com.stewsters.worldgen.map.overworld.OverWorld;

public class RoadRunnerMover implements Mover2d {

    final float offroadMult = 2f;
    final float hillClimb = 1000f;
    AStarHeuristic2d heuristic2d = new ClosestHeuristic2d();
    OverWorld overWorld;

    public RoadRunnerMover(OverWorld overWorld) {
        this.overWorld = overWorld;
    }

    @Override
    public boolean canTraverse(int sx, int sy, int tx, int ty) {
        return overWorld.getElevation(tx, ty) > 0;
    }

    @Override
    public boolean canOccupy(int tx, int ty) {
        return overWorld.getElevation(tx, ty) > 0;
    }

    @Override
    public float getCost(int sx, int sy, int tx, int ty) {
        return ((overWorld.getRoad(tx, ty) ? 1f : offroadMult) // roads are less expensive
                * (((tx == sx) || (ty == tx)) ? 0.7f : 1f) // diagonal
                * (1 + (hillClimb * Math.abs(overWorld.getElevation(tx, ty) - overWorld.getElevation(sx, sy))))); // The difference in elevation is absolutely tiny. so penalize any change drastically.
    }

    @Override
    public AStarHeuristic2d getHeuristic() {
        return heuristic2d;
    }

    @Override
    public boolean getDiagonal() {
        return true;
    }
}
