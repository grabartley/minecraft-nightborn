package com.grahambartley.nightborn.wild;

import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.GuildRumour;
import com.grahambartley.nightborn.guild.party.HunterPartySpawner;
import com.grahambartley.nightborn.mob.home.NightRoutine;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public final class FirstParty {

  static final double GREETING_REACH = 16.0;

  private FirstParty() {}

  static NightLog cross(
      final ServerWorld world,
      final ServerPlayerEntity player,
      final NightLog log,
      final int firstNights) {
    final boolean fixed = world.getDimension().hasFixedTime();
    final long time = world.getTimeOfDay();
    if (log.partyMet()
        || !log.isNew(NightRoutine.comingNight(fixed, time), firstNights)
        || NightTime.isNight(fixed, time)
        || Vampires.isVampire(player)
        || !HunterPartySpawner.spawnsParties(world)) {
      return log;
    }
    return HunterPartySpawner.gatherNear(world, player, world.getRandom())
        .map(master -> log.metParty(master.getUuid()))
        .orElse(log);
  }

  static NightLog greet(
      final ServerWorld world,
      final ServerPlayerEntity player,
      final NightLog log,
      final int firstNights) {
    if (log.greeter().isEmpty()) {
      return log;
    }
    if (!log.isNew(
        NightRoutine.comingNight(world.getDimension().hasFixedTime(), world.getTimeOfDay()),
        firstNights)) {
      return log.greeted();
    }
    if (!(world.getEntity(log.greeter().get()) instanceof GuildMasterEntity master)
        || !master.isAlive()
        || master.squaredDistanceTo(player) > GREETING_REACH * GREETING_REACH
        || Vampires.isVampire(player)) {
      return log;
    }
    player.sendMessage(
        Text.translatable("nightborn.party.greeting", master.getDisplayName()), false);
    GuildRumour.tell(player, master);
    return log.greeted();
  }
}
