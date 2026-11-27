package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.mob.brain.Intent;
import com.grahambartley.nightborn.mob.brain.MobUse;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.Identifier;

public record MobPowerUse(
    float minRange,
    float maxRange,
    int tellTicks,
    Identifier tellSound,
    String tellClip,
    boolean dodge,
    List<MobUse> uses) {

  public static final float MAX_RANGE = 64.0F;

  public static final Codec<MobPowerUse> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(0.0F, MAX_RANGE)
                          .optionalFieldOf("min_range", 0.0F)
                          .forGetter(MobPowerUse::minRange),
                      Codec.floatRange(0.0F, MAX_RANGE)
                          .fieldOf("max_range")
                          .forGetter(MobPowerUse::maxRange),
                      Codec.intRange(1, 100)
                          .fieldOf("tell_ticks")
                          .forGetter(MobPowerUse::tellTicks),
                      Identifier.CODEC.fieldOf("tell_sound").forGetter(MobPowerUse::tellSound),
                      Codec.STRING.fieldOf("tell_clip").forGetter(MobPowerUse::tellClip),
                      Codec.BOOL.optionalFieldOf("dodge", false).forGetter(MobPowerUse::dodge),
                      MobUse.CODEC
                          .listOf()
                          .optionalFieldOf("uses", List.of())
                          .forGetter(MobPowerUse::declaredUses))
                  .apply(instance, MobPowerUse::new));

  public MobPowerUse {
    uses = List.copyOf(uses);
  }

  public MobPowerUse(
      final float minRange,
      final float maxRange,
      final int tellTicks,
      final Identifier tellSound,
      final String tellClip,
      final boolean dodge) {
    this(minRange, maxRange, tellTicks, tellSound, tellClip, dodge, List.of());
  }

  private List<MobUse> declaredUses() {
    return uses;
  }

  @Override
  public List<MobUse> uses() {
    return uses.isEmpty()
        ? List.of(
            MobUse.inRange(Intent.FIGHT, minRange, maxRange),
            MobUse.inRange(Intent.CLOSE_IN, minRange, maxRange))
        : uses;
  }
}
