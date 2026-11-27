package com.grahambartley.nightborn.mind;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.accessory.GiftScale;
import com.grahambartley.nightborn.faith.FaithMarks;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.skill.CastAim;
import com.grahambartley.nightborn.skill.MesmerisePower;
import com.grahambartley.nightborn.skill.PowerSight;
import com.grahambartley.nightborn.slayer.SlayerMarks;
import java.util.Locale;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class Mesmerise {

  public static final TagKey<EntityType<?>> UNMESMERISABLE =
      TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Nightborn.MOD_ID, "unmesmerisable"));

  static final DustParticleEffect GAZE_DUST =
      new DustParticleEffect(new Vector3f(0.55F, 0.1F, 0.45F), 1.0F);

  private Mesmerise() {}

  @Nullable
  public static LivingEntity target(
      final LivingEntity caster, final double range, final CastAim aim) {
    final LivingEntity candidate =
        caster instanceof MobEntity ? aim.subject(caster) : PowerSight.looked(caster, range);
    return candidate != null && holds(caster, candidate, range) ? candidate : null;
  }

  public static boolean holds(
      final LivingEntity caster, final LivingEntity target, final double range) {
    final Vec3d toward = target.getEyePos().subtract(caster.getEyePos());
    return target.isAlive()
        && Stare.holds(
            caster instanceof MobEntity,
            Stare.angleDegrees(caster.getRotationVec(1.0F), toward),
            toward.length(),
            range,
            caster.canSee(target));
  }

  public static void staring(final ServerWorld world, final LivingEntity target) {
    for (final ServerPlayerEntity viewer : world.getPlayers()) {
      if (viewer != target) {
        world.spawnParticles(
            viewer,
            GAZE_DUST,
            false,
            target.getX(),
            target.getEyeY(),
            target.getZ(),
            2,
            0.2,
            0.2,
            0.2,
            0.0);
      }
    }
  }

  public static MesmeriseRules.Outcome land(
      final LivingEntity caster, final LivingEntity target, final MesmerisePower power) {
    final MesmeriseRules.Outcome outcome = quietly(caster, target, power);
    if (caster instanceof PlayerEntity player) {
      player.sendMessage(
          Text.translatable(
              "nightborn.mesmerise." + outcome.name().toLowerCase(Locale.ROOT),
              target.getDisplayName()),
          true);
    }
    return outcome;
  }

  public static MesmeriseRules.Outcome quietly(
      final LivingEntity caster, final LivingEntity target, final MesmerisePower power) {
    final MesmeriseRules.Outcome outcome =
        MesmeriseRules.land(
            MesmeriseRules.immune(
                target.getType().isIn(UNMESMERISABLE),
                target == caster,
                target instanceof PlayerEntity player
                    && (player.isCreative() || player.isSpectator()),
                target.isAlive(),
                FaithMarks.SHELTERED.on(target) || SlayerMarks.STEADFAST.on(target)),
            target.getAttributes().hasAttribute(HunterAttributes.MESMERISE_RESISTANCE)
                ? target.getAttributeValue(HunterAttributes.MESMERISE_RESISTANCE)
                : 0.0,
            caster.getRandom().nextFloat());
    if (outcome == MesmeriseRules.Outcome.MESMERISED) {
      apply(caster, target, power);
    }
    return outcome;
  }

  static void apply(
      final LivingEntity caster, final LivingEntity target, final MesmerisePower power) {
    final boolean player = target instanceof PlayerEntity;
    final int ticks =
        GiftScale.ticks(
            power.durationFor(player), GiftAttributes.value(target, GiftAttributes.MIND_TIME));
    target.setAttached(MindTies.MESMERISED_BY, caster.getUuid());
    target.addStatusEffect(new StatusEffectInstance(MindEffect.MESMERISED, ticks), caster);
    if (player) {
      target.addStatusEffect(
          new StatusEffectInstance(StatusEffects.SLOWNESS, ticks, power.playerSlowness()), caster);
    }
    if (target instanceof MobEntity mob) {
      still(mob, caster);
    }
    target
        .getWorld()
        .playSound(
            null,
            target.getBlockPos(),
            SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE,
            SoundCategory.HOSTILE,
            0.8F,
            0.6F);
  }

  static void still(final MobEntity mob, final Entity caster) {
    mob.goalSelector.getGoals().stream()
        .filter(PrioritizedGoal::isRunning)
        .forEach(PrioritizedGoal::stop);
    mob.targetSelector.getGoals().stream()
        .filter(PrioritizedGoal::isRunning)
        .forEach(PrioritizedGoal::stop);
    mob.getNavigation().stop();
    mob.setTarget(null);
    mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
    mob.setAttacking(false);
    mob.getLookControl().lookAt(caster, 30.0F, 30.0F);
    mob.setVelocity(0.0, mob.getVelocity().y, 0.0);
  }
}
