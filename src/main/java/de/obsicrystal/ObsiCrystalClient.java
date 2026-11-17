package de.obsicrystal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Setzt du Obsidian, wechselt die Mod danach auf einen End-Kristall in deiner Hotbar (Slots 1-9).
 * Hast du keinen Kristall, passiert nichts und du setzt einfach nur den Block.
 * Die Kamera wird nicht bewegt, gezielt und platziert wird von dir.
 */
public class ObsiCrystalClient implements ClientModInitializer {

    private static BlockPos[] candidates;
    private static int timeout;

    @Override
    public void onInitializeClient() {
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (level.isClientSide()
                    && hand == InteractionHand.MAIN_HAND
                    && player.getItemInHand(hand).is(Items.OBSIDIAN)) {
                BlockPos clicked = hit.getBlockPos();
                // Je nach Zielblock landet das Obsidian auf dem Block selbst oder daneben.
                candidates = new BlockPos[] { clicked, clicked.relative(hit.getDirection()) };
                timeout = 10;
            }
            return InteractionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(ObsiCrystalClient::tick);
    }

    private static void tick(Minecraft client) {
        if (candidates == null) {
            return;
        }
        if (client.player == null || client.level == null || timeout-- <= 0) {
            candidates = null;
            return;
        }

        boolean placed = false;
        for (BlockPos pos : candidates) {
            if (client.level.getBlockState(pos).is(Blocks.OBSIDIAN)) {
                placed = true;
                break;
            }
        }
        if (!placed) {
            return; // auf die Bestaetigung vom Server warten
        }
        candidates = null;

        Inventory inv = client.player.getInventory();
        for (int slot = 0; slot < 9; slot++) {
            if (inv.getItem(slot).is(Items.END_CRYSTAL)) {
                inv.setSelectedSlot(slot);
                return;
            }
        }
    }
}
