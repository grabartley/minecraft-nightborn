package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.sight.Sight;
import com.grahambartley.nightborn.sight.Sights;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public final class Hexes {

  static final int MARK_COLOUR = 0x7A1E6B;

  public static final AttachmentType<HexMark> TYPE =
      AttachmentRegistry.createPersistent(Identifier.of(Nightborn.MOD_ID, "hex"), HexMark.CODEC);

  private Hexes() {}

  public static void register() {
    HexEffect.register();
  }

  public static void cast(
      final LivingEntity caster, final LivingEntity target, final HexPower power) {
    final int ticks = power.durationFor(target instanceof PlayerEntity);
    final long until = target.getWorld().getTime() + ticks;
    target.setAttached(TYPE, new HexMark(caster.getUuid(), power.healing(), until));
    target.addStatusEffect(new StatusEffectInstance(HexEffect.HEXED, ticks), caster);
    for (final StatusEffectInstance effect : power.effects()) {
      target.addStatusEffect(
          new StatusEffectInstance(
              effect.getEffectType(), ticks, effect.getAmplifier(), false, true, true),
          caster);
    }
    Sights.give(caster, Sight.mark(target.getUuid(), MARK_COLOUR, until));
    if (target.getWorld() instanceof ServerWorld world) {
      world.spawnParticles(
          ParticleTypes.WITCH,
          target.getX(),
          target.getEyeY(),
          target.getZ(),
          12,
          0.3,
          0.4,
          0.3,
          0.02);
    }
    target
        .getWorld()
        .playSound(
            null,
            target.getBlockPos(),
            SoundEvents.ENTITY_WITCH_CELEBRATE,
            SoundCategory.HOSTILE,
            0.8F,
            0.6F);
  }

  public static float heal(final LivingEntity entity, final float amount) {
    final HexMark mark = entity.getAttached(TYPE);
    if (mark == null) {
      return amount;
    }
    final long now = entity.getWorld().getTime();
    if (now >= mark.until()) {
      entity.removeAttached(TYPE);
      return amount;
    }
    return mark.heal(amount, now);
  }
}
