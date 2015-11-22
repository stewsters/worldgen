package com.stewsters.worldgen.messageBus.message

import com.stewsters.worldgen.map.OverWorld

/**
 * Created by stewsters on 11/11/15.
 */
public class PrintMapMsg {
    public OverWorld overWorld;

    public PrintMapMsg(OverWorld overWorld) {
        this.overWorld = overWorld
    }
}
