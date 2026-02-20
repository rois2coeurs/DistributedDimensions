package com.edouardcourty.dd.paper.store;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores the position of a player just before they enter a portal.
 * Used to teleport them back if the target server is unreachable.
 * Persisted to disk so the position survives a server restart during a switch.
 */
public class PrePortalPositionStore {
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, StoredLocation>>() {}.getType();

    private record StoredLocation(String world, double x, double y, double z, float yaw, float pitch) {}

    private final Map<UUID, Location> positions = new ConcurrentHashMap<>();
    private final File file;
    private final JavaPlugin plugin;

    public PrePortalPositionStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "pre-portal-positions.json");
        load();
    }

    public void save(UUID uuid, Location location) {
        positions.put(uuid, location.clone());
        persist();
    }

    public Optional<Location> remove(UUID uuid) {
        Optional<Location> loc = Optional.ofNullable(positions.remove(uuid));
        if (loc.isPresent()) persist();
        return loc;
    }

    public void clear(UUID uuid) {
        if (positions.remove(uuid) != null) persist();
    }

    private void load() {
        if (!file.exists()) return;
        try {
            String json = Files.readString(file.toPath());
            Map<String, StoredLocation> raw = GSON.fromJson(json, MAP_TYPE);
            if (raw == null) return;
            raw.forEach((k, v) -> {
                World world = Bukkit.getWorld(v.world());
                if (world != null) {
                    positions.put(UUID.fromString(k), new Location(world, v.x(), v.y(), v.z(), v.yaw(), v.pitch()));
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("[PrePortalPositionStore] Could not load: " + e.getMessage());
        }
    }

    private void persist() {
        try {
            plugin.getDataFolder().mkdirs();
            Map<String, StoredLocation> raw = new HashMap<>();
            positions.forEach((uuid, loc) -> raw.put(uuid.toString(),
                new StoredLocation(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch())));
            Files.writeString(file.toPath(), GSON.toJson(raw));
        } catch (IOException e) {
            plugin.getLogger().warning("[PrePortalPositionStore] Could not save: " + e.getMessage());
        }
    }
}

