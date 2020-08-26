package com.stewsters.worldgen.messageBus.system;

import com.stewsters.worldgen.game.Leader;
import com.stewsters.worldgen.messageBus.message.MovementMsg;
import net.engio.mbassy.listener.Handler;


public class MovementSystem {

    @Handler
    public void handle(MovementMsg msg) {

        //find entity in array
        Leader leader = Leader.get(msg.id);

        if (leader == null)
            return;

        // move the entity
//        Bus.bus.post("Moved ${msg.id} ${msg.direction.toString()}" as String).asynchronously()

        int fx = leader.pos.x + msg.direction.x;
        int fy = leader.pos.y + msg.direction.y;

//        if (!overWorld.getTileType(fx, fy).blocks) {
//            return;
//        }

        leader.pos.x = fx;
        leader.pos.y = fy;


    }
}
