package com.grahambartley.nightborn.guild.raiding;

import com.grahambartley.nightborn.coffin.CoffinBlock;
import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.mind.MindTies;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class RaidConduct {

  static final int EVERY_TICKS = 10;
  static final int LID_REACH = 2;
  public static final int PLACE_REACH = 4;
  static final int QUARRY_REACH = 6;

  private RaidConduct() {}

  public static boolean stays(final PathAwareEntity raider, final RaidOrders orders) {
    if (!(raider.getWorld() instanceof ServerWorld world)) {
      return true;
    }
    final MinecraftServer server = world.getServer();
    if (orders.over(server.getOverworld().getTimeOfDay())
        || !RaidingParties.get(server)
            .against(orders.quarry())
            .filter(raid -> raid.members().contains(raider.getUuid()))
            .isPresent()) {
      return false;
    }
    if (raider.age % EVERY_TICKS != 0 || MindTies.mesmerised(raider)) {
      return true;
    }
    final int chase = ServerConfigService.current().parties().raidChase();
    final Optional<LivingEntity> foe =
        foe(raider, chase)
            .or(
                () ->
                    orders.place().isPresent()
                        ? Optional.empty()
                        : quarry(
                            raider, server.getPlayerManager().getPlayer(orders.quarry()), chase));
    if (foe.isPresent()) {
      raider.setPositionTarget(foe.get().getBlockPos(), QUARRY_REACH);
      if (raider.getTarget() == null) {
        raider.setTarget(foe.get());
      }
    } else {
      orders.place().ifPresent(place -> raider.setPositionTarget(place, PLACE_REACH));
    }
    orders.place().ifPresent(place -> openLids(world, raider.getBlockPos()));
    return true;
  }

  public static boolean chases(
      final boolean sameWorld,
      final boolean alive,
      final boolean spectator,
      final double distance,
      final int chase) {
    return sameWorld && alive && !spectator && distance <= chase;
  }

  private static Optional<LivingEntity> foe(final PathAwareEntity raider, final int chase) {
    final LivingEntity current = raider.getTarget();
    if (current != null
        && HunterEntity.hunts(current)
        && chases(true, current.isAlive(), false, raider.distanceTo(current), chase)) {
      return Optional.of(current);
    }
    return raider
        .getWorld()
        .getEntitiesByClass(
            LivingEntity.class,
            raider.getBoundingBox().expand(chase),
            seen -> HunterEntity.hunts(seen) && seen.isAlive() && raider.canSee(seen))
        .stream()
        .min(Comparator.comparingDouble(raider::squaredDistanceTo));
  }

  private static Optional<LivingEntity> quarry(
      final PathAwareEntity raider, final ServerPlayerEntity quarry, final int chase) {
    return quarry != null
            && chases(
                quarry.getWorld() == raider.getWorld(),
                quarry.isAlive(),
                quarry.isSpectator(),
                raider.distanceTo(quarry),
                chase)
        ? Optional.of(quarry)
        : Optional.empty();
  }

  static void openLids(final ServerWorld world, final BlockPos center) {
    for (final BlockPos pos :
        BlockPos.iterate(
            center.add(-LID_REACH, -1, -LID_REACH), center.add(LID_REACH, LID_REACH, LID_REACH))) {
      final BlockState state = world.getBlockState(pos);
      if (CoffinSleepers.shelters(state)) {
        CoffinBlock.openLid(world, CoffinBlock.head(pos, state));
      }
    }
  }
}
