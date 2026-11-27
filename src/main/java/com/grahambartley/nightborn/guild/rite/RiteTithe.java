package com.grahambartley.nightborn.guild.rite;

import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public final class RiteTithe {

  private RiteTithe() {}

  public static boolean affords(final int held, final int price) {
    return held >= price;
  }

  public static int held(final ServerPlayerEntity hunter) {
    return hunter.getInventory().count(Items.EMERALD);
  }

  static boolean pay(final ServerPlayerEntity hunter, final int price) {
    if (!affords(held(hunter), price)) {
      return false;
    }
    hunter
        .getInventory()
        .remove(
            stack -> stack.isOf(Items.EMERALD),
            price,
            hunter.playerScreenHandler.getCraftingInput());
    return true;
  }
}
