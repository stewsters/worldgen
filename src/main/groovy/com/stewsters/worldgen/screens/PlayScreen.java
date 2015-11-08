package com.stewsters.worldgen.screens;


import com.stewsters.util.math.Point2i;
import com.stewsters.worldgen.game.Leader;
import com.stewsters.worldgen.map.OverWorld;
import com.stewsters.worldgen.map.OverworldChunk;
import com.stewsters.worldgen.map.TileType;
import squidpony.squidcolor.SColor;
import squidpony.squidgrid.gui.swing.SwingPane;

import java.awt.event.KeyEvent;

public class PlayScreen implements Screen {
    private OverWorld overWorld;
    private Leader player;
    private int screenWidth;
    private int screenHeight;


    public PlayScreen() {
        screenWidth = 80;
        screenHeight = 40;

        overWorld = new OverWorld(10, 10);
        player = new Leader(overWorld, new Point2i(
                overWorld.xSize * OverworldChunk.chunkSize / 2,
                overWorld.xSize * OverworldChunk.chunkSize / 2
        ));

    }

//    public void flush() {
//        overWorld.flush();
//    }

    @Override
    public void displayOutput(SwingPane display) {

        int left = player.pos.x - (screenWidth / 2);
        int top = player.pos.y - (screenHeight / 2);

        for (int x = 0; x < screenWidth; x++) {
            for (int y = 0; y < screenHeight; y++) {
                int wx = x + left;
                int wy = y + top;

                TileType tileType = overWorld.getTileType(wx, wy);
                display.placeCharacter(x, y, tileType.character, tileType.color);
            }
        }

        display.placeCharacter(player.pos.x - left, player.pos.y - top, '@', SColor.WHITE);

        display.placeHorizontalString(1, screenHeight + 2, overWorld.getTileType(player.pos.x, player.pos.y).name());

        display.placeHorizontalString(1, screenHeight + 3, "Elev: " + String.format("%.2f ft", overWorld.getElevation(player.pos.x, player.pos.y) * 30000));

        display.placeHorizontalString(1, screenHeight + 4, "Temp: " + String.format("%.2f°F", overWorld.getTemp(player.pos.x, player.pos.y) * 100 + 30));

        display.placeHorizontalString(1, screenHeight + 5, "Lat: " + String.format("%.2f°", -overWorld.getLatitude(player.pos.y) * 90));

        display.placeHorizontalString(15, screenHeight + 5, "Lon:" + String.format("%.2f°", overWorld.getLongitude(player.pos.x) * 180));

    }


    @Override
    public Screen respondToUserInput(KeyEvent key) {
        switch (key.getKeyCode()) {

            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_H:
            case KeyEvent.VK_NUMPAD4:
                player.moveBy(-1, 0, 0);
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_L:
            case KeyEvent.VK_NUMPAD6:
                player.moveBy(1, 0, 0);
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_K:
            case KeyEvent.VK_NUMPAD8:
                player.moveBy(0, -1, 0);
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_J:
            case KeyEvent.VK_NUMPAD2:
                player.moveBy(0, 1, 0);
                break;
            case KeyEvent.VK_Y:
            case KeyEvent.VK_NUMPAD7:
                player.moveBy(-1, -1, 0);
                break;
            case KeyEvent.VK_U:
            case KeyEvent.VK_NUMPAD9:
                player.moveBy(1, -1, 0);
                break;
            case KeyEvent.VK_B:
            case KeyEvent.VK_NUMPAD1:
                player.moveBy(-1, 1, 0);
                break;
            case KeyEvent.VK_N:
            case KeyEvent.VK_NUMPAD3:
                player.moveBy(1, 1, 0);
                break;
            case KeyEvent.VK_PERIOD:
                player.moveBy(0, 0, 1);
                break;
            case KeyEvent.VK_COMMA:
                player.moveBy(0, 0, -1);
                break;
        }
        overWorld.update();

        return this;
    }
}
