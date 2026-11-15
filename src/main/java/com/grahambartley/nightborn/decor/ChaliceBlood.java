package com.grahambartley.nightborn.decor;

import com.grahambartley.nightborn.blood.BloodQuality;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.util.StringIdentifiable;

public enum ChaliceBlood implements StringIdentifiable {
  EMPTY("empty", null),
  ANIMAL("animal", BloodQuality.ANIMAL),
  VILLAGER("villager", BloodQuality.VILLAGER),
  PLAYER("player", BloodQuality.PLAYER);

  private final String id;
  private final BloodQuality quality;

  ChaliceBlood(final String id, final BloodQuality quality) {
    this.id = id;
    this.quality = quality;
  }

  public static ChaliceBlood of(final BloodQuality quality) {
    return Arrays.stream(values())
        .filter(blood -> blood.quality == quality)
        .findFirst()
        .orElseThrow();
  }

  public Optional<BloodQuality> quality() {
    return Optional.ofNullable(this.quality);
  }

  @Override
  public String asString() {
    return this.id;
  }
}
