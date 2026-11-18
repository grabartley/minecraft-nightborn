package com.grahambartley.nightborn.wild;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.guild.party.PartyLeader;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.smoke.SmokePoof;
import com.grahambartley.nightborn.structure.HouseholdRefill;
import com.grahambartley.nightborn.structure.HouseholdRoll;
import com.grahambartley.nightborn.structure.VampireStructureStarts;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.GlobalPos;

public final class Wanderers {

  private Wanderers() {}

  public static boolean holdsBack(final VampireEntity vampire) {
    return WandererAttachment.get(vampire).filter(Wanderer::watcher).isPresent();
  }

  static void tick(final ServerWorld world, final NightbornConfig config) {
    for (final VampireEntity vampire :
        world.getEntitiesByType(
            TypeFilter.instanceOf(VampireEntity.class),
            vampire -> vampire.isAlive() && WandererAttachment.get(vampire).isPresent())) {
      WandererAttachment.get(vampire)
          .ifPresent(
              wanderer -> {
                if (wanderer.watcher()) {
                  watch(world, vampire, wanderer, config);
                } else {
                  roam(world, vampire, wanderer, config);
                }
              });
    }
  }

  private static void roam(
      final ServerWorld world,
      final VampireEntity vampire,
      final Wanderer wanderer,
      final NightbornConfig config) {
    final long now = world.getTime();
    final Wanderer seen =
        world.getClosestPlayer(vampire, PartyLeader.PLAYER_NEAR) != null
            ? wanderer.seen(now)
            : wanderer;
    if (seen.leaves(
        now, (long) config.parties().partyLeaveMinutes() * PartyLeader.TICKS_PER_MINUTE)) {
      vampire.discard();
      return;
    }
    WandererAttachment.set(vampire, seen);
    VampireStructureStarts.homeContaining(world, vampire.getBlockPos())
        .filter(home -> HouseholdRefill.arrive(world, home))
        .ifPresent(
            home -> {
              WandererAttachment.clear(vampire);
              vampire.settleAt(home);
              HouseholdRoll.get(world.getServer())
                  .enlist(GlobalPos.create(world.getRegistryKey(), home), vampire.getUuid());
            });
  }

  private static void watch(
      final ServerWorld world,
      final VampireEntity vampire,
      final Wanderer wanderer,
      final NightbornConfig config) {
    final PlayerEntity watched = wanderer.watches().map(world::getPlayerByUuid).orElse(null);
    final boolean present = watched != null && watched.isAlive() && !watched.isSpectator();
    if (WildRules.watcherLeaves(
        present,
        present ? vampire.squaredDistanceTo(watched) : 0.0,
        config.firstNights().watcherLeaveDistance(),
        NightTime.isNight(world.getDimension().hasFixedTime(), world.getTimeOfDay()),
        vampire.getAttacker() != null)) {
      SmokePoof.burst(vampire, BatForm.ENTER_SOUND);
      vampire.discard();
    }
  }
}
