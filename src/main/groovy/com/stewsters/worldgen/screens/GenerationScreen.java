package com.stewsters.worldgen.screens;


import com.stewsters.util.math.Point2i;
import com.stewsters.worldgen.map.overworld.OverWorld;
import com.stewsters.worldgen.messageBus.Bus;
import com.stewsters.worldgen.procGen.WorldGenerator;
import squidpony.squidgrid.gui.swing.SwingPane;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class GenerationScreen implements Screen {

    private OverWorld overWorld;

    private int screenWidth;
    private int screenHeight;


    public GenerationScreen() {
        screenWidth = 80;
        screenHeight = 40;

    }


    @Override
    public void displayOutput(SwingPane display) {

        int xSize = 32;
        int ySize = 16;

        overWorld = new OverWorld(xSize, ySize);
        WorldGenerator worldGenerator = new WorldGenerator();

        ArrayList<Point2i> coords = new ArrayList<Point2i>();
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                coords.add(new Point2i(x, y));
            }
        }

        // Generate elevation
        worldGenerator.generateElevation(overWorld, coords);

        worldGenerator.evenElevation(overWorld);

        // Generate temperature based on that elevation
        worldGenerator.generateTemperature(overWorld, coords);

        worldGenerator.generateWind(overWorld, coords);

        worldGenerator.generatePrecipitation(overWorld);

        worldGenerator.generateRivers(overWorld);

        worldGenerator.evenElevation(overWorld);

        // TODO: set sealevel to average

        // Regeneration based on the new rivers
        worldGenerator.generateWind(overWorld, coords);

        worldGenerator.generatePrecipitation(overWorld);

        worldGenerator.populateSettlements(overWorld);

        worldGenerator.createRoadNetwork(overWorld);


        Bus.bus.post("Finished Generation").now();

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
