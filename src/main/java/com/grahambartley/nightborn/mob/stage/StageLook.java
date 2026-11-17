package com.grahambartley.nightborn.mob.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;

public record StageLook(
    Optional<String> idle, Optional<Identifier> marks, Optional<TextColor> nameColor) {

  public Optional<Identifier> marksTexture(final String rig) {
    return marks.map(path -> path.withSuffixedPath("_" + rig + ".png"));
  }

  public static final StageLook PLAIN =
      new StageLook(Optional.empty(), Optional.empty(), Optional.empty());

  public static final Codec<StageLook> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.STRING.optionalFieldOf("idle").forGetter(StageLook::idle),
                      Identifier.CODEC.optionalFieldOf("marks").forGetter(StageLook::marks),
                      TextColor.CODEC.optionalFieldOf("name_color").forGetter(StageLook::nameColor))
                  .apply(instance, StageLook::new));
}
