package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

final class VictimHold {

  static final TagKey<EntityType<?>> HOLDS_HEIGHT =
      TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Nightborn.MOD_ID, "holds_height"));

  private VictimHold() {}

  static void apply(final LivingEntity victim, final Vec3d anchor) {
    final boolean holdsHeight =
        HoldRules.holdsHeight(
            victim.hasNoGravity(), victim.getType().isIn(HOLDS_HEIGHT), flying(victim));
    victim.stopRiding();
    victim.setVelocity(HoldRules.pinned(victim.getVelocity(), holdsHeight));
    if (victim instanceof MobEntity mob) {
      mob.getNavigation().stop();
    }
    if (HoldRules.drifted(anchor, victim.getPos(), holdsHeight)) {
      final Vec3d back = HoldRules.anchored(anchor, victim.getPos(), holdsHeight);
      if (victim instanceof ServerPlayerEntity player) {
        player.networkHandler.requestTeleport(
            back.x, back.y, back.z, player.getYaw(), player.getPitch());
      } else {
        victim.refreshPositionAfterTeleport(back);
      }
    }
    if (!HoldRules.slows(victim instanceof ServerPlayerEntity)) {
      return;
    }
    final StatusEffectInstance current = victim.getStatusEffect(StatusEffects.SLOWNESS);
    if (current == null || HoldRules.needsRefresh(current.getDuration(), current.getAmplifier())) {
      victim.addStatusEffect(
          new StatusEffectInstance(
              StatusEffects.SLOWNESS,
              HoldRules.HOLD_TICKS,
              HoldRules.SLOWNESS_AMPLIFIER,
              false,
              false,
              false));
    }
  }

  private static boolean flying(final LivingEntity victim) {
    return victim instanceof PlayerEntity player
        && (player.getAbilities().flying || player.isFallFlying());
  }
}
