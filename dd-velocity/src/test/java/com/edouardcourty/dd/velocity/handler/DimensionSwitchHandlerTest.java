package com.edouardcourty.dd.velocity.handler;

import com.edouardcourty.dd.common.messaging.Channels;
import com.edouardcourty.dd.velocity.store.PlayerStateStore;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DimensionSwitchHandlerTest {

    private ProxyServer proxyServer;
    private PlayerStateStore stateStore;
    private DimensionSwitchHandler handler;

    @BeforeEach
    void setUp() {
        proxyServer = mock(ProxyServer.class);
        stateStore = mock(PlayerStateStore.class);
        handler = new DimensionSwitchHandler(proxyServer, stateStore, Map.of("nether", "nether_server"));
    }

    @Test
    void handle_invalidPlayer_aborts() {
        UUID uuid = UUID.randomUUID();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(uuid.toString());
        out.writeUTF("nether");

        PluginMessageEvent event = mock(PluginMessageEvent.class);
        when(event.getData()).thenReturn(out.toByteArray());
        when(proxyServer.getPlayer(uuid)).thenReturn(Optional.empty());

        handler.handle(event);

        verify(proxyServer).getPlayer(uuid);
        verifyNoInteractions(stateStore);
    }

    @Test
    void handle_serverNotFound_sendsFailedMessage() {
        UUID uuid = UUID.randomUUID();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(uuid.toString());
        out.writeUTF("unknown_dimension");

        PluginMessageEvent event = mock(PluginMessageEvent.class);
        when(event.getData()).thenReturn(out.toByteArray());

        Player player = mock(Player.class);
        when(proxyServer.getPlayer(uuid)).thenReturn(Optional.of(player));
        when(proxyServer.getServer("unknown_dimension")).thenReturn(Optional.empty());

        ServerConnection sourceConn = mock(ServerConnection.class);
        when(event.getSource()).thenReturn(sourceConn);
        RegisteredServer sourceServer = mock(RegisteredServer.class);
        when(sourceConn.getServer()).thenReturn(sourceServer);

        handler.handle(event);

        ArgumentCaptor<ChannelIdentifier> idCaptor = ArgumentCaptor.forClass(ChannelIdentifier.class);
        ArgumentCaptor<byte[]> dataCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(sourceServer).sendPluginMessage(idCaptor.capture(), dataCaptor.capture());

        assertEquals(Channels.SWITCH_FAILED.toString(), idCaptor.getValue().getId());
    }
}
