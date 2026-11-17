package com.grahambartley.nightborn.mirror;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public final class MirrorItems {

  public static final Item HAND_MIRROR =
      Registry.register(
          Registries.ITEM,
          Identifier.of(Nightborn.MOD_ID, "hand_mirror"),
          new HandMirrorItem(new Item.Settings().maxCount(1)));

  private MirrorItems() {}

  public static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
        .register(entries -> entries.addAfter(Items.SPYGLASS, HAND_MIRROR));
    UseEntityCallback.EVENT.register(
        (player, world, hand, entity, hit) -> {
          if (!player.getStackInHand(hand).isOf(HAND_MIRROR)
              || player.isSpectator()
              || !(entity instanceof LivingEntity subject)) {
            return ActionResult.PASS;
          }
          if (player instanceof ServerPlayerEntity user) {
            Mirror.raise(user, subject);
          }
          return ActionResult.SUCCESS;
        });
  }
}
