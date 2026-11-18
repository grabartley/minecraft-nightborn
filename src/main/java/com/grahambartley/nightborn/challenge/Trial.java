package com.grahambartley.nightborn.challenge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public record Trial(
    Text name,
    Text description,
    Side side,
    DeedGoal goal,
    Optional<Identifier> gate,
    List<Identifier> skills,
    TrialSize size,
    Optional<Identifier> reward) {

  public static final Codec<Trial> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      TextCodecs.CODEC.fieldOf("name").forGetter(Trial::name),
                      TextCodecs.CODEC.fieldOf("description").forGetter(Trial::description),
                      Side.CODEC.fieldOf("side").forGetter(Trial::side),
                      DeedGoal.MAP_CODEC.forGetter(Trial::goal),
                      Identifier.CODEC.optionalFieldOf("gate").forGetter(Trial::gate),
                      Identifier.CODEC
                          .listOf()
                          .optionalFieldOf("skills", List.of())
                          .forGetter(Trial::skills),
                      TrialSize.CODEC.fieldOf("size").forGetter(Trial::size),
                      Identifier.CODEC.optionalFieldOf("reward").forGetter(Trial::reward))
                  .apply(instance, Trial::new));

  public Trial {
    skills = List.copyOf(skills);
  }
}
