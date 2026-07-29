package com.edouardcourty.dd.paper.listener;

import com.edouardcourty.dd.common.model.Dimension;
import com.edouardcourty.dd.common.model.LocationData;
import com.edouardcourty.dd.common.service.DimensionSwitchService;
import com.edouardcourty.dd.paper.util.DimensionUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles cross-server respawn: a player dying in the Nether or the End
 * must respawn in the Overworld, not on the current server.
 *
 * The exact respawn point (bed/anchor) is unknown from the Nether/End servers;
 * the player is sent to (0, 64, 0) and {@link com.edouardcourty.dd.paper.portal.SafeLocationFinder}
 * will take care of finding a safe ground at the destination.
 */
public class RespawnListener implements Listener {
    private final DimensionSwitchService dimensionSwitchService;
    private final JavaPlugin plugin;

    public RespawnListener(DimensionSwitchService dimensionSwitchService, JavaPlugin plugin) {
        this.dimensionSwitchService = dimensionSwitchService;
        this.plugin = plugin;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Dimension dimension = DimensionUtil.fromWorld(e.getPlayer().getWorld());
        if (dimension == Dimension.OVERWORLD) return;

        // Retrieve the player's respawn point (bed/respawn anchor).
        // Even if the "world" world doesn't exist on this server, the coordinates are in the player's NBT.
        // If null (no bed placed or invalid), we use the world's default spawn (0, 64, 0).
        org.bukkit.Location respawn = e.getPlayer().getRespawnLocation();
        LocationData dest = (respawn != null)
            ? LocationData.of(respawn.getX(), respawn.getY(), respawn.getZ(), respawn.getYaw(), respawn.getPitch())
            : LocationData.of(0, 64, 0, 0, 0);

        Bukkit.getScheduler().runTaskLater(plugin, () ->
            dimensionSwitchService.sendSwitchRequest(
                e.getPlayer().getUniqueId(),
                Dimension.OVERWORLD,
                dest,
                false  // no portal at respawn
            ), 1L
        );
    }
}
