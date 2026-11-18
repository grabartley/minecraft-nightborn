package com.grahambartley.nightborn.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.GlobalPos;

public record HouseholdRecord(
    Set<UUID> living, Optional<GlobalPos> headCoffin, Optional<Long> refillAt) {

  public static final HouseholdRecord NONE =
      new HouseholdRecord(Set.of(), Optional.empty(), Optional.empty());

  public static final Codec<HouseholdRecord> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Uuids.CODEC
                          .listOf()
                          .<Set<UUID>>xmap(Set::copyOf, List::copyOf)
                          .optionalFieldOf("living", Set.of())
                          .forGetter(HouseholdRecord::living),
                      GlobalPos.CODEC
                          .optionalFieldOf("head_coffin")
                          .forGetter(HouseholdRecord::headCoffin),
                      Codec.LONG.optionalFieldOf("refill_at").forGetter(HouseholdRecord::refillAt))
                  .apply(instance, HouseholdRecord::new));

  public HouseholdRecord {
    living = Set.copyOf(living);
  }

  public HouseholdRecord enlisted(final UUID vampire) {
    if (living.contains(vampire)) {
      return this;
    }
    final Set<UUID> grown = new HashSet<>(living);
    grown.add(vampire);
    return new HouseholdRecord(grown, headCoffin, Optional.empty());
  }

  public HouseholdRecord forgot(final UUID vampire) {
    final Set<UUID> shrunk = new HashSet<>(living);
    shrunk.remove(vampire);
    return new HouseholdRecord(shrunk, headCoffin, refillAt);
  }

  public HouseholdRecord headSleepsAt(final GlobalPos coffin) {
    return new HouseholdRecord(living, Optional.of(coffin), refillAt);
  }

  public HouseholdRecord refillingAt(final long time) {
    return gone() ? new HouseholdRecord(living, headCoffin, Optional.of(time)) : this;
  }

  public HouseholdRecord refilledBy(final Set<UUID> arrivals) {
    return new HouseholdRecord(arrivals, headCoffin, Optional.empty());
  }

  public boolean gone() {
    return living.isEmpty();
  }

  public boolean awaitsRefillTime() {
    return gone() && refillAt.isEmpty();
  }

  public boolean refillDue(final long now) {
    return gone() && refillAt.filter(at -> now >= at).isPresent();
  }

  public boolean headSleepsIn(final GlobalPos coffin) {
    return headCoffin.filter(coffin::equals).isPresent();
  }
}
