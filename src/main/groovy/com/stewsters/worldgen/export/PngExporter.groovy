package com.stewsters.worldgen.export

import com.stewsters.worldgen.map.OverWorld
import com.stewsters.worldgen.map.OverworldChunk

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage

class PngExporter {

    public static void export(OverWorld overWorld) {

        int xTotal = overWorld.xSize * OverworldChunk.chunkSize;
        int yTotal = overWorld.ySize * OverworldChunk.chunkSize;

        BufferedImage biomes = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);
        BufferedImage height = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);
        BufferedImage precip = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);
        BufferedImage temper = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);


        for (int x = 0; x < xTotal; x++) {
            for (int y = 0; y < yTotal; y++) {

                biomes.setRGB(x, y, overWorld.getTileType(x, y).color.getRGB());

                float heightVal = (float) ((-overWorld.getElevation(x, y) - 1f) / 2f);


                height.setRGB(x, y, Color.getHSBColor(heightVal, 0.5f, 0.5f).getRGB())

            }
        }


        ImageIO.write(biomes, "PNG", new File("export/biomes.png"));
        ImageIO.write(height, "PNG", new File("export/elevation.png"));


        println("Image written")

    }


}



