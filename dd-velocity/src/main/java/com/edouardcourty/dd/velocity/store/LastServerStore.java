package com.edouardcourty.dd.velocity.store;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LastServerStore {
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    private final Path filePath;
    private final Logger logger;
    private final Map<UUID, String> data = new HashMap<>();

    public LastServerStore(Path dataDirectory, Logger logger) {
        this.filePath = dataDirectory.resolve("last-servers.json");
        this.logger = logger;
        load();
    }

    public void save(UUID uuid, String serverName) {
        data.put(uuid, serverName);
        persist();
    }

    public Optional<String> get(UUID uuid) {
        return Optional.ofNullable(data.get(uuid));
    }

    private void load() {
        if (!Files.exists(filePath)) return;
        try {
            String json = Files.readString(filePath);
            Map<String, String> raw = GSON.fromJson(json, MAP_TYPE);
            if (raw != null) raw.forEach((k, v) -> data.put(UUID.fromString(k), v));
        } catch (IOException e) {
            logger.warn("Could not load last-servers.json: " + e.getMessage());
        }
    }

    private void persist() {
        try {
            Files.createDirectories(filePath.getParent());
            Map<String, String> raw = new HashMap<>();
            data.forEach((k, v) -> raw.put(k.toString(), v));
            Files.writeString(filePath, GSON.toJson(raw));
        } catch (IOException e) {
            logger.warn("Could not save last-servers.json: " + e.getMessage());
        }
    }
}
