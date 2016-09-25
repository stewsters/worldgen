package com.stewsters.worldgen.screens

import com.stewsters.worldgen.map.overworld.OverWorld
import squidpony.squidgrid.gui.swing.SwingPane

import java.awt.event.KeyEvent

public class IntroScreen implements Screen {

    OverWorld overWorld


    public IntroScreen() {
    }

    public IntroScreen(OverWorld overWorld) {
        this.overWorld = overWorld;
    }

    @Override
    void displayOutput(SwingPane display) {

        display.placeHorizontalString(1, 1, "Terrain Gen Example");
        display.placeHorizontalString(1, 3, "Push G to generate");
        if (overWorld)
            display.placeHorizontalString(1, 4, "Push P to Play");
    }

    @Override
    Screen respondToUserInput(KeyEvent key) {

        switch (key.getKeyCode()) {

            case KeyEvent.VK_G:
                return new GenerationScreen()
                break

            case KeyEvent.VK_P:
                if (overWorld)
                    return new OverWorldScreen(overWorld)
                break
        }

        return this
    }
}
