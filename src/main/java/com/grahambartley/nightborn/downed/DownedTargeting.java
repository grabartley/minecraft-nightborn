package com.grahambartley.nightborn.downed;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class DownedTargeting {

  public static final TagKey<EntityType<?>> HUNTERS =
      TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Nightborn.MOD_ID, "hunters"));

  private DownedTargeting() {}

  public static boolean spares(final LivingEntity attacker, final LivingEntity target) {
    return DownedRules.spares(
        Downed.helpless(target), attacker instanceof MobEntity, attacker.getType().isIn(HUNTERS));
  }

  public static void forget(final MobEntity mob) {
    final LivingEntity target = mob.getTarget();
    if (target != null && spares(mob, target)) {
      mob.setTarget(null);
    }
  }
}
