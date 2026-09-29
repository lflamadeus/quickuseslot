package com.lai.quickuseslot.client;

import com.lai.quickuseslot.QuickUseSlot;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** The hotkeys that use the items stored in the quick use slots. */
@Mod.EventBusSubscriber(modid = QuickUseSlot.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class QuickUseKeys {

    /** Uses the plain quick use slot. */
    public static final KeyMapping USE_QUICK_SLOT = new KeyMapping("key.quickuseslot.use",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.quickuseslot.category");

    /** Uses the quick use slot that behaves as if shift were held. */
    public static final KeyMapping USE_SNEAK_QUICK_SLOT =
            new KeyMapping("key.quickuseslot.use_sneak", InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_B, "key.quickuseslot.category");

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(USE_QUICK_SLOT);
        event.register(USE_SNEAK_QUICK_SLOT);
    }

    private QuickUseKeys() {
    }
}
