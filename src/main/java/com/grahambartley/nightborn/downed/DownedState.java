package com.grahambartley.nightborn.downed;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;

public record DownedState(Phase phase, int ticksLeft, Optional<UUID> downedBy) {

  public enum Phase implements StringIdentifiable {
    DOWNED("downed"),
    ESCAPING("escaping");

    public static final Codec<Phase> CODEC = StringIdentifiable.createCodec(Phase::values);

    private final String id;

    Phase(final String id) {
      this.id = id;
    }

    @Override
    public String asString() {
      return id;
    }
  }

  public static final Codec<DownedState> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Phase.CODEC.fieldOf("phase").forGetter(DownedState::phase),
                      Codec.intRange(0, Integer.MAX_VALUE)
                          .fieldOf("ticksLeft")
                          .forGetter(DownedState::ticksLeft),
                      Uuids.INT_STREAM_CODEC
                          .optionalFieldOf("downedBy")
                          .forGetter(DownedState::downedBy))
                  .apply(instance, DownedState::new));

  public DownedState(final Phase phase, final int ticksLeft) {
    this(phase, ticksLeft, Optional.empty());
  }

  public DownedState next(final Phase nextPhase, final int nextTicksLeft) {
    return new DownedState(nextPhase, nextTicksLeft, downedBy);
  }

  public boolean isDowned() {
    return phase == Phase.DOWNED;
  }

  public int secondsLeft() {
    return (ticksLeft + 19) / 20;
  }
}
