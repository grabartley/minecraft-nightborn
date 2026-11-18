package com.grahambartley.nightborn.hunt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;

public record HuntRecord(
    double notoriety, Phase phase, int warningsLeft, Optional<UUID> visitor, long quietUntil) {

  public enum Phase implements StringIdentifiable {
    NONE("none"),
    WARNING("warning"),
    DUE("due"),
    VISIT("visit");

    public static final Codec<Phase> CODEC = StringIdentifiable.createCodec(Phase::values);

    private final String name;

    Phase(final String name) {
      this.name = name;
    }

    @Override
    public String asString() {
      return name;
    }
  }

  public static final long NEVER = Long.MIN_VALUE;

  public static final HuntRecord EMPTY =
      new HuntRecord(0.0, Phase.NONE, 0, Optional.empty(), NEVER);

  public static final Codec<HuntRecord> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.DOUBLE
                          .optionalFieldOf("notoriety", 0.0)
                          .forGetter(HuntRecord::notoriety),
                      Phase.CODEC.optionalFieldOf("phase", Phase.NONE).forGetter(HuntRecord::phase),
                      Codec.INT
                          .optionalFieldOf("warnings_left", 0)
                          .forGetter(HuntRecord::warningsLeft),
                      Uuids.INT_STREAM_CODEC
                          .optionalFieldOf("visitor")
                          .forGetter(HuntRecord::visitor),
                      Codec.LONG
                          .optionalFieldOf("quiet_until", NEVER)
                          .forGetter(HuntRecord::quietUntil))
                  .apply(instance, HuntRecord::new));

  public HuntRecord {
    notoriety = Math.max(0.0, notoriety);
    warningsLeft = Math.max(0, warningsLeft);
    visitor = phase == Phase.VISIT ? visitor : Optional.empty();
  }

  public boolean quiet(final long night) {
    return night < quietUntil;
  }

  public long quietNightsLeft(final long night) {
    return night >= quietUntil ? 0L : quietUntil - night - 1;
  }

  public boolean idle(final long night) {
    return notoriety <= 0.0 && phase == Phase.NONE && !quiet(night);
  }

  public HuntRecord withNotoriety(final double value) {
    return new HuntRecord(value, phase, warningsLeft, visitor, quietUntil);
  }

  public HuntRecord warning(final int left) {
    return new HuntRecord(notoriety, Phase.WARNING, left, Optional.empty(), quietUntil);
  }

  public HuntRecord due() {
    return new HuntRecord(notoriety, Phase.DUE, 0, Optional.empty(), quietUntil);
  }

  public HuntRecord visitedBy(final UUID man) {
    return new HuntRecord(notoriety, Phase.VISIT, 0, Optional.of(man), quietUntil);
  }

  public HuntRecord ended(final long night, final int quietNights) {
    final long until =
        quietNights <= 0 ? quietUntil : Math.max(quietUntil, night + quietNights + 1);
    return new HuntRecord(notoriety, Phase.NONE, 0, Optional.empty(), until);
  }
}
