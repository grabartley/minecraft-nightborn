package com.grahambartley.nightborn.downed;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringIdentifiable;

public record DownedState(Phase phase, int ticksLeft) {

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
                          .forGetter(DownedState::ticksLeft))
                  .apply(instance, DownedState::new));

  public boolean isDowned() {
    return phase == Phase.DOWNED;
  }

  public int secondsLeft() {
    return (ticksLeft + 19) / 20;
  }
}
