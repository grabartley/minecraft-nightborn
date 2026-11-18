package com.grahambartley.nightborn.challenge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public record Feat(
    Text name,
    Text description,
    Text title,
    Side side,
    DeedGoal goal,
    Optional<DeedMatch> spoiledBy,
    Identifier trophy,
    Optional<Identifier> reward) {

  public static final Codec<Feat> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      TextCodecs.CODEC.fieldOf("name").forGetter(Feat::name),
                      TextCodecs.CODEC.fieldOf("description").forGetter(Feat::description),
                      TextCodecs.CODEC.fieldOf("title").forGetter(Feat::title),
                      Side.CODEC.fieldOf("side").forGetter(Feat::side),
                      DeedGoal.MAP_CODEC.forGetter(Feat::goal),
                      DeedMatch.CODEC.optionalFieldOf("spoiled_by").forGetter(Feat::spoiledBy),
                      Identifier.CODEC.fieldOf("trophy").forGetter(Feat::trophy),
                      Identifier.CODEC.optionalFieldOf("reward").forGetter(Feat::reward))
                  .apply(instance, Feat::new));
}
