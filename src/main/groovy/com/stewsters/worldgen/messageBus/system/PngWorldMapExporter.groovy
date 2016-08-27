package com.stewsters.worldgen.messageBus.system

import com.stewsters.util.math.MatUtils
import com.stewsters.worldgen.map.overworld.OverWorld
import com.stewsters.worldgen.map.overworld.OverWorldChunk
import com.stewsters.worldgen.messageBus.Bus
import net.engio.mbassy.listener.Handler

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage

class PngWorldMapExporter {

    @Handler
    public void handle(OverWorld overWorld) {

        Bus.bus.post("Writing image").asynchronously()

        int xTotal = overWorld.xSize * OverWorldChunk.chunkSize;
        int yTotal = overWorld.ySize * OverWorldChunk.chunkSize;

        BufferedImage biomes = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);
        BufferedImage height = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);
        BufferedImage precip = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);
        BufferedImage temper = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);
        BufferedImage wind = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);

        Bus.bus.post("beginning transformation").asynchronously()

        for (int x = 0; x < xTotal; x++) {
            for (int y = 0; y < yTotal; y++) {
                try {
                    float elevation = overWorld.getElevation(x, y)

                    boolean waterVal = elevation > 0

                    biomes.setRGB(x, y, overWorld.getTileType(x, y).color.getRGB());

                    float sat = waterVal ? 0.8f : 0.2f

                    float heightVal = (float) ((elevation + 1f) / -2f);
                    height.setRGB(x, y, Color.getHSBColor(heightVal, sat, 0.5f).getRGB())

                    float precipVal = (float) (MatUtils.limit(overWorld.getPrecipitation(x, y) / 2f, 0, 1));
                    precip.setRGB(x, y, Color.getHSBColor(precipVal, sat, 0.5f).getRGB())

                    float tempVal = (float) ((overWorld.getTemp(x, y) + 1f) / -2f);
                    temper.setRGB(x, y, Color.getHSBColor(tempVal, sat, 0.5f).getRGB())


                    float windX = overWorld.getWindX(x, y)
                    float windY = overWorld.getWindY(x, y)
                    double length = Math.sqrt(windX * windX + windY * windY);

                    float windXScaled = MatUtils.limit((float) ((windX / length) + 1) / 2f, 0, 1);
                    float windYScaled = MatUtils.limit((float) ((windY / length) + 1) / 2f, 0, 1);

                    wind.setRGB(x, y, new Color(windXScaled, windYScaled, 0f).getRGB())

                } catch (Exception e) {
                    println "$x $y"
                }

            }
        }
        Bus.bus.post("beginning writing").asynchronously()

        ImageIO.write(biomes, "PNG", new File("export/biomes.png"));
        ImageIO.write(height, "PNG", new File("export/elevation.png"));
        ImageIO.write(precip, "PNG", new File("export/precipitation.png"));
        ImageIO.write(temper, "PNG", new File("export/temperature.png"));
        ImageIO.write(wind, "PNG", new File("export/wind.png"));

        Bus.bus.post("Image written").asynchronously()

    }


}



