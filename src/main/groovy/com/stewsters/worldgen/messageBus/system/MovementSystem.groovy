package com.stewsters.worldgen.messageBus.system

import com.stewsters.worldgen.game.Leader
import com.stewsters.worldgen.messageBus.Bus
import com.stewsters.worldgen.messageBus.message.MovementMsg
import net.engio.mbassy.listener.Handler


class MovementSystem {

    @Handler
    public void handle(MovementMsg msg) {

        //find entity in array
        Leader leader = Leader.get(msg.id)

        if (!leader)
            return

        // move the entity
        Bus.bus.post("Moved ${msg.id} ${msg.direction.toString()}" as String).asynchronously()

        int fx = leader.pos.x + msg.direction.x;
        int fy = leader.pos.y + msg.direction.y;

//        if (!overWorld.getTileType(fx, fy).blocks) {
        leader.pos.x = fx;
        leader.pos.y = fy;
//        }

    }
}
