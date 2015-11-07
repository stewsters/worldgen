package com.stewsters.worldgen.messageBus.system

import com.stewsters.worldgen.messageBus.message.MovementMsg
import net.engio.mbassy.listener.Handler


class MovementSystem {

    @Handler
    public void handle(MovementMsg msg){



        // move the entity
        println "Moved ${msg.id} ${msg.direction.toString()}"
        // do something with the file
    }
}
