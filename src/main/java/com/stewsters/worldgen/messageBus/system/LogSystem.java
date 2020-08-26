package com.stewsters.worldgen.messageBus.system;

import net.engio.mbassy.listener.Handler;


public class LogSystem {

    @Handler
    public void handle(String msg) {

        // move the entity
        System.out.println(msg);
        // do something with the file
    }
}
