package com.edouardcourty.dd.paper.messaging;

import com.edouardcourty.dd.common.model.Dimension;
import com.edouardcourty.dd.paper.util.ItemSerializer;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializes/deserializes a non-player entity for cross-server transfer.
 *
 * Format (recursive for passengers):
 *   targetDimension (UTF), destX/Y/Z (double)
 *   entityType (UTF), vx/vy/vz (double), customName (UTF, "null" if absent), fireTicks (int)
 *   hasItemData (boolean) → [itemBytes.length (int) + bytes]
 *   health (float, -1 if non-LivingEntity), maxHealth (float)
 *   hasChestInventory (boolean) → [item array]
 *   extraData (UTF, key=value separated by ';') — color, age, size, etc.
 *   passengerCount (int) → [recursive passengers without dimension/dest]
 *
 * Spawn : {@link EntitySpawner}
 * Specific attributes : {@link EntityExtraDataCodec}
 */
public class EntityTransferSerializer {

    private EntityTransferSerializer() {}

    public record EntityData(
        Dimension targetDimension,
        double destX, double destY, double destZ,
        String entityType,
        double vx, double vy, double vz,
        String customName,
        int fireTicks,
        byte[] itemData,
        float health,
        float maxHealth,
        ItemStack[] chestContents,
        String extraData,
        List<EntityData> passengers
    ) {}

    // ── Write ─────────────────────────────────────────────────────────────

    public static void writeRoot(ByteArrayDataOutput out, Entity entity, Dimension target, double destX, double destY, double destZ) {
        out.writeUTF(target.name());
        out.writeDouble(destX);
        out.writeDouble(destY);
        out.writeDouble(destZ);
        writeEntity(out, entity);
    }

    private static void writeEntity(ByteArrayDataOutput out, Entity entity) {
        out.writeUTF(entity.getType().name());

        var vel = entity.getVelocity();
        out.writeDouble(vel.getX());
        out.writeDouble(vel.getY());
        out.writeDouble(vel.getZ());

        var name = entity.customName();
        out.writeUTF(name != null ? net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(name) : "null");
        out.writeInt(entity.getFireTicks());

        if (entity instanceof Item item) {
            byte[] bytes = item.getItemStack().serializeAsBytes();
            out.writeBoolean(true);
            out.writeInt(bytes.length);
            out.write(bytes);
        } else {
            out.writeBoolean(false);
        }

        if (entity instanceof LivingEntity living) {
            out.writeFloat((float) living.getHealth());
            out.writeFloat((float) living.getMaxHealth());
        } else {
            out.writeFloat(-1f);
            out.writeFloat(-1f);
        }

        if (entity instanceof InventoryHolder holder) {
            ItemStack[] contents = holder.getInventory().getContents();
            out.writeBoolean(true);
            out.writeInt(contents.length);
            for (ItemStack stack : contents) ItemSerializer.write(out, stack);
        } else {
            out.writeBoolean(false);
        }

        out.writeUTF(EntityExtraDataCodec.encode(entity));

        List<Entity> passengers = entity.getPassengers();
        out.writeInt(passengers.size());
        for (Entity passenger : passengers) writeEntity(out, passenger);
    }

    // ── Read ───────────────────────────────────────────────────────────────

    public static EntityData readRoot(ByteArrayDataInput in) {
        Dimension target = Dimension.valueOf(in.readUTF());
        double destX = in.readDouble(), destY = in.readDouble(), destZ = in.readDouble();
        return readEntity(in, target, destX, destY, destZ);
    }

    private static EntityData readEntity(ByteArrayDataInput in, Dimension target, double destX, double destY, double destZ) {
        String type       = in.readUTF();
        double vx = in.readDouble(), vy = in.readDouble(), vz = in.readDouble();
        String rawName    = in.readUTF();
        String customName = "null".equals(rawName) ? null : rawName;
        int fireTicks     = in.readInt();

        byte[] itemData = null;
        if (in.readBoolean()) {
            int len = in.readInt();
            itemData = new byte[len];
            in.readFully(itemData);
        }

        float health    = in.readFloat();
        float maxHealth = in.readFloat();

        ItemStack[] chestContents = null;
        if (in.readBoolean()) {
            chestContents = new ItemStack[in.readInt()];
            for (int i = 0; i < chestContents.length; i++) chestContents[i] = ItemSerializer.read(in);
        }

        String extraData = in.readUTF();

        int passengerCount = in.readInt();
        List<EntityData> passengers = new ArrayList<>(passengerCount);
        for (int i = 0; i < passengerCount; i++) {
            passengers.add(readEntity(in, target, destX, destY, destZ));
        }

        return new EntityData(target, destX, destY, destZ, type, vx, vy, vz, customName,
            fireTicks, itemData, health, maxHealth, chestContents, extraData, passengers);
    }

    // ── Spawn (delegated to EntitySpawner) ──────────────────────────────────────

    public static Entity spawn(World world, Location loc, EntityData data) {
        return EntitySpawner.spawn(world, loc, data);
    }
}
