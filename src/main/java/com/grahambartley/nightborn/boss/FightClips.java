package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FightClips(
    String summon,
    String guard,
    String breakAway,
    String stagger,
    String takeoff,
    String land,
    String downedForm) {

  public static final FightClips NONE = new FightClips("", "", "", "", "", "", "");

  public static final Codec<FightClips> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.STRING.optionalFieldOf("summon", "").forGetter(FightClips::summon),
                      Codec.STRING.optionalFieldOf("guard", "").forGetter(FightClips::guard),
                      Codec.STRING
                          .optionalFieldOf("break_away", "")
                          .forGetter(FightClips::breakAway),
                      Codec.STRING.optionalFieldOf("stagger", "").forGetter(FightClips::stagger),
                      Codec.STRING.optionalFieldOf("takeoff", "").forGetter(FightClips::takeoff),
                      Codec.STRING.optionalFieldOf("land", "").forGetter(FightClips::land),
                      Codec.STRING
                          .optionalFieldOf("downed_form", "")
                          .forGetter(FightClips::downedForm))
                  .apply(instance, FightClips::new));

  public static String flightEdge(
      final boolean wasFlying, final boolean flying, final FightClips clips) {
    if (wasFlying == flying) {
      return "";
    }
    return flying ? clips.takeoff() : clips.land();
  }
}
