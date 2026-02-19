package com.edouardcourty.dd.paper.listener;

import com.edouardcourty.dd.common.model.Dimension;
import com.edouardcourty.dd.common.model.LocationData;
import com.edouardcourty.dd.common.model.PortalConstants;
import com.edouardcourty.dd.common.service.DimensionSwitchService;
import com.edouardcourty.dd.paper.store.PrePortalPositionStore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class EndPortalListener implements Listener {
    private final Dimension dimension;
    private final DimensionSwitchService dimensionSwitchService;
    private final PrePortalPositionStore positionStore;

    public EndPortalListener(Dimension dimension, DimensionSwitchService dimensionSwitchService, PrePortalPositionStore positionStore) {
        this.dimension = dimension;
        this.dimensionSwitchService = dimensionSwitchService;
        this.positionStore = positionStore;
    }

    @EventHandler
    public void onEndPortal(PlayerPortalEvent e) {
        if (e.getCause() != PlayerPortalEvent.TeleportCause.END_PORTAL) return;

        e.setCancelled(true);

        positionStore.save(e.getPlayer().getUniqueId(), e.getFrom());

        if (dimension == Dimension.OVERWORLD) {
            dimensionSwitchService.sendSwitchRequest(
                e.getPlayer().getUniqueId(),
                Dimension.END,
                LocationData.of(PortalConstants.END_PLATFORM_X, PortalConstants.END_PLATFORM_Y, PortalConstants.END_PLATFORM_Z, 0, 0),
                false
            );
        } else if (dimension == Dimension.END) {
            org.bukkit.Location respawn = e.getPlayer().getRespawnLocation();
            dimensionSwitchService.sendSwitchRequest(
                e.getPlayer().getUniqueId(),
                Dimension.OVERWORLD,
                respawn != null
                    ? LocationData.of(respawn.getX(), respawn.getY(), respawn.getZ(), respawn.getYaw(), respawn.getPitch())
                    : LocationData.of(0, 64, 0, 0, 0),
                false
            );
        }
    }
}

