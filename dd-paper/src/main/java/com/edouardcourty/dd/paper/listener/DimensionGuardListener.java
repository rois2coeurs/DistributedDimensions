package com.edouardcourty.dd.paper.listener;

import com.edouardcourty.dd.common.model.Dimension;
import com.edouardcourty.dd.paper.DistributedDimensions;
import java.util.List;
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
    private final List<Dimension> dimensions;

    public DimensionGuardListener(DistributedDimensions plugin, List<Dimension> dimensions) {
        this.plugin = plugin;
        this.dimensions = dimensions;
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
        if (dimensions.isEmpty()) return;

        String currentWorld = player.getWorld().getName();
        for (Dimension dim : dimensions) {
            if (currentWorld.equals(dim.toBukkitWorldName())) {
                return; // Joueur dans un monde autorisé
            }
        }

        // Joueur dans un monde non autorisé, on le renvoie dans le premier monde de la liste
        Dimension fallbackDimension = dimensions.get(0);
        String expected = fallbackDimension.toBukkitWorldName();
        World correct = plugin.getServer().getWorld(expected);
        if (correct == null) {
            plugin.getLogger().severe("[DimensionGuard] Le monde de repli '" + expected + "' est introuvable !");
            return;
        }

        plugin.getLogger().warning("[DimensionGuard] " + player.getName()
            + " was in '" + currentWorld
            + "' (non géré par ce serveur). Téléportation vers '" + expected + "'.");

        player.teleport(correct.getSpawnLocation());
    }
}
