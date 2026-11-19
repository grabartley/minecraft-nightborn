package com.grahambartley.nightborn.boss;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;

public final class SetWear {

  static final int ARMOR_SLOTS = 4;

  private SetWear() {}

  public static boolean complete(final int armorPieces, final boolean bladeHeld) {
    return armorPieces >= ARMOR_SLOTS && bladeHeld;
  }

  public static int armorPieces(final LivingEntity wearer, final TagKey<Item> armor) {
    int pieces = 0;
    for (final EquipmentSlot slot : EquipmentSlot.values()) {
      if (slot.isArmorSlot() && wearer.getEquippedStack(slot).isIn(armor)) {
        pieces++;
      }
    }
    return pieces;
  }

  public static boolean holds(final LivingEntity wielder, final TagKey<Item> blades) {
    return wielder.getMainHandStack().isIn(blades);
  }

  public static boolean wearsAll(
      final LivingEntity wearer, final TagKey<Item> armor, final TagKey<Item> blades) {
    return complete(armorPieces(wearer, armor), holds(wearer, blades));
  }
}
