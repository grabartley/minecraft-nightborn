package com.grahambartley.nightborn.mob.home;

import com.grahambartley.nightborn.config.NightbornConfig;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class HomeLife {

  static final String HOME_KEY = "Home";
  static final String COFFIN_KEY = "Coffin";
  static final String OUTING_KEY = "OutingNight";
  static final String DECIDED_KEY = "DecidedNight";
  static final int COFFIN_CHECK_TICKS = 100;
  static final long CLAIM_RETRY_TICKS = 1200L;

  @Nullable private BlockPos home;
  @Nullable private BlockPos coffin;
  private long outingNight = NightRoutine.NO_NIGHT;
  private long decidedNight = NightRoutine.NO_NIGHT;
  private long nextClaim;

  public void settle(final BlockPos home) {
    this.home = home.toImmutable();
    this.coffin = null;
    this.nextClaim = 0L;
  }

  public Optional<BlockPos> home() {
    return Optional.ofNullable(home);
  }

  public Optional<BlockPos> coffin() {
    return Optional.ofNullable(coffin);
  }

  public boolean bedtime(final Entity vampire) {
    return NightRoutine.bedtime(
        vampire.getWorld().getDimension().hasFixedTime(), vampire.getWorld().getTimeOfDay());
  }

  public boolean out(final PathAwareEntity vampire, final boolean hungry) {
    if (home == null) {
      return false;
    }
    return NightRoutine.out(
        outingNight,
        NightRoutine.night(vampire.getWorld().getTimeOfDay()),
        hungry,
        headsHome(vampire));
  }

  public boolean headsHome(final PathAwareEntity vampire) {
    return home != null
        && NightRoutine.headsHome(
            vampire.getWorld().getDimension().hasFixedTime(),
            vampire.getWorld().getTimeOfDay(),
            Math.sqrt(distanceSquared(vampire.getPos())),
            vampire.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
  }

  public boolean withinRoam(final Entity vampire, final int radius) {
    return home == null || distanceSquared(vampire.getPos()) <= (double) radius * radius;
  }

  public boolean outOfRange(
      final PathAwareEntity vampire,
      final Entity target,
      final boolean provoked,
      final boolean hungry,
      final NightbornConfig config) {
    return home != null
        && NightRoutine.outOfRange(
            provoked,
            out(vampire, hungry),
            distanceSquared(vampire.getPos()),
            distanceSquared(target.getPos()),
            config.mobs().vampireChaseRange(),
            config.mobs().vampireFeedingRange(),
            config.mobs().vampireHomeRadius());
  }

  public void tick(
      final PathAwareEntity vampire,
      final boolean hungry,
      final NightbornConfig config,
      final Runnable onOuting) {
    if (home == null || !(vampire.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final long time = world.getTimeOfDay();
    final long night = NightRoutine.night(time);
    if (NightRoutine.decidesTonight(bedtime(vampire), decidedNight, night, hungry)) {
      decidedNight = night;
      if (NightRoutine.goesOut(
          vampire.getRandom().nextInt(100), config.mobs().vampireOutingChance())) {
        outingNight = night;
        onOuting.run();
      }
    }
    vampire.setPositionTarget(
        home,
        NightRoutine.roamRadius(
            out(vampire, hungry),
            config.mobs().vampireFeedingRange(),
            config.mobs().vampireHomeRadius()));
    if (vampire.age % COFFIN_CHECK_TICKS == 0) {
      checkCoffin(world, vampire, config.mobs().vampireHomeRadius());
    }
  }

  public void release(final ServerWorld world, final Entity vampire) {
    if (coffin != null) {
      HomeCoffins.release(world, coffin, vampire.getUuid());
      coffin = null;
    }
  }

  public void write(final NbtCompound nbt) {
    if (home != null) {
      nbt.putLong(HOME_KEY, home.asLong());
    }
    if (coffin != null) {
      nbt.putLong(COFFIN_KEY, coffin.asLong());
    }
    nbt.putLong(OUTING_KEY, outingNight);
    nbt.putLong(DECIDED_KEY, decidedNight);
  }

  public void read(final NbtCompound nbt) {
    home =
        nbt.contains(HOME_KEY, NbtElement.LONG_TYPE)
            ? BlockPos.fromLong(nbt.getLong(HOME_KEY))
            : null;
    coffin =
        nbt.contains(COFFIN_KEY, NbtElement.LONG_TYPE)
            ? BlockPos.fromLong(nbt.getLong(COFFIN_KEY))
            : null;
    outingNight =
        nbt.contains(OUTING_KEY, NbtElement.LONG_TYPE)
            ? nbt.getLong(OUTING_KEY)
            : NightRoutine.NO_NIGHT;
    decidedNight =
        nbt.contains(DECIDED_KEY, NbtElement.LONG_TYPE)
            ? nbt.getLong(DECIDED_KEY)
            : NightRoutine.NO_NIGHT;
  }

  private void checkCoffin(final ServerWorld world, final Entity vampire, final int radius) {
    if (coffin != null && !HomeCoffins.keeps(world, coffin, vampire.getUuid()).orElse(true)) {
      coffin = null;
    }
    if (coffin == null && world.getTime() >= nextClaim && HomeCoffins.loaded(world, home)) {
      coffin = HomeCoffins.claim(world, home, radius, vampire.getUuid()).orElse(null);
      if (coffin == null) {
        nextClaim = world.getTime() + CLAIM_RETRY_TICKS;
      }
    }
  }

  private double distanceSquared(final Vec3d pos) {
    return home == null ? 0.0 : pos.squaredDistanceTo(Vec3d.ofBottomCenter(home));
  }
}
