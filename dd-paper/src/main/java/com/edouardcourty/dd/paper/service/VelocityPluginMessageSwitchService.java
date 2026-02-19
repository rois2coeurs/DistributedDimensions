package com.edouardcourty.dd.paper.service;

import com.edouardcourty.dd.common.messaging.Channels;
import com.edouardcourty.dd.common.model.Dimension;
import com.edouardcourty.dd.common.model.LocationData;
import com.edouardcourty.dd.common.service.DimensionSwitchService;
import com.edouardcourty.dd.paper.messaging.PlayerStateSerializer;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class VelocityPluginMessageSwitchService implements DimensionSwitchService {
    private final JavaPlugin plugin;

    public VelocityPluginMessageSwitchService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void sendSwitchRequest(UUID playerUuid, Dimension target, LocationData destination, boolean buildPortal) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return;

        var out = ByteStreams.newDataOutput();
        PlayerStateSerializer.write(out, playerUuid, target, destination, player, buildPortal);
        player.sendPluginMessage(plugin, Channels.DIM_SWITCH.toString(), out.toByteArray());
    }
}

