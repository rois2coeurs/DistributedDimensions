package com.edouardcourty.dd.paper.util;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ItemSerializer {
    private ItemSerializer() {}

    public static void write(ByteArrayDataOutput out, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            out.writeInt(0);
        } else {
            byte[] bytes = item.serializeAsBytes();
            out.writeInt(bytes.length);
            out.write(bytes);
        }
    }

    public static ItemStack read(ByteArrayDataInput in) {
        int len = in.readInt();
        if (len == 0) return new ItemStack(Material.AIR);
        byte[] bytes = new byte[len];
        in.readFully(bytes);
        return ItemStack.deserializeBytes(bytes);
    }
}
