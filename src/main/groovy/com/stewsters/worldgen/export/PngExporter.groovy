package com.stewsters.worldgen.export

import com.stewsters.worldgen.map.OverWorld
import com.stewsters.worldgen.map.OverworldChunk

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage

class PngExporter {

    public static void export(OverWorld overWorld, String filePath) {

        int xTotal = overWorld.xSize * OverworldChunk.chunkSize;
        int yTotal = overWorld.ySize * OverworldChunk.chunkSize;

        BufferedImage output = new BufferedImage(xTotal, yTotal, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < xTotal; x++) {
            for (int y = 0; y < yTotal; y++) {
                Color color = overWorld.getTileType(x, y).color
                output.setRGB(x, y, color.getRGB());
            }
        }

        ImageIO.write(output, "PNG", new File(filePath));

        println("Image written")

    }


}



