package com.grahambartley.nightborn.inquisitor;

import com.grahambartley.nightborn.hunt.ArrivalSpot;
import com.grahambartley.nightborn.hunt.HuntSounds;
import com.grahambartley.nightborn.name.MobNaming;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class InquisitorArrival {

  static final float ARRIVAL_VOLUME = 3.0F;
  static final float ARRIVAL_PITCH = 0.6F;
  static final double HEARING = 32.0;

  private InquisitorArrival() {}

  public static Optional<GrandInquisitorEntity> arrive(
      final ServerPlayerEntity quarry, final boolean bell, final Optional<String> man) {
    final ServerWorld world = quarry.getServerWorld();
    final Optional<BlockPos> spot =
        ArrivalSpot.find(world, quarry.getBlockPos(), world.getRandom());
    if (spot.isEmpty()) {
      return Optional.empty();
    }
    final GrandInquisitorEntity inquisitor = InquisitorMobs.GRAND_INQUISITOR.create(world);
    if (inquisitor == null) {
      return Optional.empty();
    }
    final BlockPos at = spot.get();
    inquisitor.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 0.0F);
    inquisitor.initialize(world, world.getLocalDifficulty(at), SpawnReason.EVENT, null);
    man.ifPresentOrElse(
        name -> MobNaming.give(inquisitor, name), () -> MobNaming.ensure(inquisitor));
    inquisitor.setPersistent();
    if (!world.spawnEntity(inquisitor)) {
      return Optional.empty();
    }
    inquisitor.lookAtEntity(quarry, 360.0F, 360.0F);
    final List<ServerPlayerEntity> near =
        world.getPlayers(player -> player.squaredDistanceTo(inquisitor) <= HEARING * HEARING);
    inquisitor.hunt(quarry, bell, near);
    inquisitor.scaleToCrowd(world, ServerConfigService.current().inquisitor());
    world.playSound(
        null, at, HuntSounds.ARRIVAL, SoundCategory.HOSTILE, ARRIVAL_VOLUME, ARRIVAL_PITCH);
    quarry.sendMessage(Text.translatable("nightborn.hunt.arrived"), false);
    return Optional.of(inquisitor);
  }

  public static void dismiss(final ServerWorld world, final UUID man) {
    if (world.getEntity(man) instanceof GrandInquisitorEntity inquisitor) {
      inquisitor.depart();
    }
  }
}
