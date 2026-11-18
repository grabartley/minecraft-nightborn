package com.grahambartley.nightborn.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record ChannelAttack(
    int ticks,
    int everyTicks,
    float heal,
    float breakDamage,
    Optional<Identifier> held,
    Optional<ChannelBar> bar) {

  public static final Codec<ChannelAttack> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(1, 1200).fieldOf("ticks").forGetter(ChannelAttack::ticks),
                      Codec.intRange(1, 200)
                          .optionalFieldOf("every_ticks", 10)
                          .forGetter(ChannelAttack::everyTicks),
                      Codec.floatRange(0.0F, 1000.0F)
                          .optionalFieldOf("heal", 0.0F)
                          .forGetter(ChannelAttack::heal),
                      Codec.floatRange(0.0F, 1000.0F)
                          .optionalFieldOf("break_damage", 0.0F)
                          .forGetter(ChannelAttack::breakDamage),
                      Identifier.CODEC.optionalFieldOf("held").forGetter(ChannelAttack::held),
                      ChannelBar.CODEC.optionalFieldOf("bar").forGetter(ChannelAttack::bar))
                  .apply(instance, ChannelAttack::new));

  public enum Step {
    HOLD,
    MEND,
    BROKEN,
    DONE
  }

  public Step step(final int tick, final float taken) {
    if (breakDamage > 0.0F && taken >= breakDamage) {
      return Step.BROKEN;
    }
    if (tick >= ticks) {
      return Step.DONE;
    }
    return heal > 0.0F && tick > 0 && tick % everyTicks == 0 ? Step.MEND : Step.HOLD;
  }
}
