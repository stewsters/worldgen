package com.stewsters.worldgen.procGen;

import com.stewsters.util.pathing.twoDimention.shared.Mover2d;
import com.stewsters.worldgen.map.overworld.OverWorld;

public class RoadRunnerMover implements Mover2d {

    OverWorld overWorld;

    final float offroadMult = 3f;
    final float hillClimb = 1000f;

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

//        overWorld.getTileType(tx, ty)

        return  ((overWorld.getRoad(tx, ty) ? 1f : offroadMult) // roads are less expensive
                * (((tx == sx) || (ty == tx)) ? 0.7f : 1f) // diagonal
                * (1 + (hillClimb * Math.abs(overWorld.getElevation(tx, ty) - overWorld.getElevation(sx, sy))))); // The difference in elevation is absolutely tiny. so penalize any change drastically.

    }
}
