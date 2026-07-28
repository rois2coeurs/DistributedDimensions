package com.edouardcourty.dd.common.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DimensionTest {

    @Test
    void toBukkitWorldName_returnsCorrectNames() {
        assertEquals("world", Dimension.OVERWORLD.toBukkitWorldName());
        assertEquals("world_nether", Dimension.NETHER.toBukkitWorldName());
        assertEquals("world_the_end", Dimension.END.toBukkitWorldName());
    }
}
