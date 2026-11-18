package com.grahambartley.nightborn.village;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.accessory.GiftScale;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.config.VillageSettings;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.familiar.FamiliarRegistry;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.thrall.ThrallRegistry;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;

public final class VillageSuspicion {

  private VillageSuspicion() {}

  public static Optional<BlockPos> bellOf(final VillagerEntity villager) {
    return villager
        .getBrain()
        .getOptionalRegisteredMemory(MemoryModuleType.MEETING_POINT)
        .filter(point -> point.dimension() == villager.getWorld().getRegistryKey())
        .map(GlobalPos::pos);
  }

  public static Optional<BlockPos> nearestBell(final ServerWorld world, final BlockPos pos) {
    return world
        .getPointOfInterestStorage()
        .getNearestPosition(
            type -> type.matchesKey(PointOfInterestTypes.MEETING),
            pos,
            settings().villageReach(),
            PointOfInterestStorage.OccupationStatus.ANY);
  }

  public static Optional<BlockPos> bellFor(final ServerWorld world, final LivingEntity entity) {
    return entity instanceof VillagerEntity villager
        ? bellOf(villager)
        : nearestBell(world, entity.getBlockPos());
  }

  public static boolean warded(final ServerWorld world, final BlockPos pos) {
    return nearestBell(world, pos)
        .filter(bell -> step(world, bell).atLeast(VillageStep.WARDED))
        .isPresent();
  }

  public static VillageRecord current(final ServerWorld world, final BlockPos bell) {
    return update(world, bell, UnaryOperator.identity());
  }

  public static VillageStep step(final ServerWorld world, final BlockPos bell) {
    return VillageStep.of(current(world, bell).suspicion(), settings());
  }

  public static void raise(
      final ServerWorld world,
      final BlockPos bell,
      final SuspicionCause cause,
      final Optional<UUID> face) {
    final int amount =
        GiftScale.count(
            VillageData.amount(world.getRegistryManager(), cause),
            feedingShare(world, cause, face));
    if (amount > 0) {
      update(world, bell, record -> record.raised(amount, night(world), face));
    }
  }

  private static double feedingShare(
      final ServerWorld world, final SuspicionCause cause, final Optional<UUID> face) {
    if (cause != SuspicionCause.SEEN_FEEDING) {
      return 1.0;
    }
    return face.map(id -> world.getServer().getPlayerManager().getPlayer(id))
        .map(vampire -> GiftAttributes.value(vampire, GiftAttributes.FEEDING_SUSPICION))
        .orElse(1.0);
  }

  public static void victim(
      final ServerWorld world, final LivingEntity victim, final SuspicionCause cause) {
    bellFor(world, victim).ifPresent(bell -> raise(world, bell, cause, Optional.empty()));
  }

  public static void charmed(final ServerWorld world, final VillagerEntity villager) {
    bellOf(villager)
        .ifPresent(
            bell ->
                update(
                    world, bell, record -> record.charmed(night(world), settings().charmFade())));
  }

  public static void gift(
      final ServerWorld world, final VillagerEntity villager, final PlayerEntity giver) {
    final VillageSettings settings = settings();
    bellOf(villager)
        .filter(bell -> knows(world, bell, giver.getUuid()))
        .ifPresent(
            bell ->
                update(
                    world,
                    bell,
                    record ->
                        record.gifted(
                            night(world), settings.giftFade(), settings.giftsPerNight())));
  }

  public static boolean knows(final ServerWorld world, final BlockPos bell, final UUID player) {
    return Villages.of(world).at(bell).filter(record -> record.knows(player)).isPresent();
  }

  public static boolean knows(final VillagerEntity villager, final PlayerEntity player) {
    return villager.getWorld() instanceof ServerWorld world
        && bellOf(villager).filter(bell -> knows(world, bell, player.getUuid())).isPresent();
  }

  public static void taken(
      final ServerWorld world, final Optional<BlockPos> bell, final TakenNeighbour neighbour) {
    bell.ifPresent(at -> update(world, at, record -> record.withTaken(neighbour)));
  }

  static VillageRecord update(
      final ServerWorld world, final BlockPos bell, final UnaryOperator<VillageRecord> change) {
    final Villages villages = Villages.of(world);
    final long night = night(world);
    final VillageRecord stored = villages.at(bell).orElseGet(() -> VillageRecord.fresh(night));
    final VillageRecord settled = settle(world, stored, night);
    final VillageRecord changed = change.apply(settled);
    villages.put(bell, changed);
    if (VillageStep.eased(
        VillageStep.of(stored.suspicion(), settings()),
        VillageStep.of(changed.suspicion(), settings()))) {
      eased(world, bell);
    }
    return changed;
  }

  private static void eased(final ServerWorld world, final BlockPos bell) {
    final double reach = settings().villageReach();
    world
        .getPlayers(
            player ->
                Vampires.isVampire(player)
                    && player.squaredDistanceTo(Vec3d.ofCenter(bell)) <= reach * reach)
        .forEach(player -> Deeds.record(player, Deed.of(DeedKinds.SUSPICION_EASED)));
  }

  private static VillageRecord settle(
      final ServerWorld world, final VillageRecord record, final long night) {
    final MinecraftServer server = world.getServer();
    final VillageRecord kept =
        record.keepingTaken(
            neighbour ->
                ThrallRegistry.of(server).keeps(neighbour.owner(), neighbour.servant())
                    || FamiliarRegistry.of(server).keeps(neighbour.owner(), neighbour.servant()));
    return kept.settled(
        night,
        kept.taken().size(),
        VillageData.amount(world.getRegistryManager(), SuspicionCause.TAKEN_NEIGHBOUR),
        settings().quietNightFade());
  }

  static long night(final ServerWorld world) {
    return NightTime.night(world.getTimeOfDay());
  }

  static VillageSettings settings() {
    return ServerConfigService.current().villages();
  }
}
