package com.edouardcourty.dd.paper.listener;

import com.edouardcourty.dd.common.model.Dimension;
import com.edouardcourty.dd.common.model.LocationData;
import com.edouardcourty.dd.common.service.DimensionSwitchService;
import com.edouardcourty.dd.paper.service.NetherCoordinateScaler;
import com.edouardcourty.dd.paper.store.PrePortalPositionStore;
import com.edouardcourty.dd.paper.util.DimensionUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class NetherPortalListener implements Listener {
    private final DimensionSwitchService dimensionSwitchService;
    private final PrePortalPositionStore positionStore;

    public NetherPortalListener(DimensionSwitchService dimensionSwitchService, PrePortalPositionStore positionStore) {
        this.dimensionSwitchService = dimensionSwitchService;
        this.positionStore = positionStore;
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent e) {
        if (e.getCause() != PlayerPortalEvent.TeleportCause.NETHER_PORTAL) return;
        
        Dimension dimension = DimensionUtil.fromWorld(e.getFrom().getWorld());
        if (dimension == Dimension.END) {
            e.setCancelled(true); // portail Nether n'existe pas dans l'End
            return;
        }

        e.setCancelled(true);

        positionStore.save(e.getPlayer().getUniqueId(), e.getFrom());

        NetherCoordinateScaler.ScaledCoords dest = NetherCoordinateScaler.scale(
            dimension, e.getFrom().getX(), e.getFrom().getY(), e.getFrom().getZ()
        );

        dimensionSwitchService.sendSwitchRequest(
            e.getPlayer().getUniqueId(),
            dest.target(),
            LocationData.of(dest.x(), dest.y(), dest.z(), e.getFrom().getYaw(), e.getFrom().getPitch())
        );
    }
}

