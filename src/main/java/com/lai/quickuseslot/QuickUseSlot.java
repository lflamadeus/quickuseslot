package com.lai.quickuseslot;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

/**
 * Entry point of the mod.
 *
 * <p>The mod owns exactly two things: two Curios slots that accept any item, and hotkeys that
 * perform a single vanilla right click with whatever is stored in them.
 */
@Mod(QuickUseSlot.MOD_ID)
public final class QuickUseSlot {

    public static final String MOD_ID = "quickuseslot";

    public QuickUseSlot() {
        QuickUseSlots.register();
        ModNetwork.register();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
