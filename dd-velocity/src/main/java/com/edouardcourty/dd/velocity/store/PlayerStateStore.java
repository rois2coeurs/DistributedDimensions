package com.edouardcourty.dd.velocity.store;

import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Persists the full state of the last DIM_SWITCH of each player on disk.
 * Bytes are written verbatim to player-states/{uuid}.bin.
 * Loaded on proxy startup, updated after each successful switch.
 */
public class PlayerStateStore {
    private final Path directory;
    private final Logger logger;
    private final Map<UUID, byte[]> data = new HashMap<>();

    public PlayerStateStore(Path dataDirectory, Logger logger) {
        this.directory = dataDirectory.resolve("player-states");
        this.logger = logger;
        load();
    }

    public void save(UUID uuid, byte[] state) {
        data.put(uuid, state);
        persist(uuid, state);
    }

    public Optional<byte[]> get(UUID uuid) {
        return Optional.ofNullable(data.get(uuid));
    }

    public void delete(UUID uuid) {
        data.remove(uuid);
        try {
            Files.deleteIfExists(directory.resolve(uuid + ".bin"));
        } catch (IOException e) {
            logger.warn("[PlayerStateStore] Could not delete state for " + uuid + ": " + e.getMessage());
        }
    }

    private void load() {
        if (!Files.exists(directory)) return;
        try {
            Files.list(directory).forEach(path -> {
                String name = path.getFileName().toString();
                if (!name.endsWith(".bin")) return;
                try {
                    UUID uuid = UUID.fromString(name.replace(".bin", ""));
                    data.put(uuid, Files.readAllBytes(path));
                } catch (Exception e) {
                    logger.warn("[PlayerStateStore] Could not load state for " + name + ": " + e.getMessage());
                }
            });
        } catch (IOException e) {
            logger.warn("[PlayerStateStore] Could not list player-states/: " + e.getMessage());
        }
    }

    private void persist(UUID uuid, byte[] state) {
        try {
            Files.createDirectories(directory);
            Files.write(directory.resolve(uuid + ".bin"), state);
        } catch (IOException e) {
            logger.warn("[PlayerStateStore] Could not save state for " + uuid + ": " + e.getMessage());
        }
    }
}
