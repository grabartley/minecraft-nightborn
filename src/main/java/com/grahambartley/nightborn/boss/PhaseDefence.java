package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.Identifier;

public record PhaseDefence(
    int guardHits,
    int windowTicks,
    float breakDamage,
    float breakAway,
    float guardKnock,
    int retargetChance,
    List<Identifier> counters) {

  public static final PhaseDefence NONE = new PhaseDefence(0, 60, 0.0F, 0.0F, 0.0F, 0, List.of());

  public enum Reaction {
    TAKE,
    GUARD,
    BREAK_AWAY
  }

  public static final Codec<PhaseDefence> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(0, 100)
                          .optionalFieldOf("guard_hits", 0)
                          .forGetter(PhaseDefence::guardHits),
                      Codec.intRange(1, 1200)
                          .optionalFieldOf("window_ticks", 60)
                          .forGetter(PhaseDefence::windowTicks),
                      Codec.floatRange(0.0F, 10000.0F)
                          .optionalFieldOf("break_damage", 0.0F)
                          .forGetter(PhaseDefence::breakDamage),
                      Codec.floatRange(0.0F, 32.0F)
                          .optionalFieldOf("break_away", 8.0F)
                          .forGetter(PhaseDefence::breakAway),
                      Codec.floatRange(0.0F, 8.0F)
                          .optionalFieldOf("guard_knock", 1.2F)
                          .forGetter(PhaseDefence::guardKnock),
                      Codec.intRange(0, 100)
                          .optionalFieldOf("retarget_chance", 0)
                          .forGetter(PhaseDefence::retargetChance),
                      Identifier.CODEC
                          .listOf()
                          .optionalFieldOf("counters", List.of())
                          .forGetter(PhaseDefence::counters))
                  .apply(instance, PhaseDefence::new));

  public PhaseDefence {
    counters = List.copyOf(counters);
  }

  public boolean guards(final int hitsInWindow) {
    return guardHits > 0 && hitsInWindow >= guardHits;
  }

  public boolean breaksAway(final float damageInWindow) {
    return breakDamage > 0.0F && damageInWindow >= breakDamage;
  }

  public Reaction before(final int hitsInWindow) {
    return guards(hitsInWindow) ? Reaction.GUARD : Reaction.TAKE;
  }

  public Reaction after(final float damageInWindow) {
    return breaksAway(damageInWindow) ? Reaction.BREAK_AWAY : Reaction.TAKE;
  }

  public boolean retargets(final boolean sameFoe, final int roll) {
    return !sameFoe && roll < retargetChance;
  }
}
