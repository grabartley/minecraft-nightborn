package com.grahambartley.nightborn.ritual;

import com.grahambartley.nightborn.boss.Arena;
import com.grahambartley.nightborn.config.RitualSettings;
import com.grahambartley.nightborn.mob.EldestEntity;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.network.RitualGloomPayload;
import com.grahambartley.nightborn.smoke.SmokeParticles;
import com.grahambartley.nightborn.structure.StandingSpot;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class Ritual {

  static final float LOUD = 3.0F;

  private Ritual() {}

  public static List<ServerPlayerEntity> near(
      final ServerWorld world, final BlockPos altar, final int range) {
    final Vec3d centre = Vec3d.ofCenter(altar);
    return world.getPlayers(
        player ->
            !player.isSpectator() && player.squaredDistanceTo(centre) <= (double) range * range);
  }

  public static void begin(
      final ServerWorld world, final BlockPos altar, final RitualSettings settings) {
    final RitualGloomPayload gloom =
        new RitualGloomPayload(altar, settings.buildUpTicks(), settings.fightRange());
    near(world, altar, settings.fightRange())
        .forEach(player -> ServerPlayNetworking.send(player, gloom));
    world.playSound(null, altar, RitualSounds.BUILD_UP, SoundCategory.BLOCKS, LOUD, 1.0F);
  }

  public static void lift(final ServerWorld world, final BlockPos altar, final int range) {
    final RitualGloomPayload lifted = RitualGloomPayload.lifted(altar);
    near(world, altar, range).forEach(player -> ServerPlayNetworking.send(player, lifted));
  }

  public static void tick(final ServerWorld world, final BlockPos altar, final RitualBuildUp at) {
    final Vec3d top = Vec3d.ofCenter(altar).add(0.0, 0.6, 0.0);
    world.spawnParticles(
        SmokeParticles.VAMPIRE_SMOKE, top.x, top.y, top.z, at.smoke(), 0.6, 0.2, 0.6, 0.01);
    world.spawnParticles(ParticleTypes.SOUL, top.x, top.y, top.z, 1, 1.5, 0.4, 1.5, 0.02);
    if (at.tolls()) {
      world.playSound(
          null, altar, SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, LOUD, at.tollPitch());
    }
    if (at.gutters(world.getRandom().nextDouble())) {
      CandleGutter.douseOne(world, altar, world.getRandom());
    }
  }

  public static boolean summon(
      final ServerWorld world,
      final BlockPos altar,
      final Direction facing,
      final RitualSettings settings) {
    final EldestEntity eldest = NightbornMobs.ELDEST.create(world);
    if (eldest == null) {
      return false;
    }
    final BlockPos spot = SummonSpot.pick(altar, facing, pos -> stands(world, pos));
    eldest.refreshPositionAndAngles(
        spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, facing.asRotation(), 0.0F);
    eldest.initialize(world, world.getLocalDifficulty(spot), SpawnReason.MOB_SUMMONED, null);
    eldest.claimArena(new Arena(altar, settings.arenaRadius()));
    eldest.scaleFor(near(world, altar, settings.fightRange()).size(), settings);
    world.spawnEntityAndPassengers(eldest);
    eldest.riseFromRitual();
    strike(world, spot);
    world.spawnParticles(
        SmokeParticles.VAMPIRE_SMOKE,
        eldest.getX(),
        eldest.getBodyY(0.5),
        eldest.getZ(),
        60,
        0.4,
        0.9,
        0.4,
        0.05);
    world.playSound(null, altar, RitualSounds.SUMMON, SoundCategory.HOSTILE, LOUD, 1.0F);
    return true;
  }

  private static boolean stands(final ServerWorld world, final BlockPos pos) {
    return StandingSpot.fits(
        world.getBlockState(pos.down()), world.getBlockState(pos), world.getBlockState(pos.up()));
  }

  private static void strike(final ServerWorld world, final BlockPos spot) {
    final LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(world);
    if (bolt != null) {
      bolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(spot));
      bolt.setCosmetic(true);
      world.spawnEntity(bolt);
    }
  }
}
