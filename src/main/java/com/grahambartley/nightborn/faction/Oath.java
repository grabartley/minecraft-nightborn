package com.grahambartley.nightborn.faction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Oath(boolean proven, boolean sworn) {

  public static final Oath NONE = new Oath(false, false);

  public static final Codec<Oath> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.BOOL.optionalFieldOf("proven", false).forGetter(Oath::proven),
                      Codec.BOOL.optionalFieldOf("sworn", false).forGetter(Oath::sworn))
                  .apply(instance, Oath::new));

  public Oath prove() {
    return new Oath(true, sworn);
  }

  public Oath swear() {
    return new Oath(true, true);
  }

  public boolean keeps(final boolean hasHunterProgress) {
    return sworn || hasHunterProgress;
  }

  public boolean hunter(final boolean vampire, final boolean hasHunterProgress) {
    return !vampire && keeps(hasHunterProgress);
  }

  public boolean maySwear(final boolean vampire, final boolean hasHunterProgress) {
    return !vampire && proven && !keeps(hasHunterProgress);
  }
}
