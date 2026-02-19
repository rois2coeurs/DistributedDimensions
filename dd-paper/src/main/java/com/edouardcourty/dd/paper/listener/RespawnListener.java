package com.edouardcourty.dd.paper.listener;

import com.edouardcourty.dd.common.model.Dimension;
import com.edouardcourty.dd.common.model.LocationData;
import com.edouardcourty.dd.common.service.DimensionSwitchService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Gère le respawn cross-serveur : un joueur qui meurt sur le Nether ou l'End
 * doit réapparaître sur l'Overworld, pas sur le serveur courant.
 *
 * Le point de respawn exact (lit/ancre) est inconnu depuis les serveurs Nether/End ;
 * le joueur est envoyé à (0, 64, 0) et {@link com.edouardcourty.dd.paper.portal.SafeLocationFinder}
 * se charge de trouver un sol sûr à destination.
 */
public class RespawnListener implements Listener {
    private final Dimension dimension;
    private final DimensionSwitchService dimensionSwitchService;
    private final JavaPlugin plugin;

    public RespawnListener(Dimension dimension, DimensionSwitchService dimensionSwitchService, JavaPlugin plugin) {
        this.dimension = dimension;
        this.dimensionSwitchService = dimensionSwitchService;
        this.plugin = plugin;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (dimension == Dimension.OVERWORLD) return;

        // Récupère le point de respawn du joueur (lit/ancre de respawn).
        // Même si le monde "world" n'existe pas sur ce serveur, les coordonnées sont dans le NBT du joueur.
        // Si null (pas de lit posé ou invalide), on utilise le spawn par défaut du monde (0, 64, 0).
        org.bukkit.Location respawn = e.getPlayer().getRespawnLocation();
        LocationData dest = (respawn != null)
            ? LocationData.of(respawn.getX(), respawn.getY(), respawn.getZ(), respawn.getYaw(), respawn.getPitch())
            : LocationData.of(0, 64, 0, 0, 0);

        Bukkit.getScheduler().runTaskLater(plugin, () ->
            dimensionSwitchService.sendSwitchRequest(
                e.getPlayer().getUniqueId(),
                Dimension.OVERWORLD,
                dest,
                false  // pas de portail au respawn
            ), 1L
        );
    }
}
