package com.edouardcourty.dd.common.service;

import com.edouardcourty.dd.common.model.Dimension;
import com.edouardcourty.dd.common.model.LocationData;

import java.util.UUID;

public interface DimensionSwitchService {
    void sendSwitchRequest(UUID playerUuid, Dimension target, LocationData destination, boolean buildPortal);

    default void sendSwitchRequest(UUID playerUuid, Dimension target, LocationData destination) {
        sendSwitchRequest(playerUuid, target, destination, true);
    }
}
