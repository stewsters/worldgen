package com.stewsters.worldgen.game;


import com.stewsters.util.math.Point2i;

import java.util.ArrayList;

public class Leader {

    private static int topId = 0;
    private static ArrayList<Leader> leaders = new ArrayList<Leader>();

    public int id;
    public Point2i pos;

    private Leader(int id, Point2i point2i) {
        this.id = id;
        this.pos = point2i;
    }


    public static Leader build(int x, int y) {

        Leader leader = new Leader(topId, new Point2i(x, y));

        leaders.add(topId++, leader);
        return leader;
    }

    public static Leader get(int id) {
        return leaders.get(id);

    }

}
