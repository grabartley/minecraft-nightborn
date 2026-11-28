package com.grahambartley.nightborn.guild.veteran;

import com.grahambartley.nightborn.guild.HunterKit;
import com.grahambartley.nightborn.guild.build.HunterBuild;
import com.grahambartley.nightborn.mob.brain.Mind;
import com.grahambartley.nightborn.mob.brain.Sharpness;
import com.grahambartley.nightborn.mob.combat.Presence;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record Veteran(
    Identifier weapon,
    Optional<Identifier> crucifix,
    double healthScale,
    double armor,
    List<Identifier> attacks,
    Optional<Identifier> rank,
    float boltDamage,
    Optional<Integer> thinkTicks,
    Optional<Sharpness> sharpness,
    Optional<Presence> presence) {

  public static final double MOST_SCALE = 16.0;
  public static final double MOST_ARMOR = 30.0;
  public static final float MOST_BOLT_DAMAGE = 64.0F;

  public static final Codec<Veteran> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("weapon").forGetter(Veteran::weapon),
                      Identifier.CODEC.optionalFieldOf("crucifix").forGetter(Veteran::crucifix),
                      Codec.doubleRange(1.0, MOST_SCALE)
                          .optionalFieldOf("health_scale", 1.0)
                          .forGetter(Veteran::healthScale),
                      Codec.doubleRange(0.0, MOST_ARMOR)
                          .optionalFieldOf("armor", 0.0)
                          .forGetter(Veteran::armor),
                      Identifier.CODEC
                          .listOf()
                          .optionalFieldOf("attacks", List.of())
                          .forGetter(Veteran::attacks),
                      Identifier.CODEC.optionalFieldOf("rank").forGetter(Veteran::rank),
                      Codec.floatRange(0.0F, MOST_BOLT_DAMAGE)
                          .optionalFieldOf("bolt_damage", 0.0F)
                          .forGetter(Veteran::boltDamage),
                      Codec.intRange(1, HunterBuild.MOST_THINK_TICKS)
                          .optionalFieldOf("think_ticks")
                          .forGetter(Veteran::thinkTicks),
                      Sharpness.CODEC.optionalFieldOf("sharpness").forGetter(Veteran::sharpness),
                      Presence.CODEC.optionalFieldOf("presence").forGetter(Veteran::presence))
                  .apply(instance, Veteran::new));

  public Veteran {
    attacks = List.copyOf(attacks);
  }

  public Veteran(
      final Identifier weapon,
      final Optional<Identifier> crucifix,
      final double healthScale,
      final double armor,
      final List<Identifier> attacks,
      final Optional<Identifier> rank,
      final float boltDamage) {
    this(
        weapon,
        crucifix,
        healthScale,
        armor,
        attacks,
        rank,
        boltDamage,
        Optional.empty(),
        Optional.empty(),
        Optional.empty());
  }

  public Mind mind(final Mind ranked) {
    return new Mind(
        thinkTicks.orElse(ranked.thinkTicks()),
        sharpness.orElse(ranked.sharpness()),
        ranked.disposition());
  }

  public HunterKit kit() {
    return new HunterKit(weapon, Optional.empty(), crucifix, false, 1, 0.0F);
  }

  public double health(final double hunterHealth) {
    return hunterHealth * healthScale;
  }
}
