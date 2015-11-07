package com.stewsters.worldgen.messageBus.message

import com.stewsters.util.math.Facing2d


public class MovementMsg {
    int id
    Facing2d direction

    MovementMsg(int id, Facing2d direction) {
        this.id = id
        this.direction = direction
    }
}
