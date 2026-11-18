package com.grahambartley.nightborn.wild;

import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.guild.party.PartyLeader;
import com.grahambartley.nightborn.mob.home.NightRoutine;
import com.grahambartley.nightborn.server.ServerConfigService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;

public final class WildFeatures {

  static final int SECOND = 20;

  private WildFeatures() {}

  public static void register() {
    WandererAttachment.register();
    NightLogAttachment.register();
    RumourMaps.register();
    ServerTickEvents.END_WORLD_TICK.register(WildFeatures::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> DrainedAnimals.forgetAll());
  }

  private static void tick(final ServerWorld world) {
    if (world.getTime() % SECOND != 0) {
      return;
    }
    final NightbornConfig config = ServerConfigService.current();
    Wanderers.tick(world, config);
    DrainedAnimals.tick(world);
    if (world.getRegistryKey() != World.OVERWORLD) {
      return;
    }
    final boolean minute = world.getTime() % PartyLeader.TICKS_PER_MINUTE == 0;
    for (final ServerPlayerEntity player :
        world.getPlayers(player -> player.isAlive() && !player.isSpectator())) {
      NightLog log = NightLogAttachment.get(player).orElseGet(() -> begun(world, player, config));
      log = FirstParty.greet(world, player, log, config.firstNights().firstNights());
      if (minute) {
        log = WildVampires.night(world, player, log, config);
        log = FirstParty.cross(world, player, log, config.firstNights().firstNights());
        VillagerMutters.listen(world, player, log, config.firstNights().signRange());
        StructureSigns.show(
            world, player, config.firstNights().signChance(), config.firstNights().signRange());
      }
      NightLogAttachment.set(player, log);
    }
  }

  private static NightLog begun(
      final ServerWorld world, final ServerPlayerEntity player, final NightbornConfig config) {
    return NightLog.begun(
        NightRoutine.comingNight(world.getDimension().hasFixedTime(), world.getTimeOfDay()),
        player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)),
        config.firstNights().firstNights(),
        NightTime.DAY_LENGTH);
  }
}
