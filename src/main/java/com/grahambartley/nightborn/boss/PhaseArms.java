package com.grahambartley.nightborn.boss;

import java.util.Optional;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class PhaseArms {

  private PhaseArms() {}

  public static boolean changes(
      final boolean handEmpty, final boolean holdsIt, final boolean wantsOne) {
    return wantsOne ? !holdsIt : !handEmpty;
  }

  public static void hold(final MobEntity boss, final Optional<Identifier> named) {
    final Optional<Item> held = named.flatMap(Registries.ITEM::getOrEmpty);
    final ItemStack hand = boss.getMainHandStack();
    if (!changes(hand.isEmpty(), held.map(hand::isOf).orElse(false), held.isPresent())) {
      return;
    }
    boss.equipStack(EquipmentSlot.MAINHAND, held.map(ItemStack::new).orElse(ItemStack.EMPTY));
    boss.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0F);
  }
}
