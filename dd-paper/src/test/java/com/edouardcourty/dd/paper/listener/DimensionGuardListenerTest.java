package com.edouardcourty.dd.paper.listener;

import com.edouardcourty.dd.common.model.Dimension;
import com.edouardcourty.dd.paper.DistributedDimensions;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

class DimensionGuardListenerTest {

    @Test
    void testGuardAllowedWorld() {
        DistributedDimensions plugin = mock(DistributedDimensions.class);
        DimensionGuardListener listener = new DimensionGuardListener(plugin, Arrays.asList(Dimension.OVERWORLD, Dimension.NETHER));

        Player player = mock(Player.class);
        World currentWorld = mock(World.class);
        when(currentWorld.getName()).thenReturn("world_nether");
        when(player.getWorld()).thenReturn(currentWorld);

        PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(player, mock(World.class));
        listener.onWorldChange(event);

        verify(player, never()).teleport(any(Location.class));
    }

    @Test
    void testGuardDisallowedWorld() {
        DistributedDimensions plugin = mock(DistributedDimensions.class);
        Server server = mock(Server.class);
        Logger logger = mock(Logger.class);

        when(plugin.getServer()).thenReturn(server);
        when(plugin.getLogger()).thenReturn(logger);

        DimensionGuardListener listener = new DimensionGuardListener(plugin, Arrays.asList(Dimension.OVERWORLD, Dimension.NETHER));

        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Notch");

        World currentWorld = mock(World.class);
        when(currentWorld.getName()).thenReturn("world_the_end"); // Not allowed
        when(player.getWorld()).thenReturn(currentWorld);

        World fallbackWorld = mock(World.class);
        Location fallbackSpawn = new Location(fallbackWorld, 0, 64, 0);
        when(fallbackWorld.getSpawnLocation()).thenReturn(fallbackSpawn);
        when(server.getWorld("world")).thenReturn(fallbackWorld); // First in list is OVERWORLD ("world")

        PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(player, mock(World.class));
        listener.onWorldChange(event);

        verify(player).teleport(fallbackSpawn);
    }
}
