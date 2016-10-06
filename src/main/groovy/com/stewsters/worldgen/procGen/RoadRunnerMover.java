package com.stewsters.worldgen.procGen;

import com.stewsters.util.pathing.twoDimention.shared.Mover2d;
import com.stewsters.worldgen.map.overworld.OverWorld;

public class RoadRunnerMover implements Mover2d {

    OverWorld overWorld;

    final float offroadMult = 3f;
    final float hillClimb = 10f;
    final float elevationPenalty = 2f;

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

        return (float) ((overWorld.getRoad(tx, ty) ? 1f : offroadMult) // roads are less expensive
                * (((tx == sx) || (ty == tx)) ? 1f : 1.41421356237f) // diagonal
                * (1 + Math.pow(hillClimb * (overWorld.getElevation(tx, ty) - overWorld.getElevation(sx, sy)), 2)))
                + elevationPenalty * overWorld.getElevation(tx, ty); // elevation changes are bad too

    }
}