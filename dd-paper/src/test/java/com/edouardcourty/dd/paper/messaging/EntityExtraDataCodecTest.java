package com.edouardcourty.dd.paper.messaging;

import org.bukkit.DyeColor;
import org.bukkit.entity.Sheep;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class EntityExtraDataCodecTest {

    @Test
    void encode_sheep_returnsCorrectString() {
        Sheep sheep = mock(Sheep.class);
        when(sheep.isAdult()).thenReturn(true);
        when(sheep.isSheared()).thenReturn(false);
        when(sheep.getColor()).thenReturn(DyeColor.RED);

        String result = EntityExtraDataCodec.encode(sheep);

        assertTrue(result.contains("adult=true;"));
        assertTrue(result.contains("sheared=false;"));
        assertTrue(result.contains("woolColor=RED;"));
    }

    @Test
    void parse_validData_returnsMap() {
        String data = "adult=true;sheared=false;woolColor=RED;";
        Map<String, String> map = EntityExtraDataCodec.parse(data);

        assertEquals(3, map.size());
        assertEquals("true", map.get("adult"));
        assertEquals("false", map.get("sheared"));
        assertEquals("RED", map.get("woolColor"));
    }

    @Test
    void apply_sheep_setsProperties() {
        Sheep sheep = mock(Sheep.class);
        String data = "adult=true;sheared=true;woolColor=BLUE;";

        EntityExtraDataCodec.apply(sheep, data);

        verify(sheep).setAdult();
        verify(sheep).setSheared(true);
        verify(sheep).setColor(DyeColor.BLUE);
    }
}
