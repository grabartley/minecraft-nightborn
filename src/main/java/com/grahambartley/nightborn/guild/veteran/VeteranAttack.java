package com.grahambartley.nightborn.guild.veteran;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public record VeteranAttack(
    Kind kind,
    String clip,
    Identifier sound,
    Optional<Identifier> mark,
    int windupTicks,
    int cooldownTicks,
    double reachMin,
    double reach,
    int count,
    double radius,
    int effectTicks,
    float heal,
    Optional<Identifier> held,
    Optional<Identifier> ammo,
    boolean shout,
    Trigger trigger) {

  public enum Kind implements StringIdentifiable {
    SILVER_CHAIN("silver_chain"),
    BOLT_VOLLEY("bolt_volley"),
    HOLY_WATER_ARC("holy_water_arc"),
    RALLYING_CRY("rallying_cry");

    public static final Codec<Kind> CODEC = StringIdentifiable.createCodec(Kind::values);

    private final String name;

    Kind(final String name) {
      this.name = name;
    }

    @Override
    public String asString() {
      return name;
    }
  }

  public record Trigger(
      double meleeReach, int crowd, double crowdReach, int stillTicks, double partyHealth) {

    public static final Trigger NONE = new Trigger(0.0, 0, 0.0, 0, 0.0);

    public static final Codec<Trigger> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.doubleRange(0.0, MOST_REACH)
                            .optionalFieldOf("melee_reach", 0.0)
                            .forGetter(Trigger::meleeReach),
                        Codec.intRange(0, MOST_COUNT)
                            .optionalFieldOf("crowd", 0)
                            .forGetter(Trigger::crowd),
                        Codec.doubleRange(0.0, MOST_REACH)
                            .optionalFieldOf("crowd_reach", 0.0)
                            .forGetter(Trigger::crowdReach),
                        Codec.intRange(0, MOST_TICKS)
                            .optionalFieldOf("still_ticks", 0)
                            .forGetter(Trigger::stillTicks),
                        Codec.doubleRange(0.0, 1.0)
                            .optionalFieldOf("party_health", 0.0)
                            .forGetter(Trigger::partyHealth))
                    .apply(instance, Trigger::new));
  }

  public static final int MOST_TICKS = 72000;
  public static final double MOST_REACH = 64.0;
  public static final int MOST_COUNT = 12;

  public static final Codec<VeteranAttack> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Kind.CODEC.fieldOf("kind").forGetter(VeteranAttack::kind),
                      Codec.STRING.fieldOf("clip").forGetter(VeteranAttack::clip),
                      Identifier.CODEC.fieldOf("sound").forGetter(VeteranAttack::sound),
                      Identifier.CODEC.optionalFieldOf("mark").forGetter(VeteranAttack::mark),
                      Codec.intRange(1, MOST_TICKS)
                          .fieldOf("windup_ticks")
                          .forGetter(VeteranAttack::windupTicks),
                      Codec.intRange(0, MOST_TICKS)
                          .fieldOf("cooldown_ticks")
                          .forGetter(VeteranAttack::cooldownTicks),
                      Codec.doubleRange(0.0, MOST_REACH)
                          .optionalFieldOf("reach_min", 0.0)
                          .forGetter(VeteranAttack::reachMin),
                      Codec.doubleRange(0.0, MOST_REACH)
                          .optionalFieldOf("reach", 0.0)
                          .forGetter(VeteranAttack::reach),
                      Codec.intRange(1, MOST_COUNT)
                          .optionalFieldOf("count", 1)
                          .forGetter(VeteranAttack::count),
                      Codec.doubleRange(0.0, MOST_REACH)
                          .optionalFieldOf("radius", 0.0)
                          .forGetter(VeteranAttack::radius),
                      Codec.intRange(0, MOST_TICKS)
                          .optionalFieldOf("effect_ticks", 0)
                          .forGetter(VeteranAttack::effectTicks),
                      Codec.floatRange(0.0F, 1024.0F)
                          .optionalFieldOf("heal", 0.0F)
                          .forGetter(VeteranAttack::heal),
                      Identifier.CODEC.optionalFieldOf("held").forGetter(VeteranAttack::held),
                      Identifier.CODEC.optionalFieldOf("ammo").forGetter(VeteranAttack::ammo),
                      Codec.BOOL.optionalFieldOf("shout", false).forGetter(VeteranAttack::shout),
                      Trigger.CODEC
                          .optionalFieldOf("trigger", Trigger.NONE)
                          .forGetter(VeteranAttack::trigger))
                  .apply(instance, VeteranAttack::new));
}
