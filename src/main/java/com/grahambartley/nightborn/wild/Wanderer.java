package com.grahambartley.nightborn.wild;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.Uuids;

public record Wanderer(Optional<UUID> watches, long lastNear) {

  public static final Codec<Wanderer> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Uuids.CODEC.optionalFieldOf("watches").forGetter(Wanderer::watches),
                      Codec.LONG.optionalFieldOf("last_near", 0L).forGetter(Wanderer::lastNear))
                  .apply(instance, Wanderer::new));

  public static Wanderer wild(final long now) {
    return new Wanderer(Optional.empty(), now);
  }

  public static Wanderer watching(final UUID player, final long now) {
    return new Wanderer(Optional.of(player), now);
  }

  public boolean watcher() {
    return watches.isPresent();
  }

  public Wanderer roaming() {
    return new Wanderer(Optional.empty(), lastNear);
  }

  public Wanderer seen(final long now) {
    return new Wanderer(watches, now);
  }

  public boolean leaves(final long now, final long leaveTicks) {
    return now - lastNear >= leaveTicks;
  }
}
