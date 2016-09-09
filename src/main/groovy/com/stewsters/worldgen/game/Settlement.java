package com.stewsters.worldgen.game;


import com.stewsters.util.math.MatUtils;
import com.stewsters.util.math.Point2i;

import java.util.ArrayList;

public class Settlement {

    private static int topId = 0;
    public static ArrayList<Settlement> settlements = new ArrayList<Settlement>();

    public int id;
    public Point2i pos;
    public int population;

    private Settlement(int id, Point2i point2i) {
        this.id = id;
        this.pos = point2i;
        this.population = MatUtils.d(10000);
    }


    public static Settlement build(int x, int y) {

        Settlement settlement = new Settlement(topId, new Point2i(x, y));

        settlements.add(topId++, settlement);
        return settlement;
    }

    public static Settlement get(int id) {
        return settlements.get(id);
    }

}
