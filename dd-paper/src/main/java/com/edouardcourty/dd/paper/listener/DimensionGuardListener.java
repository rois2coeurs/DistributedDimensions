package com.edouardcourty.dd.paper.listener;

import com.edouardcourty.dd.common.model.Dimension;
import com.edouardcourty.dd.paper.DistributedDimensions;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Garantit qu'aucun joueur ne reste dans un monde qui n'est pas la dimension
 * gérée par ce serveur.
 *
 * <ul>
 *   <li>À la connexion : vérifie après un court délai (le temps que le DIM_SWITCH
 *       éventuel ait eu lieu) que le joueur est dans le bon monde.</li>
 *   <li>Au changement de monde : corrige immédiatement si le nouveau monde est
 *       incorrect (ex : commande /world par un admin).</li>
 * </ul>
 */
public class DimensionGuardListener implements Listener {
    private final DistributedDimensions plugin;
    private final Dimension dimension;

    public DimensionGuardListener(DistributedDimensions plugin, Dimension dimension) {
        this.plugin = plugin;
        this.dimension = dimension;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        // Délai : laisse le temps au DimensionSwitchListener de téléporter le joueur
        // avant de vérifier. 20 ticks = 1 seconde, largement suffisant.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) guard(player);
        }, 20L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        guard(e.getPlayer());
    }

    private void guard(Player player) {
        String expected = dimension.toBukkitWorldName();
        if (player.getWorld().getName().equals(expected)) return;

        World correct = plugin.getServer().getWorld(expected);
        if (correct == null) {
            plugin.getLogger().severe("[DimensionGuard] Le monde '" + expected + "' est introuvable sur ce serveur !");
            return;
        }

        plugin.getLogger().warning("[DimensionGuard] " + player.getName()
            + " was in '" + player.getWorld().getName()
            + "' au lieu de '" + expected + "' — correction automatique.");

        player.teleport(correct.getSpawnLocation());
    }
}
