package com.grahambartley.nightborn.accessory;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;

public final class BeltAmmo {

  private record Draw(ItemStack store, ItemStack drawn, ItemStack kind) {}

  private static final Map<LivingEntity, Draw> DRAWN = new WeakHashMap<>();

  private BeltAmmo() {}

  public static Optional<ItemStack> draw(final PlayerEntity shooter, final ItemStack weapon) {
    if (!(weapon.getItem() instanceof RangedWeaponItem ranged)
        || !RangedWeaponItem.getHeldProjectile(shooter, ranged.getHeldProjectiles()).isEmpty()) {
      return Optional.empty();
    }
    final Predicate<ItemStack> fits = ranged.getProjectiles();
    for (final WornStores.WornStore worn : WornStores.worn(shooter)) {
      final Optional<ItemStack> ammo =
          AccessoryStore.contents(worn.stack()).stream().filter(fits).findFirst();
      if (ammo.isPresent()) {
        final ItemStack drawn = ammo.get().copy();
        synchronized (DRAWN) {
          DRAWN.put(shooter, new Draw(worn.stack(), drawn, drawn.copy()));
        }
        return Optional.of(drawn);
      }
    }
    return Optional.empty();
  }

  public static void loaded(final LivingEntity shooter, final ItemStack projectile) {
    final Draw draw;
    synchronized (DRAWN) {
      draw = DRAWN.remove(shooter);
    }
    if (draw == null || draw.drawn() != projectile || shooter.getWorld().isClient()) {
      return;
    }
    final int used = used(draw.kind().getCount(), projectile.getCount());
    if (used > 0) {
      AccessoryStore.remove(
          draw.store(), held -> ItemStack.areItemsAndComponentsEqual(held, draw.kind()), used);
    }
  }

  static int used(final int before, final int after) {
    return Math.max(0, before - Math.max(0, after));
  }
}
