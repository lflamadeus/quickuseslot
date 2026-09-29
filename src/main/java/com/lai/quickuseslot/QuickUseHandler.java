package com.lai.quickuseslot;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/**
 * Performs one vanilla right click with the item stored in a quick use slot.
 *
 * <p>The item is briefly moved into the main hand so that everything reading
 * {@code player.getMainHandItem()} - block placement, buckets, tool use, entity interaction - sees
 * it, and it is moved back into the slot afterwards together with whatever the use left behind
 * (a shrunk stack, a damaged stack, a filled bucket, or nothing at all).
 *
 * <p>The sneak variant additionally holds shift for the duration of the use, so the player gets
 * the "shift + right click" behaviour - placing a block against a chest instead of opening it,
 * using an item on an entity instead of interacting with it - without having to hold shift.
 */
public final class QuickUseHandler {

    private static final int SLOT_INDEX = 0;

    /** Matches the padding vanilla adds on top of the entity reach when validating a hit. */
    private static final double ENTITY_REACH_PADDING = 3.0D;

    /** Matches the padding vanilla adds on top of the block reach when validating a hit. */
    private static final double BLOCK_REACH_PADDING = 1.5D;

    public static void use(ServerPlayer player, boolean sneak, HitResult hit) {
        // Vanilla refuses to start a use while the hands are already busy.
        if (player.isUsingItem()) {
            return;
        }
        ICurioStacksHandler stacksHandler = CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(inventory -> inventory.getStacksHandler(
                        sneak ? QuickUseSlots.SNEAK_SLOT_ID : QuickUseSlots.SLOT_ID))
                .orElse(null);
        if (stacksHandler == null) {
            return;
        }
        IDynamicStackHandler stacks = stacksHandler.getStacks();
        ItemStack curio = stacks.getStackInSlot(SLOT_INDEX);
        if (curio.isEmpty()) {
            return;
        }

        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack held = player.getMainHandItem();
        boolean wasSneaking = player.isShiftKeyDown();
        player.setItemInHand(hand, curio);
        player.setShiftKeyDown(sneak || wasSneaking);
        try {
            InteractionResult result = interact(player, curio, hand, hit);
            settleUse(player, hand);
            if (result.shouldSwing()) {
                player.swing(hand, true);
            }
        } finally {
            // Items may replace, consume or damage the stack they were used from, so whatever ends
            // up in the hand is what belongs back in the slot.
            ItemStack used = player.getMainHandItem();
            player.setItemInHand(hand, held);
            player.setShiftKeyDown(wasSneaking);
            if (used != curio) {
                stacks.setStackInSlot(SLOT_INDEX, used);
            }
        }
    }

    /**
     * Mirrors the order a vanilla right click resolves in: the specific entity hit, the general
     * entity hit, the block hit, and finally using the item at air.
     */
    private static InteractionResult interact(ServerPlayer player, ItemStack stack,
                                              InteractionHand hand, HitResult hit) {
        InteractionResult result = InteractionResult.PASS;
        if (hit instanceof EntityHitResult entityHit) {
            Entity target = entityHit.getEntity();
            if (player.canReach(target, ENTITY_REACH_PADDING)) {
                Vec3 relative = entityHit.getLocation().subtract(target.position());
                result = ForgeHooks.onInteractEntityAt(player, target, relative, hand);
                if (result == null) {
                    result = target.interactAt(player, relative, hand);
                }
                if (!result.consumesAction()) {
                    result = player.interactOn(target, hand);
                }
            }
        } else if (hit instanceof BlockHitResult blockHit) {
            ServerLevel level = player.serverLevel();
            if (level.mayInteract(player, blockHit.getBlockPos())
                    && player.canReach(blockHit.getBlockPos(), BLOCK_REACH_PADDING)) {
                result = player.gameMode.useItemOn(player, level, stack, hand, blockHit);
            }
        }
        if (!result.consumesAction()) {
            result = player.gameMode.useItem(player, player.serverLevel(), stack, hand);
        }
        return result;
    }

    /**
     * A right click on an item that has to be held - a bow, a shield, food - only <em>starts</em>
     * the use; vanilla would carry it on over the following ticks. The stack is about to leave the
     * hand, so anything that would still be running has to be dealt with now: eating and drinking
     * is completed straight away, and everything else that needs a real charge up is dropped.
     * Without this the dangling use state would later complete against a hand holding something
     * else.
     */
    private static void settleUse(ServerPlayer player, InteractionHand hand) {
        if (!player.isUsingItem()) {
            return;
        }
        ItemStack using = player.getUseItem();
        UseAnim animation = using.getUseAnimation();
        if (animation == UseAnim.EAT || animation == UseAnim.DRINK) {
            ItemStack finished = using.finishUsingItem(player.level(), player);
            if (finished != using) {
                player.setItemInHand(hand, finished);
            }
        }
        player.stopUsingItem();
    }

    private QuickUseHandler() {
    }
}
