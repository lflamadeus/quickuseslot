package com.lai.quickuseslot;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Declares the single client to server channel used by the hotkey.
 *
 * <p>Nothing is ever sent the other way, and nothing is sent unless the player actually presses the
 * key, so the channel costs nothing while idle.
 */
public final class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(QuickUseSlot.id("main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    public static void register() {
        CHANNEL.messageBuilder(QuickUsePacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(QuickUsePacket::encode)
                .decoder(QuickUsePacket::decode)
                .consumerMainThread(QuickUsePacket::handle)
                .add();
    }

    public static void sendToServer(QuickUsePacket packet) {
        CHANNEL.sendToServer(packet);
    }

    private ModNetwork() {
    }
}
