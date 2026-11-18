package com.grahambartley.nightborn.raid;

import com.grahambartley.nightborn.downed.DownedTargeting;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.home.Housemates;
import com.grahambartley.nightborn.mob.home.NightRoutine;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public final class HouseAlarm {

  private HouseAlarm() {}

  public static void wake(
      final ServerWorld world, final LivingEntity sleeper, final LivingEntity intruder) {
    if (sleeper instanceof VampireEntity vampire && vampire.homeLife().home().isPresent()) {
      raise(world, vampire.homeLife().home().get(), intruder);
      return;
    }
    rouse(sleeper, intruder);
  }

  public static void raise(
      final ServerWorld world, final BlockPos home, final LivingEntity intruder) {
    final double reach =
        NightRoutine.coffinReach(ServerConfigService.current().mobs().vampireHomeRadius());
    final Optional<BlockPos> house = Optional.of(home);
    for (final VampireEntity member :
        world.getEntitiesByClass(
            VampireEntity.class,
            new Box(home).expand(reach),
            mate ->
                mate.isAlive()
                    && mate.isSleeping()
                    && Housemates.share(house, mate.homeLife().home()))) {
      rouse(member, intruder);
    }
  }

  public static boolean intruder(final LivingEntity entity) {
    return HouseGuards.intrudes(
        entity instanceof PlayerEntity,
        Vampires.isVampire(entity),
        entity.isSpectator() || entity instanceof PlayerEntity player && player.isCreative(),
        entity.getType().isIn(DownedTargeting.HUNTERS));
  }

  private static void rouse(final LivingEntity sleeper, final LivingEntity intruder) {
    HouseNoise.forget(sleeper);
    if (sleeper instanceof ServerPlayerEntity player) {
      player.wakeUp(false, true);
      player.sendMessage(Text.translatable("nightborn.raid.woken"), true);
      return;
    }
    sleeper.wakeUp();
    if (sleeper instanceof VampireEntity vampire) {
      vampire.warnOff();
      if (intruder(intruder)) {
        vampire.setAttacker(intruder);
        vampire.setTarget(intruder);
      }
    }
  }
}
