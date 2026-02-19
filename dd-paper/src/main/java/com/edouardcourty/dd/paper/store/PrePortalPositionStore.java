package com.edouardcourty.dd.paper.store;

import org.bukkit.Location;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persiste la position d'un joueur juste avant qu'il entre dans un portail.
 * Utilisée pour le remettre en place si le serveur cible est inaccessible.
 */
public class PrePortalPositionStore {
    private final Map<UUID, Location> positions = new ConcurrentHashMap<>();

    public void save(UUID uuid, Location location) {
        positions.put(uuid, location.clone());
    }

    public Optional<Location> remove(UUID uuid) {
        return Optional.ofNullable(positions.remove(uuid));
    }

    public void clear(UUID uuid) {
        positions.remove(uuid);
    }
}
