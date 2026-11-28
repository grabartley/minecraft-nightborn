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
    int weight,
    float damage,
    float rankDamage) {

  public static final float MOST_DAMAGE = 64.0F;

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
                      Codec.intRange(0, Weighted.MAX_WEIGHT)
                          .optionalFieldOf("weight", 1)
                          .forGetter(HunterKit::weight),
                      Codec.floatRange(0.0F, MOST_DAMAGE)
                          .optionalFieldOf("damage", 0.0F)
                          .forGetter(HunterKit::damage),
                      Codec.floatRange(-MOST_DAMAGE, MOST_DAMAGE)
                          .optionalFieldOf("rank_damage", 0.0F)
                          .forGetter(HunterKit::rankDamage))
                  .apply(instance, HunterKit::new));

  public float damageAt(final int rank) {
    return Math.max(0.0F, damage + rankDamage * rank);
  }

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
