package com.edouardcourty.dd.common.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LocationDataTest {

    @Test
    void of_createsInstanceWithCorrectValues() {
        LocationData ld = LocationData.of(1.5, 64.0, -3.5, 90.0f, 45.0f);
        
        assertEquals(1.5, ld.x);
        assertEquals(64.0, ld.y);
        assertEquals(-3.5, ld.z);
        assertEquals(90.0f, ld.yaw);
        assertEquals(45.0f, ld.pitch);
    }
}
