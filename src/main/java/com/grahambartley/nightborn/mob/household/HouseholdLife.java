package com.grahambartley.nightborn.mob.household;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.DoubleSupplier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class HouseholdLife {

  static final String TEMPERAMENT_KEY = "Temperament";
  static final String GRIEF_KEY = "GriefUntil";
  static final String BORROWED_KEY = "BorrowedHand";
  static final long RESCAN_TICKS = 1200L;

  @Nullable private Identifier temperament;
  private long griefUntil;
  private boolean borrowedHand;
  @Nullable private Engagement engagement;
  private Map<FurnitureKind, List<BlockPos>> furniture = Map.of();
  private long scannedAt = Long.MIN_VALUE;

  public record Engagement(int partnerId, long until) {}

  public Identifier temperamentId(
      final DynamicRegistryManager registries, final DoubleSupplier roll) {
    if (temperament == null) {
      temperament =
          Temperaments.pick(Temperaments.all(registries), roll.getAsDouble())
              .orElse(Temperaments.FALLBACK);
    }
    return temperament;
  }

  public Temperament temperament(
      final DynamicRegistryManager registries, final DoubleSupplier roll) {
    return Temperaments.of(registries, temperamentId(registries, roll));
  }

  public void grieve(final long now, final int seconds) {
    griefUntil = Math.max(griefUntil, now + seconds * 20L);
  }

  public boolean grieving(final long now) {
    return now < griefUntil;
  }

  public void engage(final int partnerId, final long until) {
    engagement = new Engagement(partnerId, until);
  }

  public void release() {
    engagement = null;
  }

  public Optional<Engagement> engagement(final long now) {
    if (engagement != null && now >= engagement.until()) {
      engagement = null;
    }
    return Optional.ofNullable(engagement);
  }

  public Map<FurnitureKind, List<BlockPos>> furniture(
      final ServerWorld world, final BlockPos home, final int radius) {
    if (Rescan.due(scannedAt, world.getTime(), RESCAN_TICKS)) {
      furniture = HomeFurniture.scan(world, home, radius);
      scannedAt = world.getTime();
    }
    return furniture;
  }

  public void borrowHand(final boolean borrowed) {
    borrowedHand = borrowed;
  }

  public boolean handBorrowed() {
    return borrowedHand;
  }

  public void write(final NbtCompound nbt) {
    nbt.putBoolean(BORROWED_KEY, borrowedHand);
    if (temperament != null) {
      nbt.putString(TEMPERAMENT_KEY, temperament.toString());
    }
    nbt.putLong(GRIEF_KEY, griefUntil);
  }

  public void read(final NbtCompound nbt) {
    temperament =
        nbt.contains(TEMPERAMENT_KEY, NbtElement.STRING_TYPE)
            ? Identifier.tryParse(nbt.getString(TEMPERAMENT_KEY))
            : null;
    griefUntil = nbt.getLong(GRIEF_KEY);
    borrowedHand = nbt.getBoolean(BORROWED_KEY);
  }
}
