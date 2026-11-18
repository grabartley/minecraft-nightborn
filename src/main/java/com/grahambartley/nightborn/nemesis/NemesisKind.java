package com.grahambartley.nightborn.nemesis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record NemesisKind(
    Identifier entity,
    Identifier keepsake,
    Identifier epithets,
    float epithetChance,
    boolean ages,
    float boost,
    int maxBoosts,
    int experienceTimes,
    int companions,
    int fightEndSeconds,
    float sight,
    int spread,
    NemesisLines lines) {

  public static final Codec<NemesisKind> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("entity").forGetter(NemesisKind::entity),
                      Identifier.CODEC.fieldOf("keepsake").forGetter(NemesisKind::keepsake),
                      Identifier.CODEC.fieldOf("epithets").forGetter(NemesisKind::epithets),
                      Codec.floatRange(0.0F, 1.0F)
                          .optionalFieldOf("epithet_chance", 0.0F)
                          .forGetter(NemesisKind::epithetChance),
                      Codec.BOOL.optionalFieldOf("ages", false).forGetter(NemesisKind::ages),
                      Codec.floatRange(0.0F, 10.0F)
                          .optionalFieldOf("boost", 0.0F)
                          .forGetter(NemesisKind::boost),
                      Codec.intRange(0, 100)
                          .optionalFieldOf("max_boosts", 0)
                          .forGetter(NemesisKind::maxBoosts),
                      Codec.intRange(0, 1000)
                          .optionalFieldOf("experience_times", 1)
                          .forGetter(NemesisKind::experienceTimes),
                      Codec.intRange(0, 8)
                          .optionalFieldOf("companions", 0)
                          .forGetter(NemesisKind::companions),
                      Codec.intRange(1, 600)
                          .optionalFieldOf("fight_end_seconds", 30)
                          .forGetter(NemesisKind::fightEndSeconds),
                      Codec.floatRange(1.0F, 128.0F)
                          .optionalFieldOf("sight", 32.0F)
                          .forGetter(NemesisKind::sight),
                      Codec.intRange(0, 8)
                          .optionalFieldOf("spread", 2)
                          .forGetter(NemesisKind::spread),
                      NemesisLines.CODEC
                          .optionalFieldOf("lines", NemesisLines.NONE)
                          .forGetter(NemesisKind::lines))
                  .apply(instance, NemesisKind::new));

  public int fightEndTicks() {
    return fightEndSeconds * 20;
  }
}
