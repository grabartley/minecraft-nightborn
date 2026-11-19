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
    Identifier mark,
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
    boolean shout) {

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
                      Identifier.CODEC.fieldOf("mark").forGetter(VeteranAttack::mark),
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
                      Codec.BOOL.optionalFieldOf("shout", false).forGetter(VeteranAttack::shout))
                  .apply(instance, VeteranAttack::new));
}
