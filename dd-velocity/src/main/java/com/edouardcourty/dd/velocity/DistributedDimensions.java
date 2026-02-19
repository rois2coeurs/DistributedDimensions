package com.edouardcourty.dd.velocity;

import com.edouardcourty.dd.common.messaging.Channels;
import com.edouardcourty.dd.velocity.handler.ChatBroadcastHandler;
import com.edouardcourty.dd.velocity.handler.DimensionSwitchHandler;
import com.edouardcourty.dd.velocity.handler.EntityTransferHandler;
import com.edouardcourty.dd.velocity.handler.JoinQuitBroadcastHandler;
import com.edouardcourty.dd.velocity.listener.PlayerSessionListener;
import com.edouardcourty.dd.velocity.store.LastServerStore;
import com.edouardcourty.dd.velocity.store.PlayerStateStore;
import com.edouardcourty.dd.velocity.util.DebugLogger;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Plugin(id = "distributed-dimensions", name = "DistributedDimensionsProxy", version = "1.0.0")
public class DistributedDimensions {
    @Inject
    private ProxyServer server;

    @Inject
    private org.slf4j.Logger logger;

    @Inject
    @DataDirectory
    private Path dataDirectory;

    private ChannelIdentifier dimSwitchChannel;
    private ChannelIdentifier entityTransferChannel;
    private DimensionSwitchHandler dimensionSwitchHandler;
    private EntityTransferHandler entityTransferHandler;
    private DebugLogger debugLogger;

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        boolean debug = loadDebugConfig();
        debugLogger = new DebugLogger(logger, debug);

        dimSwitchChannel = MinecraftChannelIdentifier.from(Channels.DIM_SWITCH.toString());
        entityTransferChannel = MinecraftChannelIdentifier.from(Channels.ENTITY_TRANSFER.toString());
        server.getChannelRegistrar().register(dimSwitchChannel);
        server.getChannelRegistrar().register(entityTransferChannel);

        PlayerStateStore playerStateStore = new PlayerStateStore(dataDirectory, logger);
        dimensionSwitchHandler = new DimensionSwitchHandler(server, playerStateStore);
        entityTransferHandler = new EntityTransferHandler(server);

        LastServerStore lastServerStore = new LastServerStore(dataDirectory, logger);
        server.getEventManager().register(this, new PlayerSessionListener(server, lastServerStore, playerStateStore, entityTransferHandler));
        server.getEventManager().register(this, new ChatBroadcastHandler(server));
        server.getEventManager().register(this, new JoinQuitBroadcastHandler(server));
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        debugLogger.debug("PluginMessage received: channel=" + event.getIdentifier() + " source=" + event.getSource().getClass().getSimpleName());
        if (event.getIdentifier().equals(dimSwitchChannel)) {
            dimensionSwitchHandler.handle(event);
        } else if (event.getIdentifier().equals(entityTransferChannel)) {
            entityTransferHandler.handle(event);
        }
    }

    private boolean loadDebugConfig() {
        Path configFile = dataDirectory.resolve("config.toml");
        if (!Files.exists(configFile)) {
            try (InputStream in = getClass().getResourceAsStream("/config.toml")) {
                if (in != null) {
                    Files.createDirectories(dataDirectory);
                    Files.copy(in, configFile);
                }
            } catch (IOException ignored) {}
        }
        try {
            String content = Files.readString(configFile);
            return content.contains("debug = true");
        } catch (IOException e) {
            return false;
        }
    }
}
