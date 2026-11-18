package com.grahambartley.nightborn.rank;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.StringIdentifiable;

public record RankDeed(Act act, int minimumStage, Text description) {

  public enum Act implements StringIdentifiable {
    STAKE("stake"),
    CLEAR_HOUSEHOLD("clear_household"),
    END_ELDEST("end_eldest");

    public static final Codec<Act> CODEC = StringIdentifiable.createCodec(Act::values);

    private final String id;

    Act(final String id) {
      this.id = id;
    }

    @Override
    public String asString() {
      return id;
    }
  }

  public static final Codec<RankDeed> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Act.CODEC.fieldOf("act").forGetter(RankDeed::act),
                      Codec.intRange(0, Integer.MAX_VALUE)
                          .optionalFieldOf("minimum_stage", 0)
                          .forGetter(RankDeed::minimumStage),
                      TextCodecs.CODEC.fieldOf("description").forGetter(RankDeed::description))
                  .apply(instance, RankDeed::new));

  public boolean doneBy(final Act done, final int stageRank) {
    return act == done && stageRank >= minimumStage;
  }
}
