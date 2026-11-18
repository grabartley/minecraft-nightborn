package com.grahambartley.nightborn.nemesis;

import com.grahambartley.nightborn.age.AgeNight;
import com.grahambartley.nightborn.config.VillageSettings;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.hunt.ArrivalSpot;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public final class NemesisVisits {

  private NemesisVisits() {}

  static void tick(final MinecraftServer server) {
    final NemesisLedger ledger = NemesisLedger.of(server);
    final ServerWorld overworld = server.getOverworld();
    final long time = overworld.getTimeOfDay();
    final long night = AgeNight.index(time);
    final boolean dark = NightTime.isNight(overworld.getDimension().hasFixedTime(), time);
    for (final UUID id : ledger.quarries()) {
      final ServerPlayerEntity quarry = server.getPlayerManager().getPlayer(id);
      if (quarry == null || !quarry.isAlive() || quarry.isSpectator()) {
        continue;
      }
      final ServerWorld world = quarry.getServerWorld();
      final boolean overworldNow = world.getRegistryKey() == World.OVERWORLD;
      final boolean openSky = overworldNow && world.isSkyVisible(quarry.getBlockPos());
      ledger.of(id).stream()
          .filter(
              entry -> NemesisRules.visitDue(night, entry.nextVisit(), dark, overworldNow, openSky))
          .findFirst()
          .ifPresent(entry -> visit(ledger, quarry, entry, night));
    }
  }

  static boolean visit(
      final NemesisLedger ledger,
      final ServerPlayerEntity quarry,
      final NemesisEntry entry,
      final long night) {
    final ServerWorld world = quarry.getServerWorld();
    final Optional<BlockPos> spot =
        ArrivalSpot.find(world, quarry.getBlockPos(), world.getRandom());
    if (spot.isEmpty()) {
      return false;
    }
    final BlockPos at = spot.get();
    final Optional<MobEntity> nemesis = arrive(world, entry, at);
    if (nemesis.isEmpty()) {
      return false;
    }
    final MobEntity mob = nemesis.get();
    final VillageSettings settings = ServerConfigService.current().villages();
    final int token = NemesisAttachment.get(mob).map(NemesisMark::token).orElse(entry.token());
    ledger.put(
        quarry.getUuid(),
        entry.visited(
            NemesisRules.nextVisit(
                night,
                settings.nemesisVisitFewest(),
                settings.nemesisVisitMost(),
                world.getRandom().nextInt(Integer.MAX_VALUE)),
            token));
    mob.setTarget(quarry);
    NemesisKinds.of(mob)
        .ifPresent(
            found -> {
              final int company =
                  NemesisRules.companions(found.kind().companions(), world.getRandom().nextInt(64));
              for (int i = 0; i < company; i++) {
                companion(world, mob, at, quarry);
              }
            });
    return true;
  }

  private static Optional<MobEntity> arrive(
      final ServerWorld world, final NemesisEntry entry, final BlockPos at) {
    if (world.getEntity(entry.mob()) instanceof MobEntity loaded && loaded.isAlive()) {
      loaded.requestTeleport(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
      return Optional.of(loaded);
    }
    if (entry.snapshot().isEmpty()) {
      return Optional.empty();
    }
    final NbtCompound saved = entry.snapshot().copy();
    final Optional<MobEntity> copy =
        EntityType.getEntityFromNbt(saved, world)
            .filter(MobEntity.class::isInstance)
            .map(MobEntity.class::cast);
    if (copy.isEmpty()) {
      return Optional.empty();
    }
    final MobEntity mob = copy.get();
    final int token = entry.token() + 1;
    NemesisAttachment.get(mob).ifPresent(mark -> NemesisAttachment.set(mob, mark.withToken(token)));
    mob.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 0.0F);
    if (!world.spawnEntity(mob)) {
      return Optional.empty();
    }
    NemesisWatch.track(mob);
    return Optional.of(mob);
  }

  private static void companion(
      final ServerWorld world,
      final MobEntity leader,
      final BlockPos at,
      final ServerPlayerEntity quarry) {
    final Entity made = leader.getType().create(world);
    if (!(made instanceof MobEntity mob)) {
      return;
    }
    final BlockPos column =
        at.add(world.getRandom().nextInt(5) - 2, 0, world.getRandom().nextInt(5) - 2);
    if (!world.isChunkLoaded(column.getX() >> 4, column.getZ() >> 4)) {
      return;
    }
    final BlockPos near = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, column);
    mob.refreshPositionAndAngles(near.getX() + 0.5, near.getY(), near.getZ() + 0.5, 0.0F, 0.0F);
    mob.initialize(world, world.getLocalDifficulty(near), SpawnReason.EVENT, null);
    if (world.spawnEntity(mob)) {
      mob.setTarget(quarry);
    }
  }
}
