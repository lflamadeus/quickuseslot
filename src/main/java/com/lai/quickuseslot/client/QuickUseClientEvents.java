package com.lai.quickuseslot.client;

import com.lai.quickuseslot.ModNetwork;
import com.lai.quickuseslot.QuickUsePacket;
import com.lai.quickuseslot.QuickUseSlot;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Turns hotkey presses into requests to the server.
 *
 * <p>Only the press itself does any work: there is no per tick inventory scanning, and no packet is
 * sent unless a key was actually clicked.
 */
@Mod.EventBusSubscriber(modid = QuickUseSlot.MOD_ID, value = Dist.CLIENT)
public final class QuickUseClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        sendClicks(minecraft, QuickUseKeys.USE_QUICK_SLOT, false);
        sendClicks(minecraft, QuickUseKeys.USE_SNEAK_QUICK_SLOT, true);
    }

    private static void sendClicks(Minecraft minecraft, KeyMapping key, boolean sneak) {
        while (key.consumeClick()) {
            ModNetwork.sendToServer(QuickUsePacket.of(minecraft.hitResult, sneak));
        }
    }

    private QuickUseClientEvents() {
    }
}
