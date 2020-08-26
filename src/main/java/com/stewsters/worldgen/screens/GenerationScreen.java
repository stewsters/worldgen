package com.stewsters.worldgen.screens;


import com.stewsters.worldgen.map.overworld.OverWorld;
import com.stewsters.worldgen.messageBus.Bus;
import com.stewsters.worldgen.procGen.WorldGenerator;
import squidpony.squidgrid.gui.swing.SwingPane;

import java.awt.event.KeyEvent;

public class GenerationScreen implements Screen {

    private OverWorld overWorld;


    @Override
    public void displayOutput(SwingPane display) {

        int xSize = 32;
        int ySize = 16;

        overWorld = new OverWorld(xSize, ySize);
        WorldGenerator worldGenerator = new WorldGenerator();

        // Generate elevation
        worldGenerator.generateElevation(overWorld);

        worldGenerator.evenElevation(overWorld);

        worldGenerator.dropEdges(overWorld);

        // Generate temperature based on that elevation
        worldGenerator.generateTemperature(overWorld);

        worldGenerator.generateWind(overWorld);

        worldGenerator.generatePrecipitation(overWorld);

        worldGenerator.generateRivers(overWorld);

        worldGenerator.evenElevation(overWorld);

        // TODO: set sealevel to average

        // Regeneration based on the new rivers
        worldGenerator.generateWind(overWorld);

        worldGenerator.generatePrecipitation(overWorld);

        worldGenerator.generateContinents(overWorld);

        worldGenerator.populateSettlements(overWorld);

        worldGenerator.createRoadNetwork(overWorld);

        worldGenerator.expandRealms(overWorld);


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
