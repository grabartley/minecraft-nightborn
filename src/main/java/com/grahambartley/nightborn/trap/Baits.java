package com.grahambartley.nightborn.trap;

import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import com.grahambartley.nightborn.boss.ZoneShape;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class Baits {

  static final int SCENT_EVERY_TICKS = 20;

  private record Bait(
      RegistryKey<World> world, BlockPos spot, BaitPower power, long until, Set<UUID> told) {

    boolean lasts(final World at) {
      return world.equals(at.getRegistryKey()) && FieldRules.lasts(at.getTime(), until);
    }

    double squaredDistanceTo(final Vec3d point) {
      return point.squaredDistanceTo(spot.toBottomCenterPos());
    }
  }

  private static final List<Bait> BAITS = new ArrayList<>();

  private Baits() {}

  static void register() {
    ServerTickEvents.END_WORLD_TICK.register(Baits::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> clearAll());
  }

  public static void clearAll() {
    BAITS.clear();
  }

  public static void place(final LivingEntity caster, final BaitPower power, final BlockPos spot) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return;
    }
    BAITS.add(
        new Bait(
            world.getRegistryKey(),
            spot.toImmutable(),
            power,
            world.getTime() + power.durationTicks(),
            new HashSet<>()));
    ZoneMarkEntity.place(
            world,
            ZoneShape.circle(spot.getX() + 0.5, spot.getZ() + 0.5, power.size()),
            spot.getY() + 0.02,
            Optional.of(power.look()))
        .linger(power.durationTicks());
  }

  public static Optional<BlockPos> nearest(final World world, final Vec3d from) {
    return BAITS.stream()
        .filter(bait -> bait.lasts(world))
        .filter(bait -> FieldRules.inside(bait.squaredDistanceTo(from), bait.power().lureRange()))
        .min(Comparator.comparingDouble(bait -> bait.squaredDistanceTo(from)))
        .map(Bait::spot);
  }

  public static boolean lasts(final World world, final BlockPos spot) {
    return BAITS.stream().anyMatch(bait -> bait.spot().equals(spot) && bait.lasts(world));
  }

  private static void tick(final ServerWorld world) {
    BAITS.removeIf(
        bait ->
            bait.world().equals(world.getRegistryKey())
                && !FieldRules.lasts(world.getTime(), bait.until()));
    if (world.getTime() % SCENT_EVERY_TICKS != 0) {
      return;
    }
    for (final Bait bait : BAITS) {
      if (!bait.lasts(world)) {
        continue;
      }
      for (final ServerPlayerEntity player : world.getPlayers()) {
        if (Vampires.isVampire(player)
            && FieldRules.inside(bait.squaredDistanceTo(player.getPos()), bait.power().scentRange())
            && bait.told().add(player.getUuid())) {
          player.sendMessage(Text.translatable("nightborn.trap.bait_scent"), true);
        }
      }
    }
  }
}
