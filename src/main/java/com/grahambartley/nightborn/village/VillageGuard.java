package com.grahambartley.nightborn.village;

import com.grahambartley.nightborn.config.PartySettings;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.party.HunterPartySpawner;
import com.grahambartley.nightborn.guild.party.PartyRoll;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;

public final class VillageGuard {

  private VillageGuard() {}

  public static boolean sends(
      final VillageStep step,
      final boolean guarded,
      final boolean sentTonight,
      final boolean spawning) {
    return step == VillageStep.HUNTED && !guarded && !sentTonight && spawning;
  }

  static VillageRecord keep(
      final ServerWorld world,
      final BlockPos bell,
      final VillageStep step,
      final VillageRecord record) {
    final List<GuildMasterEntity> guards = guards(world, bell);
    if (step != VillageStep.HUNTED) {
      guards.forEach(master -> master.lead(master.party().guarding(Optional.empty())));
      return record;
    }
    final long night = VillageSuspicion.night(world);
    if (!sends(
        step,
        !guards.isEmpty(),
        record.sentNight() == night,
        world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)
            && world.getGameRules().getBoolean(GameRules.DO_PATROL_SPAWNING))) {
      return record;
    }
    final Random random = world.getRandom();
    final PartySettings settings = ServerConfigService.current().parties();
    HunterPartySpawner.spot(world, bell, random)
        .flatMap(
            spot ->
                HunterPartySpawner.gather(
                    world,
                    spot,
                    PartyRoll.size(
                        random::nextInt, settings.partyMinSize(), settings.partyMaxSize()),
                    random))
        .ifPresent(master -> master.lead(master.party().guarding(Optional.of(bell))));
    return record.sent(night);
  }

  public static List<GuildMasterEntity> guards(final ServerWorld world, final BlockPos bell) {
    final double reach =
        VillageSuspicion.settings().villageReach() + (double) HunterPartySpawner.NEAR_MAX;
    return world.getEntitiesByClass(
        GuildMasterEntity.class,
        new Box(bell).expand(reach),
        master ->
            master.party() != null && master.party().guards().filter(bell::equals).isPresent());
  }
}
