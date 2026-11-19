package com.grahambartley.nightborn.guild.raiding;

import com.grahambartley.nightborn.age.AgeAttachment;
import com.grahambartley.nightborn.age.AgeLadder;
import com.grahambartley.nightborn.age.AgeNight;
import com.grahambartley.nightborn.coffin.DaySkip;
import com.grahambartley.nightborn.config.HuntSettings;
import com.grahambartley.nightborn.core.SurfaceSpots;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.guild.party.HunterPartySpawner;
import com.grahambartley.nightborn.guild.veteran.VeteranHunterEntity;
import com.grahambartley.nightborn.house.HousePlace;
import com.grahambartley.nightborn.house.Houses;
import com.grahambartley.nightborn.hunt.HuntRecord;
import com.grahambartley.nightborn.hunt.HuntRules;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.village.VillageStep;
import com.grahambartley.nightborn.village.VillageSuspicion;
import com.grahambartley.nightborn.village.Villages;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public final class RaidDispatch {

  static final String SENT = "nightborn.raid.sent";

  private RaidDispatch() {}

  public static HuntRecord check(
      final ServerPlayerEntity player,
      final HuntRecord record,
      final long now,
      final HuntSettings settings) {
    final MinecraftServer server = player.server;
    final RaidingParties raids = RaidingParties.get(server);
    final HuntRecord settled =
        raids
            .takeEnded(player.getUuid())
            .filter(night -> RaidRules.restsAfter(record.phase()))
            .map(night -> record.ended(night, settings.quietNights()))
            .orElse(record);
    if (!Vampires.isVampire(player)) {
      return settled;
    }
    final boolean wanted =
        RaidRules.wanted(
            HuntRules.level(settled.notoriety(), settings), knownFace(server, player.getUuid()));
    final Optional<RaidingParty> underway = raids.against(player.getUuid());
    if (underway.isPresent()) {
      if (RaidRules.callsOff(underway.get().passing(), wanted, settled.phase())) {
        RaidEnding.end(server, underway.get(), RaidRules.End.CALLED_OFF);
      }
      return settled;
    }
    if (!RaidRules.sends(wanted, settled.quiet(AgeNight.index(now)), settled.phase())) {
      return settled;
    }
    final Optional<RaidArms> arms =
        AgeLadder.of(server.getRegistryManager())
            .rung(AgeAttachment.get(player).rank())
            .flatMap(rung -> RaidingData.arms(server.getRegistryManager(), rung.id()));
    if (arms.isEmpty()) {
      return settled;
    }
    final Optional<GlobalPos> house =
        Houses.get(server).of(player.getUuid()).flatMap(found -> HousePlace.of(server, found));
    if (!RaidRules.goesNow(house.isPresent(), now)) {
      return settled;
    }
    if (house.isPresent()) {
      final ServerWorld world = server.getWorld(house.get().dimension());
      final BlockPos place = house.get().pos();
      if (world == null || !SurfaceSpots.loaded(world, place.getX(), place.getZ())) {
        return settled;
      }
      HunterPartySpawner.spot(world, place, world.getRandom())
          .ifPresent(spot -> send(world, spot, player, house, arms.get(), now));
    } else if (player.getServerWorld().getRegistryKey() == World.OVERWORLD) {
      final ServerWorld world = player.getServerWorld();
      HunterPartySpawner.spot(world, player.getBlockPos(), world.getRandom())
          .ifPresent(spot -> send(world, spot, player, Optional.empty(), arms.get(), now));
    }
    return settled;
  }

  public static boolean concerns(final ServerPlayerEntity player) {
    final RaidingParties raids = RaidingParties.get(player.server);
    return raids.against(player.getUuid()).isPresent()
        || raids.hasEnded(player.getUuid())
        || Vampires.isVampire(player) && knownFace(player.server, player.getUuid());
  }

  public static void callOff(final ServerPlayerEntity player) {
    final RaidingParties raids = RaidingParties.get(player.server);
    raids
        .against(player.getUuid())
        .ifPresent(raid -> RaidEnding.end(player.server, raid, RaidRules.End.CALLED_OFF));
    raids.forget(player.getUuid());
  }

  static boolean knownFace(final MinecraftServer server, final UUID player) {
    final ServerWorld world = server.getOverworld();
    return Villages.of(world).all().keySet().stream()
        .anyMatch(
            bell ->
                VillageSuspicion.current(world, bell).knows(player)
                    && VillageSuspicion.step(world, bell) == VillageStep.HUNTED);
  }

  public static Optional<RaidingParty> send(
      final ServerWorld world,
      final BlockPos spot,
      final ServerPlayerEntity quarry,
      final Optional<GlobalPos> house,
      final RaidArms arms,
      final long now) {
    final Random random = world.getRandom();
    final RaidOrders orders =
        new RaidOrders(quarry.getUuid(), DaySkip.nextDusk(now), house.map(GlobalPos::pos));
    final List<UUID> members = new ArrayList<>();
    final int percent = ServerConfigService.current().parties().raidPartyPercent();
    arms.veteran()
        .filter(id -> arms.led(percent))
        .ifPresent(
            id -> {
              final VeteranHunterEntity veteran =
                  place(world, GuildMobs.VETERAN_HUNTER, spot, random);
              if (veteran != null) {
                veteran.serveAs(id);
                veteran.enlist(orders, "");
                members.add(veteran.getUuid());
              }
            });
    for (final var kit : arms.hunterKits(percent)) {
      final HunterEntity hunter = place(world, GuildMobs.HUNTER, spot, random);
      if (hunter != null) {
        hunter.enlist(orders, kit.toString());
        members.add(hunter.getUuid());
      }
    }
    if (members.isEmpty()) {
      return Optional.empty();
    }
    final RaidingParty raid =
        new RaidingParty(
            quarry.getUuid(), members, members.size(), orders.until(), house, Optional.empty());
    RaidingParties.get(world.getServer()).start(raid);
    quarry.sendMessage(Text.translatable(SENT), false);
    quarry.playSoundToPlayer(
        SoundEvents.EVENT_RAID_HORN.value(), SoundCategory.HOSTILE, 0.6F, 0.8F);
    return Optional.of(raid);
  }

  private static <T extends HunterEntity> T place(
      final ServerWorld world, final EntityType<T> type, final BlockPos spot, final Random random) {
    final BlockPos at =
        SurfaceSpots.at(
                world,
                spot.add(
                    random.nextBetween(-HunterPartySpawner.SPREAD, HunterPartySpawner.SPREAD),
                    0,
                    random.nextBetween(-HunterPartySpawner.SPREAD, HunterPartySpawner.SPREAD)))
            .orElse(spot);
    final T mob = type.create(world);
    if (mob == null) {
      return null;
    }
    mob.refreshPositionAndAngles(at, random.nextFloat() * 360.0F, 0.0F);
    mob.initialize(world, world.getLocalDifficulty(at), SpawnReason.EVENT, null);
    world.spawnEntityAndPassengers(mob);
    return mob;
  }
}
