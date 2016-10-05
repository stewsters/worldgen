package com.stewsters.worldgen.procGen;

import com.stewsters.util.pathing.twoDimention.shared.Mover2d;
import com.stewsters.worldgen.map.overworld.OverWorld;

public class RoadRunnerMover implements Mover2d {

    OverWorld overWorld;

    public RoadRunnerMover(OverWorld overWorld) {
        this.overWorld = overWorld;
    }

    @Override
    public boolean canTraverse(int sx, int sy, int tx, int ty) {

        return overWorld.getElevation(tx, ty) > 0;
        //overWorld.getTileType(tx, ty).water;
    }

    @Override
    public boolean canOccupy(int tx, int ty) {
//        return !overWorld.getTileType(tx, ty).water;
        return overWorld.getElevation(tx, ty) > 0;
    }

    @Override
    public float getCost(int sx, int sy, int tx, int ty) {

        return (overWorld.getRoad(tx, ty) ? 1f : 4f)
                * (((tx == sx) || (ty == tx)) ? 1f : 1.414f);
//                +  Math.max(0,overWorld.getElevation(tx,ty));

    }
}
