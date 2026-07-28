package com.edouardcourty.dd.paper.messaging;

import com.edouardcourty.dd.common.model.Dimension;
import com.edouardcourty.dd.common.model.LocationData;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerStateSerializerTest {

    @Test
    void testSerializationWithoutDataTransfer() {
        Player player = mock(Player.class);
        UUID uuid = UUID.randomUUID();
        LocationData location = LocationData.of(1, 2, 3, 0, 0);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        
        // Write state with transferData = false
        PlayerStateSerializer.write(out, uuid, Dimension.OVERWORLD, location, player, true, false);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        PlayerStateSerializer.PlayerState state = PlayerStateSerializer.read(in);
        
        assertEquals(uuid, state.playerUuid());
        assertEquals(Dimension.OVERWORLD, state.targetDimension());
        assertTrue(state.buildPortal());
        assertFalse(state.transferData());
        
        // Data should be defaulted to 0 or nulls/empty
        assertEquals(0, state.xpLevel());
        assertEquals(0, state.inventoryContents().length);
        assertEquals(0, state.armorContents().length);
    }

    @Test
    void testSerializationWithDataTransfer() {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        
        UUID uuid = UUID.randomUUID();
        LocationData location = LocationData.of(1, 2, 3, 0, 0);

        when(player.getLevel()).thenReturn(30);
        when(player.getExp()).thenReturn(0.5f);
        when(player.getFoodLevel()).thenReturn(20);
        when(player.getSaturation()).thenReturn(5.0f);
        when(player.getExhaustion()).thenReturn(1.0f);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(player.getActivePotionEffects()).thenReturn(Collections.emptyList());
        when(player.getVehicle()).thenReturn(null);
        
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getContents()).thenReturn(new ItemStack[0]);
        when(inventory.getArmorContents()).thenReturn(new ItemStack[0]);
        when(inventory.getItemInOffHand()).thenReturn(null);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        
        // Write state with transferData = true
        PlayerStateSerializer.write(out, uuid, Dimension.NETHER, location, player, false, true);
        
        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        PlayerStateSerializer.PlayerState state;
        
        try (org.mockito.MockedStatic<com.edouardcourty.dd.paper.util.ItemSerializer> mockedItemSerializer = org.mockito.Mockito.mockStatic(com.edouardcourty.dd.paper.util.ItemSerializer.class)) {
            mockedItemSerializer.when(() -> com.edouardcourty.dd.paper.util.ItemSerializer.read(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
                ByteArrayDataInput input = invocation.getArgument(0);
                int len = input.readInt();
                if (len > 0) input.skipBytes(len);
                return mock(ItemStack.class);
            });
            
            state = PlayerStateSerializer.read(in);
        }
        
        assertEquals(uuid, state.playerUuid());
        assertEquals(Dimension.NETHER, state.targetDimension());
        assertFalse(state.buildPortal());
        assertTrue(state.transferData());
        
        // Data should match what we mocked
        assertEquals(30, state.xpLevel());
        assertEquals(0.5f, state.xpProgress());
        assertEquals(20, state.foodLevel());
        assertEquals(5.0f, state.saturation());
        assertEquals(1.0f, state.exhaustion());
        assertEquals(GameMode.SURVIVAL, state.gameMode());
    }
}
