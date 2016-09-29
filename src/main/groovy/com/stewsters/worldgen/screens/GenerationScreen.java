package com.stewsters.worldgen.screens;


import com.stewsters.util.math.Point2i;
import com.stewsters.worldgen.game.Leader;
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
        coords.parallelStream().forEach(coord ->
                overWorld.chunks[coord.x][coord.y] = worldGenerator.generateChunkedHeightMap(overWorld, coord.x, coord.y)
        );
        Bus.bus.post("Finished Chunk Elevation").now();

        worldGenerator.evenElevation(overWorld);

        Bus.bus.post("Finished Evening").now();

        // Generate temperature based on that elevation
        coords.parallelStream().forEach(coord ->
                overWorld.chunks[coord.x][coord.y] = worldGenerator.generateChunkedTemperatureMap(overWorld, coord.x, coord.y)
        );
        Bus.bus.post("Finished Chunk Temperature").now();

        worldGenerator.generateWind(overWorld);
        Bus.bus.post("Finished Wind").now();

        worldGenerator.generatePrecipitation(overWorld);
        Bus.bus.post("Finished Precipitation").now();

        worldGenerator.generateRivers(overWorld);
        Bus.bus.post("Finished Rivers").now();

        worldGenerator.evenElevation(overWorld);
        Bus.bus.post("Finished Evening 2").now();

        // TODO: set sealevel to average

        // Regeneration based on the new rivers
        worldGenerator.generateWind(overWorld);
        Bus.bus.post("Finished Wind 2").now();

        worldGenerator.generatePrecipitation(overWorld);
        Bus.bus.post("Finished Precipitation 2").now();


        worldGenerator.populateSettlements(overWorld);
        Bus.bus.post("Finished Populating Settlements").now();

        worldGenerator.createRoadNetwork(overWorld);
        Bus.bus.post("Finished Creating Roads").now();

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
