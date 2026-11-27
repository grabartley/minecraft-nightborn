package com.grahambartley.nightborn.village;

import com.grahambartley.nightborn.config.PartySettings;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.party.HunterPartySpawner;
import com.grahambartley.nightborn.guild.party.PartyRoll;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.BellBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;

public final class VillageGuard {

  private VillageGuard() {}

  public static boolean sends(
      final boolean guarded, final boolean sentTonight, final boolean spawning) {
    return !guarded && !sentTonight && spawning;
  }

  public static boolean keepsGuarding(
      final boolean bellLoaded, final boolean bellStands, final VillageStep step) {
    return !bellLoaded || bellStands && step == VillageStep.HUNTED;
  }

  public static boolean relieves(final ServerWorld world, final Optional<BlockPos> bell) {
    return bell.filter(at -> !stillGuarded(world, at)).isPresent();
  }

  public static boolean stillGuarded(final ServerWorld world, final BlockPos bell) {
    final boolean loaded =
        world.isChunkLoaded(
            ChunkSectionPos.getSectionCoord(bell.getX()),
            ChunkSectionPos.getSectionCoord(bell.getZ()));
    return keepsGuarding(
        loaded,
        loaded && world.getBlockState(bell).getBlock() instanceof BellBlock,
        VillageStep.of(
            Villages.of(world).at(bell).map(VillageRecord::suspicion).orElse(0),
            VillageSuspicion.settings()));
  }

  static VillageRecord keep(
      final ServerWorld world,
      final BlockPos bell,
      final VillageStep step,
      final VillageRecord record) {
    if (step != VillageStep.HUNTED) {
      return record;
    }
    final List<GuildMasterEntity> guards = guards(world, bell);
    final long night = VillageSuspicion.night(world);
    if (!sends(
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
