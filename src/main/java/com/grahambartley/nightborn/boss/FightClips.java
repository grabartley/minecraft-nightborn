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
    String downedForm,
    String fedForm,
    String rise,
    int riseTicks,
    String death,
    int deathTicks) {

  public static final FightClips NONE =
      new FightClips("", "", "", "", "", "", "", "", "", 0, "", 0);

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
                          .forGetter(FightClips::downedForm),
                      Codec.STRING.optionalFieldOf("fed_form", "").forGetter(FightClips::fedForm),
                      Codec.STRING.optionalFieldOf("rise", "").forGetter(FightClips::rise),
                      Codec.intRange(0, Integer.MAX_VALUE)
                          .optionalFieldOf("rise_ticks", 0)
                          .forGetter(FightClips::riseTicks),
                      Codec.STRING.optionalFieldOf("death", "").forGetter(FightClips::death),
                      Codec.intRange(0, Integer.MAX_VALUE)
                          .optionalFieldOf("death_ticks", 0)
                          .forGetter(FightClips::deathTicks))
                  .apply(instance, FightClips::new));

  public static String flightEdge(
      final boolean wasFlying, final boolean flying, final FightClips clips) {
    if (wasFlying == flying) {
      return "";
    }
    return flying ? clips.takeoff() : clips.land();
  }

  public String floor(final boolean fed) {
    return fed && !fedForm.isEmpty() ? fedForm : downedForm;
  }

  public ShownForm dying() {
    return new ShownForm("", "", death, false);
  }

  public long riseEnd(final long now) {
    return now + riseTicks;
  }

  public int deathHold() {
    return death.isEmpty() ? 0 : deathTicks;
  }
}
