package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.clothing.WornPowers;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public final class AccessoryTrinket implements Trinket {

  public static final AccessoryTrinket INSTANCE = new AccessoryTrinket();
  static final String SOURCE_PREFIX = "accessory/";

  private AccessoryTrinket() {}

  public static String source(final String slotId) {
    return SOURCE_PREFIX + slotId;
  }

  @Override
  public void onEquip(final ItemStack stack, final SlotReference slot, final LivingEntity entity) {
    if (!entity.getWorld().isClient()) {
      WornPowers.wear(entity, source(slot.getId()), stack);
      AccessoryGifts.apply(entity);
    }
  }

  @Override
  public void onUnequip(
      final ItemStack stack, final SlotReference slot, final LivingEntity entity) {
    if (!entity.getWorld().isClient()) {
      WornPowers.takeOff(entity, source(slot.getId()));
      AccessoryGifts.apply(entity);
    }
  }
}
