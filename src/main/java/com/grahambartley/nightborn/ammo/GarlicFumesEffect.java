package com.grahambartley.nightborn.ammo;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.weakened.Weakened;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class GarlicFumesEffect extends InstantStatusEffect {

  public static final String WEAKENED_SOURCE = "garlic_bomb";
  static final int COLOUR = 0xE6DCC0;

  public static final RegistryEntry<StatusEffect> ENTRY =
      Registry.registerReference(
          Registries.STATUS_EFFECT,
          Identifier.of(Nightborn.MOD_ID, "garlic_fumes"),
          new GarlicFumesEffect());

  private GarlicFumesEffect() {
    super(StatusEffectCategory.NEUTRAL, COLOUR);
  }

  public static void register() {}

  @Override
  public void applyInstantEffect(
      @Nullable final Entity source,
      @Nullable final Entity attacker,
      final LivingEntity target,
      final int amplifier,
      final double proximity) {
    weaken(target);
  }

  @Override
  public boolean applyUpdateEffect(final LivingEntity entity, final int amplifier) {
    weaken(entity);
    return true;
  }

  private static void weaken(final LivingEntity target) {
    if (target.getWorld().isClient || !Vampires.isVampire(target)) {
      return;
    }
    final GarlicCloud cloud = GarlicCloud.of(ServerConfigService.current().gear());
    Weakened.add(target, WEAKENED_SOURCE, cloud.strength(), cloud.weakenTicks());
  }
}
