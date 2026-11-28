package com.grahambartley.nightborn.mob.brain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.util.StringIdentifiable;

public record Sharpness(float blur, Set<Sharpness.Reads> reads, float foresight) {

  public enum Reads implements StringIdentifiable {
    GROUP(Fact.GROUP),
    PLACE(Fact.PLACE);

    public static final Codec<Reads> CODEC = StringIdentifiable.createCodec(Reads::values);

    private final Set<Fact> facts;

    Reads(final Set<Fact> facts) {
      this.facts = facts;
    }

    @Override
    public String asString() {
      return name().toLowerCase(Locale.ROOT);
    }
  }

  public static final float MOST_BLUR = 0.9F;
  public static final float MOST_FORESIGHT = 2.0F;

  public static final Sharpness KEEN = new Sharpness(0.0F, EnumSet.allOf(Reads.class), 1.0F);

  public static final Codec<Sharpness> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(0.0F, MOST_BLUR)
                          .optionalFieldOf("blur", 0.0F)
                          .forGetter(Sharpness::blur),
                      Reads.CODEC
                          .listOf()
                          .<Set<Reads>>xmap(Sharpness::readsOf, List::copyOf)
                          .optionalFieldOf("reads", EnumSet.allOf(Reads.class))
                          .forGetter(Sharpness::reads),
                      Codec.floatRange(0.0F, MOST_FORESIGHT)
                          .optionalFieldOf("foresight", 1.0F)
                          .forGetter(Sharpness::foresight))
                  .apply(instance, Sharpness::new));

  public Sharpness {
    reads = readsOf(List.copyOf(reads));
  }

  public boolean reads(final Reads group) {
    return reads.contains(group);
  }

  public Set<Fact> visible(final Set<Fact> needed) {
    final Set<Fact> visible =
        needed.isEmpty() ? EnumSet.noneOf(Fact.class) : EnumSet.copyOf(needed);
    for (final Reads group : Reads.values()) {
      if (!reads(group)) {
        visible.removeAll(group.facts);
      }
    }
    return visible;
  }

  public double blurred(final double score, final double roll) {
    return score * (1.0 + blur * (2.0 * roll - 1.0));
  }

  public boolean blurs() {
    return blur > 0.0F;
  }

  private static Set<Reads> readsOf(final List<Reads> listed) {
    final Set<Reads> reads = EnumSet.noneOf(Reads.class);
    reads.addAll(listed);
    return reads;
  }
}
