package com.stewsters.worldgen

import com.stewsters.util.math.Facing2d
import com.stewsters.worldgen.map.overworld.OverWorld
import com.stewsters.worldgen.messageBus.message.MovementMsg
import com.stewsters.worldgen.messageBus.system.LogSystem
import com.stewsters.worldgen.messageBus.system.MovementSystem
import net.engio.mbassy.bus.MBassador
import org.junit.Test

/**
 * Created by stewsters on 11/2/15.
 */
class BusTest {

    @Test
    public void testSendMessage() {

        OverWorld overworld = new OverWorld(10, 10)


        MBassador bus = new MBassador()
        def movementSystem = new MovementSystem()
        bus.subscribe(movementSystem)


        def logSystem = new LogSystem()
        bus.subscribe(logSystem)

        bus.post("WangJangler").now()
        bus.post("lawl").asynchronously()

        bus.post(new MovementMsg(4, Facing2d.NORTH)).now()


    }


}
