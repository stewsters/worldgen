package com.stewsters.worldgen.messageBus.message

import com.stewsters.worldgen.map.overworld.OverWorld

public class PrintMapMsg {
    public OverWorld overWorld;

    public PrintMapMsg(OverWorld overWorld) {
        this.overWorld = overWorld
    }
}
