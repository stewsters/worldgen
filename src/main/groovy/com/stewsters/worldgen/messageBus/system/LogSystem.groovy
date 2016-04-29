package com.stewsters.worldgen.messageBus.system

import net.engio.mbassy.listener.Handler


class LogSystem {

    @Handler
    public void handle(String msg) {

        // move the entity
        println msg
        // do something with the file
    }
}
