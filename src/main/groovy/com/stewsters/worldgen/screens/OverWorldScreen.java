package com.stewsters.worldgen.screens;


import com.stewsters.util.math.Facing2d;
import com.stewsters.worldgen.game.Leader;
import com.stewsters.worldgen.map.BiomeType;
import com.stewsters.worldgen.map.overworld.OverWorld;
import com.stewsters.worldgen.map.overworld.OverWorldChunk;
import com.stewsters.worldgen.messageBus.Bus;
import com.stewsters.worldgen.messageBus.message.MovementMsg;
import squidpony.squidcolor.SColor;
import squidpony.squidgrid.gui.swing.SwingPane;

import java.awt.event.KeyEvent;

public class OverWorldScreen implements Screen {
    private OverWorld overWorld;
    private Leader player;
    private int screenWidth;
    private int screenHeight;


    public OverWorldScreen() {
        screenWidth = 80;
        screenHeight = 40;

        overWorld = new OverWorld(32, 16);
        player = Leader.build(
                overWorld.xSize * OverWorldChunk.chunkSize / 2,
                overWorld.ySize * OverWorldChunk.chunkSize / 2
        );

    }

    private int toWorldX(int screenX) {
        return player.pos.x - (screenWidth / 2) + screenX;
    }

    private int toWorldY(int screenY) {
        return player.pos.y + (screenHeight / 2) - screenY;
    }

    private int toScreenX(int worldX) {
        return worldX - (player.pos.x - (screenWidth / 2));
    }

    private int toScreenY(int worldY) {
        return -worldY + player.pos.y + (screenHeight / 2);
    }

    @Override
    public void displayOutput(SwingPane display) {

        int left = player.pos.x - (screenWidth / 2);
        int top = player.pos.y + (screenHeight / 2);

        for (int x = 0; x < screenWidth; x++) {
            for (int y = 0; y < screenHeight; y++) {
                //Get world coords for screen
                int wx = x + left;
                int wy = -y + top;

                if (overWorld.getSettlement(wx, wy) != null) {
                    display.placeCharacter(x, y, '#', SColor.AMBER);
                } else {

                    BiomeType biomeType = overWorld.getTileType(wx, wy);

                    float diff = overWorld.getElevation(wx, wy) - overWorld.getElevation(wx, wy + 1);

                    if (diff < -0.01) {
                        display.placeCharacter(x, y, biomeType.character, biomeType.darkColor);
                    } else if (diff > 0.01) {
                        display.placeCharacter(x, y, biomeType.character, biomeType.brightColor);
                    } else {
                        display.placeCharacter(x, y, biomeType.character, biomeType.color);
                    }

                }
            }
        }

        display.placeCharacter(player.pos.x - left, -player.pos.y + top, '@', SColor.WHITE);

        display.placeHorizontalString(1, screenHeight + 2, overWorld.getTileType(player.pos.x, player.pos.y).name());

        display.placeHorizontalString(1, screenHeight + 3, "Elev: " + String.format("%.2f ft", overWorld.getElevation(player.pos.x, player.pos.y) * 30000));

        display.placeHorizontalString(1, screenHeight + 4, "Temp: " + String.format("%.2f°F", overWorld.getTemp(player.pos.x, player.pos.y) * 100 + 30));

        display.placeHorizontalString(1, screenHeight + 5, "Prec: " + String.format("%.2f u", overWorld.getPrecipitation(player.pos.x, player.pos.y)));

        display.placeHorizontalString(1, screenHeight + 6, "Lat: " + String.format("%.2f°", overWorld.getLatitude(player.pos.y) * 90));

        display.placeHorizontalString(15, screenHeight + 6, "Lon:" + String.format("%.2f°", overWorld.getLongitude(player.pos.x) * 180));


    }


    @Override
    public Screen respondToUserInput(KeyEvent key) {
        switch (key.getKeyCode()) {

            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_H:
            case KeyEvent.VK_NUMPAD4:
                Bus.bus.post(new MovementMsg(player.id, Facing2d.WEST)).now();
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_L:
            case KeyEvent.VK_NUMPAD6:
                Bus.bus.post(new MovementMsg(player.id, Facing2d.EAST)).now();
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_K:
            case KeyEvent.VK_NUMPAD8:
                Bus.bus.post(new MovementMsg(player.id, Facing2d.NORTH)).now();
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_J:
            case KeyEvent.VK_NUMPAD2:
                Bus.bus.post(new MovementMsg(player.id, Facing2d.SOUTH)).now();
                break;
            case KeyEvent.VK_Y:
            case KeyEvent.VK_NUMPAD7:
                Bus.bus.post(new MovementMsg(player.id, Facing2d.NORTHWEST)).now();
                break;
            case KeyEvent.VK_U:
            case KeyEvent.VK_NUMPAD9:
                Bus.bus.post(new MovementMsg(player.id, Facing2d.NORTHEAST)).now();
                break;
            case KeyEvent.VK_B:
            case KeyEvent.VK_NUMPAD1:
                Bus.bus.post(new MovementMsg(player.id, Facing2d.SOUTHWEST)).now();
                break;
            case KeyEvent.VK_N:
            case KeyEvent.VK_NUMPAD3:
                Bus.bus.post(new MovementMsg(player.id, Facing2d.SOUTHEAST)).now();
                break;
            case KeyEvent.VK_E:
                Bus.bus.post(overWorld).asynchronously();
                break;
        }

        return this;
    }
}
