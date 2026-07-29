package com.edouardcourty.dd.paper.messaging;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import org.bukkit.entity.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class EntityExtraDataCodec {
    private EntityExtraDataCodec() {}

    private static final Set<String> STRIPPED_KEYS = Set.of(
            "WorldUUIDMost", "WorldUUIDLeast"
    );

    public static String encode(Entity entity) {
        return NBT.get(entity, (ReadableNBT nbt) -> {
            ReadWriteNBT copy = NBT.createNBTObject();
            copy.mergeCompound(nbt);
            for (String key : STRIPPED_KEYS) copy.removeKey(key);
            return copy.toString();
        });
    }

    public static Map<String, String> parse(String extraData) {
        return new HashMap<>();
    }

    public static void apply(Entity entity, String extraData) {
        NBT.modify(entity, nbt -> {
            nbt.mergeCompound(NBT.parseNBT(extraData));
        });
    }
}
