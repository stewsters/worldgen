package com.stewsters.worldgen.messageBus.system;

import com.stewsters.util.math.MatUtils;
import com.stewsters.worldgen.map.overworld.OverWorld;
import com.stewsters.worldgen.map.overworld.OverWorldChunk;
import com.stewsters.worldgen.messageBus.Bus;
import net.engio.mbassy.listener.Handler;
import squidpony.squidcolor.SColor;
import squidpony.squidcolor.SColorFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class PngWorldMapExporter {

    @Handler
    public void handle(OverWorld overWorld) {

        Bus.bus.post("Writing image").asynchronously();

        int xTotal = overWorld.xSize * OverWorldChunk.chunkSize;
        int yTotal = overWorld.ySize * OverWorldChunk.chunkSize;

        BufferedImage biomes = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);
        BufferedImage height = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);
        BufferedImage height2 = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);
        BufferedImage precip = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);
        BufferedImage temper = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);
        BufferedImage wind = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);
        BufferedImage roads = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);
        BufferedImage continents = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);

        Bus.bus.post("beginning transformation").asynchronously();

        HashMap<Integer, Color> colors = new HashMap<>();

        for (int x = 0; x < xTotal; x++) {
            for (int y = 0; y < yTotal; y++) {
                try {
                    float elevation = overWorld.getElevation(x, y);

                    boolean aboveSeaLevel = elevation > 0;

                    biomes.setRGB(x, y, overWorld.getTileType(x, y).color.getRGB());

                    float sat = aboveSeaLevel ? 0.8f : 0.2f;

                    float heightVal = (float) ((elevation + 1f) / 2f);

                    height.setRGB(x, y, Color.HSBtoRGB(-heightVal, sat, 0.5f));
                    height2.setRGB(x, y, Color.HSBtoRGB(1f, 1f, heightVal));

                    float precipVal = (float) (MatUtils.limit(overWorld.getPrecipitation(x, y) / 2f, 0, 1));
                    precip.setRGB(x, y, Color.getHSBColor(precipVal, sat, 0.5f).getRGB());

                    float tempVal = (float) ((overWorld.getTemp(x, y) + 1f) / -2f);
                    temper.setRGB(x, y, Color.getHSBColor(tempVal, sat, 0.5f).getRGB());

                    int region = overWorld.getRegionId(x, y);
                    if (colors.get(region) == null) {
                        colors.put(region, Color.getHSBColor(MatUtils.getFloatInRange(0, 1), 0.8f, 0.5f));
                    }
                    continents.setRGB(x, y, colors.get(region).getRGB());

                    float windX = overWorld.getWindX(x, y);
                    float windY = overWorld.getWindY(x, y);

                    float windXScaled = MatUtils.limit((windX + 1) / 2f, 0, 1);
                    float windYScaled = MatUtils.limit((windY + 1) / 2f, 0, 1);
                    wind.setRGB(x, y, new Color(windXScaled, windYScaled, 0f).getRGB());

                    SColor color = overWorld.getSettlement(x, y) != null ? SColor.GREEN :
                            overWorld.getRoad(x, y) ? SColor.YELLOW :
                                    aboveSeaLevel ? SColorFactory.blend(SColor.BROWN, SColor.WHITE, Math.max(0, elevation)) : SColor.DARK_BLUE;

                    roads.setRGB(x, y, color.getRGB());

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        Bus.bus.post("beginning writing").asynchronously();

        List<Callable<Boolean>> callables = Arrays.asList(
                () -> ImageIO.write(biomes, "PNG", new File("export/biomes.png")),
                () -> ImageIO.write(height, "PNG", new File("export/elevation.png")),
                () -> ImageIO.write(height2, "PNG", new File("export/elevation2.png")),
                () -> ImageIO.write(precip, "PNG", new File("export/precipitation.png")),
                () -> ImageIO.write(temper, "PNG", new File("export/temperature.png")),
                () -> ImageIO.write(wind, "PNG", new File("export/wind.png")),
                () -> ImageIO.write(roads, "PNG", new File("export/roads.png")),
                () -> ImageIO.write(continents, "PNG", new File("export/continents.png"))
        );

        try {
            Executors.newWorkStealingPool().invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Bus.bus.post("Image written").asynchronously();

    }


}
