package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.house.Houses;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class DayRest {

  public static final String RESTED = "nightborn.coffin.rested";
  public static final String RESTED_AT_HOME = "nightborn.coffin.rested_at_home";

  private DayRest() {}

  public static void woke(final LivingEntity sleeper) {
    if (!(sleeper instanceof ServerPlayerEntity player)) {
      return;
    }
    final ServerWorld world = player.getServerWorld();
    if (!RestedRules.earned(
        Vampires.isVampire(player),
        CoffinSleepers.inCoffin(player),
        player.canResetTimeBySleeping(),
        NightTime.isNight(world.getDimension().hasFixedTime(), world.getTimeOfDay()),
        DaySleep.wholeDay(player))) {
      DaySleep.forget(player);
      return;
    }
    DaySleep.forget(player);
    final int level =
        RestedRules.level(
            Houses.get(world.getServer()).of(player.getUuid()),
            player.getSleepingPosition().flatMap(pos -> CoffinOwnership.owner(world, pos)));
    player.setHealth(player.getMaxHealth());
    player.addStatusEffect(
        new StatusEffectInstance(
            RestedEffect.ENTRY,
            RestedRules.ticksUntilDawn(world.getTimeOfDay()),
            level,
            false,
            false,
            true));
    player.sendMessage(
        Text.translatable(level == RestedRules.RESTED_AT_HOME ? RESTED_AT_HOME : RESTED), true);
    player.playSoundToPlayer(
        SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.PLAYERS, 0.6F, 0.8F);
    Deeds.record(player, Deed.of(DeedKinds.WOKE_RESTED));
  }
}
