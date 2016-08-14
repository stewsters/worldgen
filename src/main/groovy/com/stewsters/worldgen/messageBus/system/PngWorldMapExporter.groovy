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
        BufferedImage water = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < xTotal; x++) {
            for (int y = 0; y < yTotal; y++) {

                boolean waterVal = overWorld.getElevation(x, y) > 0

                biomes.setRGB(x, y, overWorld.getTileType(x, y).color.getRGB());

                float sat = waterVal ? 0.8f : 0.2f

                float heightVal = (float) ((-overWorld.getElevation(x, y) - 1f) / 2f);
                height.setRGB(x, y, Color.getHSBColor(heightVal, sat, 0.5f).getRGB())

                float precipVal = (float) (MatUtils.limit(overWorld.getPrecipitation(x, y)/2f, 0, 1));
                precip.setRGB(x, y, Color.getHSBColor(precipVal, sat, 0.5f).getRGB())

                float tempVal = (float) ((-overWorld.getTemp(x, y) - 1f) / 2f);
                temper.setRGB(x, y, Color.getHSBColor(tempVal, sat, 0.5f).getRGB())

            }
        }


        ImageIO.write(biomes, "PNG", new File("export/biomes.png"));
        ImageIO.write(height, "PNG", new File("export/elevation.png"));
        ImageIO.write(precip, "PNG", new File("export/precipitation.png"));
        ImageIO.write(temper, "PNG", new File("export/temperature.png"));

        Bus.bus.post("Image written").asynchronously()

    }


}



