package com.stewsters.worldgen.game;


import com.stewsters.worldgen.map.OverWorld;
import com.stewsters.util.math.Point2i;

public class Leader {

    public OverWorld overWorld;
    public Point2i pos;

    public Leader(OverWorld overWorld, Point2i point2i) {
        this.overWorld = overWorld;
        this.pos = point2i;
    }

    public void moveBy(int dx, int dy, int dz) {

        int fx = pos.x + dx;
        int fy = pos.y + dy;

        if (!overWorld.getTileType(fx, fy).blocks) {
            pos.x = fx;
            pos.y = fy;
        }

    }
}
