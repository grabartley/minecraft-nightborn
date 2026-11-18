package com.grahambartley.nightborn.decor;

import com.grahambartley.nightborn.blood.BloodQuality;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.StringIdentifiable;

public record ChaliceContents(ChaliceBlood blood, int bottles) {

  public static final int CAPACITY = 7;
  public static final ChaliceContents EMPTY = new ChaliceContents(ChaliceBlood.EMPTY, 0);

  public static final Codec<ChaliceContents> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      StringIdentifiable.createCodec(ChaliceBlood::values)
                          .fieldOf("blood")
                          .forGetter(ChaliceContents::blood),
                      Codec.intRange(0, CAPACITY)
                          .fieldOf("bottles")
                          .forGetter(ChaliceContents::bottles))
                  .apply(instance, ChaliceContents::new));

  public ChaliceContents {
    if (blood == ChaliceBlood.EMPTY || bottles <= 0) {
      blood = ChaliceBlood.EMPTY;
      bottles = 0;
    } else {
      bottles = Math.min(CAPACITY, bottles);
    }
  }

  public boolean empty() {
    return bottles == 0;
  }

  public boolean full() {
    return bottles >= CAPACITY;
  }

  public Optional<BloodQuality> quality() {
    return blood.quality();
  }

  public boolean takes(final BloodQuality poured) {
    return !full() && (empty() || quality().orElseThrow() == poured);
  }

  public ChaliceContents poured(final BloodQuality poured) {
    return new ChaliceContents(ChaliceBlood.of(poured), bottles + 1);
  }

  public ChaliceContents less() {
    return new ChaliceContents(blood, bottles - 1);
  }

  public int stateBottles() {
    return Math.max(1, bottles);
  }
}
