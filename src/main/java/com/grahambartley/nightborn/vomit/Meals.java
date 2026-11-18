package com.grahambartley.nightborn.vomit;

import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.ward.WardTags;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public final class Meals {

  private Meals() {}

  public static void eaten(final ServerPlayerEntity player, final ItemStack food) {
    if (Vampires.isVampire(player)) {
      return;
    }
    final Hand other = player.getActiveHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
    Deeds.record(
        player,
        deed(
            Registries.ITEM.getId(food.getItem()),
            player.getStackInHand(other).isIn(WardTags.GARLIC)));
  }

  static Deed deed(final Identifier food, final boolean garlicInOtherHand) {
    return Deed.of(DeedKinds.ATE)
        .with(DeedKinds.FOOD, food)
        .with(DeedKinds.GARLIC_IN_OTHER_HAND, garlicInOtherHand);
  }
}
