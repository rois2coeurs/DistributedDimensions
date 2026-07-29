package com.edouardcourty.dd.paper.messaging;

import com.edouardcourty.dd.common.model.Dimension;
import com.edouardcourty.dd.common.model.LocationData;
import com.edouardcourty.dd.paper.util.ItemSerializer;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Serializes and deserializes the full state of a player for cross-server switches.
 *
 * Message format (in order):
 *   playerUUID (UTF), targetDimension (UTF),
 *   x/y/z (double), yaw/pitch (float),
 *   xpLevel (int), xpProgress (float),
 *   foodLevel (int), saturation (float), exhaustion (float),
 *   gameMode (UTF),
 *   inventory contents, armor contents, offhand item
 *
 * Item format: int length (0 = empty/AIR), then bytes if not empty.
 */
public class PlayerStateSerializer {

    private PlayerStateSerializer() {}

    public record PlayerState(
        UUID playerUuid,
        Dimension targetDimension,
        LocationData location,
        boolean buildPortal,
        boolean transferData,
        int xpLevel,
        float xpProgress,
        int foodLevel,
        float saturation,
        float exhaustion,
        GameMode gameMode,
        ItemStack[] inventoryContents,
        ItemStack[] armorContents,
        ItemStack offhand,
        EntityTransferSerializer.EntityData vehicle,  // null if the player is not in a vehicle
        List<PotionEffect> potionEffects
    ) {}

    public static void write(ByteArrayDataOutput out, UUID playerUuid, Dimension target, LocationData dest, Player player, boolean buildPortal, boolean transferData) {
        out.writeUTF(playerUuid.toString());
        out.writeUTF(target.name());
        out.writeDouble(dest.x);
        out.writeDouble(dest.y);
        out.writeDouble(dest.z);
        out.writeFloat(dest.yaw);
        out.writeFloat(dest.pitch);
        out.writeBoolean(buildPortal);
        out.writeBoolean(transferData);

        if (transferData) {
            out.writeInt(player.getLevel());
            out.writeFloat(player.getExp());
            out.writeInt(player.getFoodLevel());
            out.writeFloat(player.getSaturation());
            out.writeFloat(player.getExhaustion());
            out.writeUTF(player.getGameMode().name());

            PlayerInventory inv = player.getInventory();
            writeItemArray(out, inv.getContents());
            writeItemArray(out, inv.getArmorContents());
            writeItem(out, inv.getItemInOffHand());

            // Potion effects
            Collection<PotionEffect> effects = player.getActivePotionEffects();
            out.writeInt(effects.size());
            for (PotionEffect effect : effects) {
                out.writeUTF(effect.getType().getKey().toString());
                out.writeInt(effect.getDuration());
                out.writeInt(effect.getAmplifier());
                out.writeBoolean(effect.isAmbient());
                out.writeBoolean(effect.hasParticles());
                out.writeBoolean(effect.hasIcon());
            }

            // Optional vehicle
            org.bukkit.entity.Entity vehicle = player.getVehicle();
            if (vehicle != null) {
                out.writeBoolean(true);
                EntityTransferSerializer.writeRoot(out, vehicle, target, dest.x, dest.y, dest.z);
                player.leaveVehicle();
            } else {
                out.writeBoolean(false);
            }
        }
    }

    public static PlayerState read(ByteArrayDataInput in) {
        UUID playerUuid       = UUID.fromString(in.readUTF());
        Dimension dimension   = Dimension.valueOf(in.readUTF());
        double x = in.readDouble(), y = in.readDouble(), z = in.readDouble();
        float yaw = in.readFloat(), pitch = in.readFloat();
        boolean buildPortal   = in.readBoolean();
        boolean transferData  = in.readBoolean();

        if (!transferData) {
            return new PlayerState(
                playerUuid, dimension,
                LocationData.of(x, y, z, yaw, pitch),
                buildPortal, false,
                0, 0f, 0, 0f, 0f, null,
                new ItemStack[0], new ItemStack[0], null, null, new ArrayList<>()
            );
        }

        int xpLevel           = in.readInt();
        float xpProgress      = in.readFloat();
        int foodLevel         = in.readInt();
        float saturation      = in.readFloat();
        float exhaustion      = in.readFloat();
        GameMode gameMode     = GameMode.valueOf(in.readUTF());
        ItemStack[] contents  = readItemArray(in);
        ItemStack[] armor     = readItemArray(in);
        ItemStack offhand     = readItem(in);

        // Potion effects
        int effectCount = in.readInt();
        List<PotionEffect> potionEffects = new ArrayList<>(effectCount);
        for (int i = 0; i < effectCount; i++) {
            PotionEffectType type = Registry.EFFECT.get(NamespacedKey.fromString(in.readUTF()));
            int duration  = in.readInt();
            int amplifier = in.readInt();
            boolean ambient   = in.readBoolean();
            boolean particles = in.readBoolean();
            boolean icon      = in.readBoolean();
            if (type != null) potionEffects.add(new PotionEffect(type, duration, amplifier, ambient, particles, icon));
        }

        // Optional vehicle
        EntityTransferSerializer.EntityData vehicle = in.readBoolean()
            ? EntityTransferSerializer.readRoot(in)
            : null;

        return new PlayerState(
            playerUuid, dimension,
            LocationData.of(x, y, z, yaw, pitch),
            buildPortal, true,
            xpLevel, xpProgress,
            foodLevel, saturation, exhaustion,
            gameMode, contents, armor, offhand, vehicle, potionEffects
        );
    }

    private static void writeItemArray(ByteArrayDataOutput out, ItemStack[] items) {
        out.writeInt(items.length);
        for (ItemStack item : items) ItemSerializer.write(out, item);
    }

    private static ItemStack[] readItemArray(ByteArrayDataInput in) {
        ItemStack[] items = new ItemStack[in.readInt()];
        for (int i = 0; i < items.length; i++) items[i] = ItemSerializer.read(in);
        return items;
    }

    private static void writeItem(ByteArrayDataOutput out, ItemStack item) {
        ItemSerializer.write(out, item);
    }

    private static ItemStack readItem(ByteArrayDataInput in) {
        return ItemSerializer.read(in);
    }
}
