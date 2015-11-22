package com.stewsters.worldgen.messageBus;

import com.stewsters.worldgen.messageBus.system.LogSystem;
import com.stewsters.worldgen.messageBus.system.MovementSystem;
import com.stewsters.worldgen.messageBus.system.PngWorldMapExporter;
import net.engio.mbassy.bus.MBassador;

/**
 * Created by stewsters on 11/10/15.
 */
public class Bus {

    public static MBassador bus;

    private static MovementSystem movementSystem;
    private static LogSystem logSystem;
    private static PngWorldMapExporter pngWorldMapExporter;

    public static void init() {
        bus = new MBassador();

        movementSystem = new MovementSystem();
        bus.subscribe(movementSystem);

        logSystem = new LogSystem();
        bus.subscribe(logSystem);

        pngWorldMapExporter = new PngWorldMapExporter();
        bus.subscribe(pngWorldMapExporter);

    }

}
