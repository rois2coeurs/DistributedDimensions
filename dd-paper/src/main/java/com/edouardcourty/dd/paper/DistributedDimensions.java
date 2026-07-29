package com.edouardcourty.dd.paper;

import com.edouardcourty.dd.common.messaging.Channels;
import com.edouardcourty.dd.common.model.Dimension;
import com.edouardcourty.dd.common.service.DimensionSwitchService;
import com.edouardcourty.dd.paper.listener.DimensionGuardListener;
import com.edouardcourty.dd.paper.listener.DimensionSwitchListener;
import com.edouardcourty.dd.paper.listener.EndPortalListener;
import com.edouardcourty.dd.paper.listener.EntityPortalListener;
import com.edouardcourty.dd.paper.listener.EntitySpawnListener;
import com.edouardcourty.dd.paper.listener.NetherPortalListener;
import com.edouardcourty.dd.paper.listener.PlayerBroadcastListener;
import com.edouardcourty.dd.paper.listener.RespawnListener;
import com.edouardcourty.dd.paper.listener.SwitchFailedListener;
import com.edouardcourty.dd.paper.service.VelocityPluginMessageSwitchService;
import com.edouardcourty.dd.paper.store.PrePortalPositionStore;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class DistributedDimensions extends JavaPlugin {
    private DimensionSwitchService dimensionSwitchService;
    private List<Dimension> managedDimensions;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        managedDimensions = new ArrayList<>();
        List<String> worldsList = getConfig().getStringList("worlds");
        if (worldsList != null && !worldsList.isEmpty()) {
            for (String w : worldsList) {
                if (w == null || w.isBlank()) continue;
                try {
                    managedDimensions.add(Dimension.valueOf(w.trim().toUpperCase(java.util.Locale.ROOT)));
                } catch (IllegalArgumentException ex) {
                    getLogger().warning("[Config] Ignoring invalid dimension in 'worlds': " + w);
                }
            }
        }
        if (managedDimensions.isEmpty()) {
            String legacy = getConfig().getString("world", Dimension.OVERWORLD.name());
            try {
                managedDimensions.add(Dimension.valueOf(legacy.trim().toUpperCase(java.util.Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                getLogger().warning("[Config] Invalid 'world' value: " + legacy + ". Falling back to OVERWORLD.");
                managedDimensions.add(Dimension.OVERWORLD);
            }
        }

        dimensionSwitchService = new VelocityPluginMessageSwitchService(this);
        
        org.bukkit.command.PluginCommand ddinfoCmd = getCommand("ddinfo");
        if (ddinfoCmd != null) {
            ddinfoCmd.setExecutor(new com.edouardcourty.dd.paper.command.DistributedDimensionsCommand(this));
        }

        PrePortalPositionStore positionStore = new PrePortalPositionStore(this);

        DimensionSwitchListener dimSwitchListener = new DimensionSwitchListener(this, positionStore);
        getServer().getMessenger().registerOutgoingPluginChannel(this, Channels.DIM_SWITCH.toString());
        getServer().getMessenger().registerIncomingPluginChannel(this, Channels.DIM_SWITCH.toString(), dimSwitchListener);
        getServer().getMessenger().registerIncomingPluginChannel(this, Channels.RESTORE_STATE.toString(), dimSwitchListener);

        getServer().getMessenger().registerOutgoingPluginChannel(this, Channels.ENTITY_TRANSFER.toString());
        getServer().getMessenger().registerIncomingPluginChannel(this, Channels.ENTITY_TRANSFER.toString(), new EntitySpawnListener(this));

        getServer().getMessenger().registerIncomingPluginChannel(this, Channels.SWITCH_FAILED.toString(), new SwitchFailedListener(this, positionStore));

        PlayerBroadcastListener broadcastListener = new PlayerBroadcastListener(this);
        getServer().getMessenger().registerIncomingPluginChannel(this, Channels.PLAYER_BROADCAST.toString(), broadcastListener);
        getServer().getPluginManager().registerEvents(broadcastListener, this);

        getServer().getPluginManager().registerEvents(new NetherPortalListener(dimensionSwitchService, positionStore), this);
        getServer().getPluginManager().registerEvents(new EndPortalListener(dimensionSwitchService, positionStore), this);
        getServer().getPluginManager().registerEvents(new EntityPortalListener(this), this);
        getServer().getPluginManager().registerEvents(new RespawnListener(dimensionSwitchService, this), this);
        getServer().getPluginManager().registerEvents(new DimensionGuardListener(this, managedDimensions), this);
    }
}
