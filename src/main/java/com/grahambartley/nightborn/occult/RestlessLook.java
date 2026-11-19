package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.mob.look.Weighted;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.util.Identifier;

public record RestlessLook(VampireRig rig, Identifier texture, int weight) {

  private static final Codec<VampireRig> CUT =
      VampireRig.CODEC.validate(
          rig ->
              VampireRig.COMMON.contains(rig)
                  ? DataResult.success(rig)
                  : DataResult.error(() -> "The restless dead wear a male or female cut: " + rig));

  public static final Codec<RestlessLook> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      CUT.fieldOf("rig").forGetter(RestlessLook::rig),
                      Identifier.CODEC.fieldOf("texture").forGetter(RestlessLook::texture),
                      Codec.intRange(1, Weighted.MAX_WEIGHT)
                          .optionalFieldOf("weight", 1)
                          .forGetter(RestlessLook::weight))
                  .apply(instance, RestlessLook::new));

  public static Optional<Identifier> pick(
      final Map<Identifier, RestlessLook> looks, final double roll) {
    return Weighted.pick(
            new TreeMap<>(looks).entrySet().stream().toList(),
            entry -> entry.getValue().weight(),
            roll)
        .map(Map.Entry::getKey);
  }

  public static Optional<RestlessLook> worn(
      final Map<Identifier, RestlessLook> looks, final String id) {
    return Optional.ofNullable(Identifier.tryParse(id))
        .map(looks::get)
        .or(() -> new TreeMap<>(looks).values().stream().findFirst());
  }
}
