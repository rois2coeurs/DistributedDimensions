package com.edouardcourty.dd.paper.util;

import com.edouardcourty.dd.common.model.Dimension;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DimensionUtilTest {

    @Test
    void testFromWorld() {
        World overworld = mock(World.class);
        when(overworld.getEnvironment()).thenReturn(World.Environment.NORMAL);

        World nether = mock(World.class);
        when(nether.getEnvironment()).thenReturn(World.Environment.NETHER);

        World end = mock(World.class);
        when(end.getEnvironment()).thenReturn(World.Environment.THE_END);

        assertEquals(Dimension.OVERWORLD, DimensionUtil.fromWorld(overworld));
        assertEquals(Dimension.NETHER, DimensionUtil.fromWorld(nether));
        assertEquals(Dimension.END, DimensionUtil.fromWorld(end));
    }
}
