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

public class DistributedDimensions extends JavaPlugin {
    private Dimension dimension;
    private DimensionSwitchService dimensionSwitchService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        dimension = Dimension.valueOf(getConfig().getString("world", Dimension.OVERWORLD.name()));
        dimensionSwitchService = new VelocityPluginMessageSwitchService(this);

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

        getServer().getPluginManager().registerEvents(new NetherPortalListener(dimension, dimensionSwitchService, positionStore), this);
        getServer().getPluginManager().registerEvents(new EndPortalListener(dimension, dimensionSwitchService, positionStore), this);
        getServer().getPluginManager().registerEvents(new EntityPortalListener(dimension, this), this);
        getServer().getPluginManager().registerEvents(new RespawnListener(dimension, dimensionSwitchService, this), this);
        getServer().getPluginManager().registerEvents(new DimensionGuardListener(this, dimension), this);
    }
}
