package com.grahambartley.nightborn.guild.raiding;

import com.grahambartley.nightborn.age.AgeNight;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedFacts;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.gear.Staking;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.guild.party.PassingRaid;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public final class RaidEnding {

  static final int CHECK_TICKS = 20;
  static final String BEATEN = "nightborn.raid.beaten";
  static final String LEFT = "nightborn.raid.left";

  private RaidEnding() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(RaidEnding::afterDeath);
    ServerTickEvents.END_SERVER_TICK.register(RaidEnding::tick);
  }

  private static void afterDeath(final LivingEntity victim, final DamageSource source) {
    final MinecraftServer server = victim.getServer();
    if (server == null) {
      return;
    }
    final RaidingParties raids = RaidingParties.get(server);
    if (victim instanceof HunterEntity hunter) {
      hunter
          .raid()
          .flatMap(orders -> raids.against(orders.quarry()))
          .filter(raid -> raid.members().contains(hunter.getUuid()))
          .map(raid -> raid.without(hunter.getUuid()))
          .ifPresent(
              raid -> {
                if (RaidRules.beaten(raid.members().size())) {
                  end(server, raid, RaidRules.End.BEATEN);
                } else {
                  raids.put(raid);
                }
              });
    } else if (victim instanceof ServerPlayerEntity player && Staking.staked(source)) {
      raids.against(player.getUuid()).ifPresent(raid -> end(server, raid, RaidRules.End.STAKED));
    }
  }

  private static void tick(final MinecraftServer server) {
    if (server.getTicks() % CHECK_TICKS != 0) {
      return;
    }
    final long time = server.getOverworld().getTimeOfDay();
    RaidingParties.get(server).all().stream()
        .filter(raid -> time >= raid.until())
        .forEach(raid -> end(server, raid, RaidRules.End.DUSK));
  }

  public static void end(
      final MinecraftServer server, final RaidingParty raid, final RaidRules.End cause) {
    final RaidingParties raids = RaidingParties.get(server);
    raids.remove(raid.quarry());
    raids.endedFor(raid.quarry(), AgeNight.index(server.getOverworld().getTimeOfDay()));
    for (final ServerWorld world : server.getWorlds()) {
      for (final var member : raid.members()) {
        final Entity entity = world.getEntity(member);
        if (entity instanceof HunterEntity hunter) {
          if (raid.passing()) {
            hunter.standDown();
          } else {
            hunter.discard();
          }
        }
      }
    }
    raid.leader().ifPresent(leader -> PassingRaid.moveOn(server, leader));
    final ServerPlayerEntity quarry = server.getPlayerManager().getPlayer(raid.quarry());
    if (quarry == null) {
      return;
    }
    switch (cause) {
      case BEATEN -> {
        quarry.sendMessage(Text.translatable(BEATEN), false);
        Deeds.record(
            quarry,
            Deed.of(DeedKinds.RAIDING_PARTY_BEATEN)
                .with(DeedKinds.SIZE, raid.size())
                .with(DeedKinds.NEAREST_PLAYER, DeedFacts.nearestOtherPlayer(quarry)));
      }
      case DUSK, CALLED_OFF -> quarry.sendMessage(Text.translatable(LEFT), false);
      case STAKED -> {}
    }
  }
}
