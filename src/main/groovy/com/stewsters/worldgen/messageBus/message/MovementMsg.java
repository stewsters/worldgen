package com.stewsters.worldgen.messageBus.message;

import com.stewsters.util.math.Facing2d;


public class MovementMsg {
    public int id;
    public Facing2d direction;

    public MovementMsg(int id, Facing2d direction) {
        this.id = id;
        this.direction = direction;
    }
}
