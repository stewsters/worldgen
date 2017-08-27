package com.stewsters.worldgen.game;


import com.stewsters.util.math.Point2i;

import java.util.ArrayList;

public class Settlement {

    public static ArrayList<Settlement> settlements = new ArrayList<Settlement>();
    private static int topId = 0;
    public int id;
    public Point2i pos;
    public int population;

    private Settlement(int id, Point2i point2i, int pop) {
        this.id = id;
        this.pos = point2i;
        this.population = pop;
    }


    public static Settlement build(int x, int y, int pop) {

        Settlement settlement = new Settlement(topId, new Point2i(x, y), pop);

        settlements.add(topId++, settlement);
        return settlement;
    }

    public static Settlement get(int id) {
        return settlements.get(id);
    }

}
