package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.feeding.BiteRules;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public final class Prey {

  private Prey() {}

  public static boolean isPrey(
      final boolean warmBlood, final boolean vampire, final boolean untouchablePlayer) {
    return BiteRules.drinkable(warmBlood, vampire) && !untouchablePlayer;
  }

  public static boolean hasDrinkableBlood(final LivingEntity target) {
    return BiteRules.drinkable(
        BloodQuality.of(target.getType()).isPresent(), Vampires.isVampire(target));
  }

  public static boolean isPrey(final LivingEntity target) {
    return isPrey(
        BloodQuality.of(target.getType()).isPresent(),
        Vampires.isVampire(target),
        target instanceof PlayerEntity player && (player.isCreative() || player.isSpectator()));
  }

  public static boolean mayBite(
      final boolean downedOrEscaping, final boolean prey, final boolean provoked) {
    return !downedOrEscaping && (prey || provoked);
  }

  public static boolean vampireHunts(final World world) {
    return NightTime.isNight(world.getDimension().hasFixedTime(), world.getTimeOfDay());
  }

  public static boolean batHunts(
      final boolean peaceful,
      final boolean fixedTime,
      final long timeOfDay,
      final boolean openSky) {
    return !peaceful && (!openSky || NightTime.isNight(fixedTime, timeOfDay));
  }
}
