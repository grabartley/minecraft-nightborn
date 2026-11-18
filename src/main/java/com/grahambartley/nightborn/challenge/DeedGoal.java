package com.grahambartley.nightborn.challenge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.util.StringIdentifiable;

public record DeedGoal(
    DeedMatch match,
    int count,
    Measure measure,
    Optional<String> key,
    Within within,
    Optional<DeedMatch> reset) {

  public static final int MOST = 100_000;

  public enum Measure implements StringIdentifiable {
    TOTAL,
    DISTINCT,
    MOST;

    public static final Codec<Measure> CODEC = StringIdentifiable.createCodec(Measure::values);

    @Override
    public String asString() {
      return name().toLowerCase(Locale.ROOT);
    }
  }

  public enum Within implements StringIdentifiable {
    ANY_TIME,
    ONE_NIGHT;

    public static final Codec<Within> CODEC = StringIdentifiable.createCodec(Within::values);

    @Override
    public String asString() {
      return name().toLowerCase(Locale.ROOT);
    }
  }

  public static final MapCodec<DeedGoal> MAP_CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      DeedMatch.MAP_CODEC.forGetter(DeedGoal::match),
                      Codec.intRange(1, MOST)
                          .optionalFieldOf("count", 1)
                          .forGetter(DeedGoal::count),
                      Measure.CODEC
                          .optionalFieldOf("measure", Measure.TOTAL)
                          .forGetter(DeedGoal::measure),
                      Codec.STRING.optionalFieldOf("key").forGetter(DeedGoal::key),
                      Within.CODEC
                          .optionalFieldOf("within", Within.ANY_TIME)
                          .forGetter(DeedGoal::within),
                      DeedMatch.CODEC.optionalFieldOf("reset_by").forGetter(DeedGoal::reset))
                  .apply(instance, DeedGoal::new));

  public Tally after(final Tally tally, final Deed deed, final OptionalLong night) {
    if (reset.filter(r -> r.matches(deed)).isPresent()) {
      return Tally.EMPTY;
    }
    if (!match.matches(deed)) {
      return tally;
    }
    Tally counting = tally;
    if (within == Within.ONE_NIGHT) {
      if (night.isEmpty()) {
        return tally;
      }
      if (tally.night() != night.getAsLong()) {
        counting = Tally.startingOn(night.getAsLong());
      }
    }
    return switch (measure) {
      case TOTAL -> counting.plus(deed.amount());
      case MOST -> counting.atLeast(deed.amount());
      case DISTINCT -> key.flatMap(deed::fact).map(counting::seeing).orElse(counting);
    };
  }

  public int progress(final Tally tally) {
    return Math.min(count, measure == Measure.DISTINCT ? tally.seen().size() : tally.total());
  }

  public boolean done(final Tally tally) {
    return progress(tally) >= count;
  }
}
