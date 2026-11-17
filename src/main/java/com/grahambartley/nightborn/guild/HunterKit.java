package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.mob.look.Weighted;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.util.Identifier;

public record HunterKit(
    Identifier weapon,
    Optional<Identifier> bolt,
    Optional<Identifier> crucifix,
    boolean holyWater,
    int weight) {

  public static final Codec<HunterKit> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("weapon").forGetter(HunterKit::weapon),
                      Identifier.CODEC.optionalFieldOf("bolt").forGetter(HunterKit::bolt),
                      Identifier.CODEC.optionalFieldOf("crucifix").forGetter(HunterKit::crucifix),
                      Codec.BOOL
                          .optionalFieldOf("holy_water", false)
                          .forGetter(HunterKit::holyWater),
                      Codec.intRange(1, Weighted.MAX_WEIGHT)
                          .optionalFieldOf("weight", 1)
                          .forGetter(HunterKit::weight))
                  .apply(instance, HunterKit::new));

  public boolean ranged() {
    return bolt.isPresent();
  }

  public static Optional<Identifier> pick(
      final Map<Identifier, HunterKit> kits, final double roll) {
    return Weighted.pick(
            new TreeMap<>(kits).entrySet().stream().toList(),
            entry -> entry.getValue().weight(),
            roll)
        .map(Map.Entry::getKey);
  }
}
