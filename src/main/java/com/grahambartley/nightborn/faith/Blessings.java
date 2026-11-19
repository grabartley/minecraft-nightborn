package com.grahambartley.nightborn.faith;

import com.grahambartley.nightborn.coating.Coat;
import com.grahambartley.nightborn.coating.Coatings;
import com.grahambartley.nightborn.occult.Circles;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public final class Blessings {

  private Blessings() {}

  public static void bless(
      final LivingEntity caster, final LivingEntity wielder, final BlessingPower power) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return;
    }
    if (Circles.stopsPrayers(world, wielder.getPos())) {
      world.spawnParticles(
          ParticleTypes.SMOKE,
          wielder.getX(),
          wielder.getBodyY(0.6),
          wielder.getZ(),
          12,
          0.3,
          0.3,
          0.3,
          0.01);
      if (caster instanceof ServerPlayerEntity player) {
        player.sendMessage(Text.translatable("nightborn.faith.smothered"), true);
      }
      return;
    }
    wielder
        .getMainHandStack()
        .set(Coatings.COAT, Coat.blessing(world.getTime(), power.durationTicks()));
    world.spawnParticles(
        ParticleTypes.END_ROD,
        wielder.getX(),
        wielder.getBodyY(0.6),
        wielder.getZ(),
        16,
        0.3,
        0.4,
        0.3,
        0.02);
    if (wielder != caster && wielder instanceof ServerPlayerEntity player) {
      player.sendMessage(
          Text.translatable("nightborn.faith.blessed", caster.getDisplayName()), true);
    }
  }
}
