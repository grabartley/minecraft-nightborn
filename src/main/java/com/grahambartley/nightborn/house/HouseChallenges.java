package com.grahambartley.nightborn.house;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.Uuids;

public record HouseChallenges(Optional<Challenge> pending, Map<UUID, Long> barredUntil) {

  public static final HouseChallenges NONE = new HouseChallenges(Optional.empty(), Map.of());

  public static final Codec<HouseChallenges> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Challenge.CODEC
                          .optionalFieldOf("pending")
                          .forGetter(HouseChallenges::pending),
                      Codec.unboundedMap(Uuids.STRING_CODEC, Codec.LONG)
                          .optionalFieldOf("barred_until", Map.of())
                          .forGetter(HouseChallenges::barredUntil))
                  .apply(instance, HouseChallenges::new));

  public HouseChallenges {
    barredUntil = Map.copyOf(barredUntil);
  }

  public boolean barred(final UUID member, final long night) {
    return Optional.ofNullable(barredUntil.get(member)).filter(until -> night < until).isPresent();
  }

  public HouseChallenges declared(final Challenge challenge) {
    return new HouseChallenges(Optional.of(challenge), barredUntil);
  }

  public HouseChallenges settled() {
    return new HouseChallenges(Optional.empty(), barredUntil);
  }

  public HouseChallenges lost(final UUID challenger, final long until) {
    final Map<UUID, Long> barred = new HashMap<>(barredUntil);
    barred.put(challenger, until);
    return new HouseChallenges(Optional.empty(), barred);
  }

  public HouseChallenges forgetting(final UUID member) {
    final Map<UUID, Long> barred = new HashMap<>(barredUntil);
    barred.remove(member);
    return new HouseChallenges(
        pending.filter(challenge -> !challenge.challenger().equals(member)), barred);
  }
}
