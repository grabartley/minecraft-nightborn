package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.mob.look.Weighted;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.util.Identifier;

public record HunterLook(HunterRig rig, Identifier texture, int weight) {

  public static final Identifier MISSING = Identifier.ofVanilla("missingno");

  public static final Codec<HunterLook> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      HunterRig.CODEC.fieldOf("rig").forGetter(HunterLook::rig),
                      Identifier.CODEC.fieldOf("texture").forGetter(HunterLook::texture),
                      Codec.intRange(1, Weighted.MAX_WEIGHT)
                          .optionalFieldOf("weight", 1)
                          .forGetter(HunterLook::weight))
                  .apply(instance, HunterLook::new));

  public static Optional<Identifier> pick(
      final Map<Identifier, HunterLook> looks, final HunterRig rig, final double roll) {
    return Weighted.pick(worn(looks, rig), entry -> entry.getValue().weight(), roll)
        .map(Map.Entry::getKey);
  }

  public static Identifier texture(
      final Map<Identifier, HunterLook> looks, final Optional<Identifier> id, final HunterRig rig) {
    return id.map(looks::get)
        .filter(look -> look.rig() == rig)
        .or(() -> worn(looks, rig).stream().findFirst().map(Map.Entry::getValue))
        .map(HunterLook::texture)
        .orElse(MISSING);
  }

  private static List<Map.Entry<Identifier, HunterLook>> worn(
      final Map<Identifier, HunterLook> looks, final HunterRig rig) {
    return new TreeMap<>(looks)
        .entrySet().stream().filter(entry -> entry.getValue().rig() == rig).toList();
  }
}
