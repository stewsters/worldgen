package com.stewsters.worldgen.screens;


import com.stewsters.worldgen.game.Leader;
import com.stewsters.worldgen.map.overworld.OverWorld;
import com.stewsters.worldgen.messageBus.Bus;
import squidpony.squidgrid.gui.swing.SwingPane;

import java.awt.event.KeyEvent;

public class GenerationScreen implements Screen {

    private OverWorld overWorld;
    private Leader player;
    private int screenWidth;
    private int screenHeight;


    public GenerationScreen() {
        screenWidth = 80;
        screenHeight = 40;

//        player = Leader.build(
//                overWorld.xSize * OverWorldChunk.chunkSize / 2,
//                overWorld.ySize * OverWorldChunk.chunkSize / 2
//        );

    }


    @Override
    public void displayOutput(SwingPane display) {

        overWorld = new OverWorld(32, 16);

        Bus.bus.post(overWorld).asynchronously();

        display.placeHorizontalString(1, 2, "Done");


    }


    @Override
    public Screen respondToUserInput(KeyEvent key) {
        switch (key.getKeyCode()) {

            case KeyEvent.VK_SPACE:
                return new IntroScreen(overWorld);
            case KeyEvent.VK_E:
                Bus.bus.post(overWorld).asynchronously();
                break;
        }

        return this;
    }
}
