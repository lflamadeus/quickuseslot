package com.lai.quickuseslot;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

/**
 * "Use the item in a quick use slot", sent when the player presses one of the hotkeys.
 *
 * <p>The client is authoritative for what the player is aiming at - that is how vanilla right
 * clicks work too - so the hit result travels with the request and the server re-validates it
 * before anything happens.
 */
public final class QuickUsePacket {

    private static final byte MISS = 0;
    private static final byte BLOCK = 1;
    private static final byte ENTITY = 2;

    /** Whether the use should behave as if shift were held. */
    private final boolean sneak;

    /** Set for a block hit, otherwise {@code null}. */
    @Nullable
    private final BlockHitResult blockHit;

    /** Entity id for an entity hit, otherwise {@code -1}. */
    private final int entityId;

    /** Absolute hit position for an entity hit, otherwise {@code null}. */
    @Nullable
    private final Vec3 entityHit;

    private QuickUsePacket(boolean sneak, @Nullable BlockHitResult blockHit, int entityId,
                           @Nullable Vec3 entityHit) {
        this.sneak = sneak;
        this.blockHit = blockHit;
        this.entityId = entityId;
        this.entityHit = entityHit;
    }

    /** Snapshots the client's current target. A {@code null} or unmatched result means "at air". */
    public static QuickUsePacket of(@Nullable HitResult hit, boolean sneak) {
        if (hit instanceof BlockHitResult blockHit) {
            return new QuickUsePacket(sneak, blockHit, -1, null);
        }
        if (hit instanceof EntityHitResult entityHit) {
            return new QuickUsePacket(sneak, null, entityHit.getEntity().getId(),
                    entityHit.getLocation());
        }
        return new QuickUsePacket(sneak, null, -1, null);
    }

    public static void encode(QuickUsePacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.sneak);
        if (packet.blockHit != null) {
            buf.writeByte(BLOCK);
            buf.writeBlockHitResult(packet.blockHit);
        } else if (packet.entityId >= 0 && packet.entityHit != null) {
            buf.writeByte(ENTITY);
            buf.writeVarInt(packet.entityId);
            buf.writeDouble(packet.entityHit.x);
            buf.writeDouble(packet.entityHit.y);
            buf.writeDouble(packet.entityHit.z);
        } else {
            buf.writeByte(MISS);
        }
    }

    public static QuickUsePacket decode(FriendlyByteBuf buf) {
        boolean sneak = buf.readBoolean();
        return switch (buf.readByte()) {
            case BLOCK -> new QuickUsePacket(sneak, buf.readBlockHitResult(), -1, null);
            case ENTITY -> new QuickUsePacket(sneak, null, buf.readVarInt(),
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
            default -> new QuickUsePacket(sneak, null, -1, null);
        };
    }

    public static void handle(QuickUsePacket packet, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) {
            return;
        }
        HitResult hit = packet.blockHit;
        if (hit == null && packet.entityHit != null) {
            Entity entity = player.serverLevel().getEntity(packet.entityId);
            if (entity != null) {
                hit = new EntityHitResult(entity, packet.entityHit);
            }
        }
        QuickUseHandler.use(player, packet.sneak, hit);
    }
}
