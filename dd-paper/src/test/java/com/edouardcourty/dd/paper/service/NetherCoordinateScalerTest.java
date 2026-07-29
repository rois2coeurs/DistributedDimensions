package com.edouardcourty.dd.paper.service;

import com.edouardcourty.dd.common.model.Dimension;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NetherCoordinateScalerTest {

    @Test
    void testScaleOverworldToNether() {
        NetherCoordinateScaler.ScaledCoords result = NetherCoordinateScaler.scale(Dimension.OVERWORLD, 800, 64, 160);
        assertEquals(100.0, result.x());
        assertEquals(64.0, result.y());
        assertEquals(20.0, result.z());
        assertEquals(Dimension.NETHER, result.target());
    }

    @Test
    void testScaleNetherToOverworld() {
        NetherCoordinateScaler.ScaledCoords result = NetherCoordinateScaler.scale(Dimension.NETHER, 100, 64, 20);
        assertEquals(800.0, result.x());
        assertEquals(64.0, result.y());
        assertEquals(160.0, result.z());
        assertEquals(Dimension.OVERWORLD, result.target());
    }
}
