package com.grahambartley.nightborn.mob.household;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.DoubleSupplier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class HouseholdLife {

  static final String TEMPERAMENT_KEY = "Temperament";
  static final String GRIEF_KEY = "GriefUntil";

  @Nullable private Identifier temperament;
  private long griefUntil;
  private long talkingUntil;
  @Nullable private Engagement engagement;
  private final HandLoan handLoan = new HandLoan();
  private final Map<BlockPos, Long> skipped = new HashMap<>();

  public record Engagement(int partnerId, long until) {}

  public Identifier temperamentId(
      final DynamicRegistryManager registries, final DoubleSupplier roll) {
    if (temperament == null) {
      final Optional<Identifier> picked =
          Temperaments.pick(Temperaments.all(registries), roll.getAsDouble());
      picked.ifPresent(id -> temperament = id);
      return picked.orElse(Temperaments.FALLBACK);
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

  public void startTalking(final long until) {
    talkingUntil = until;
  }

  public void stopTalking() {
    talkingUntil = 0L;
  }

  public boolean talking(final long now) {
    return now < talkingUntil;
  }

  public void skip(final BlockPos spot, final long until) {
    skipped.put(spot.toImmutable(), until);
  }

  public boolean skips(final BlockPos spot, final long now) {
    skipped.values().removeIf(until -> now >= until);
    return skipped.containsKey(spot);
  }

  public HandLoan handLoan() {
    return handLoan;
  }

  public void write(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    if (temperament != null) {
      nbt.putString(TEMPERAMENT_KEY, temperament.toString());
    }
    nbt.putLong(GRIEF_KEY, griefUntil);
    handLoan.write(nbt, registries);
  }

  public void read(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    temperament =
        nbt.contains(TEMPERAMENT_KEY, NbtElement.STRING_TYPE)
            ? Identifier.tryParse(nbt.getString(TEMPERAMENT_KEY))
            : null;
    griefUntil = nbt.getLong(GRIEF_KEY);
    handLoan.read(nbt, registries);
  }
}
