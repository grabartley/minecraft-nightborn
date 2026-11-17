package com.grahambartley.nightborn.mob.stage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.StringIdentifiable;

public record StageDeed(Act act, TagKey<EntityType<?>> victims, Text description) {

  public enum Act implements StringIdentifiable {
    DRAIN("drain"),
    END("end");

    public static final Codec<Act> CODEC = StringIdentifiable.createCodec(Act::values);

    private final String id;

    Act(final String id) {
      this.id = id;
    }

    @Override
    public String asString() {
      return id;
    }
  }

  public static final Codec<StageDeed> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Act.CODEC.fieldOf("act").forGetter(StageDeed::act),
                      TagKey.unprefixedCodec(RegistryKeys.ENTITY_TYPE)
                          .fieldOf("victims")
                          .forGetter(StageDeed::victims),
                      TextCodecs.CODEC.fieldOf("description").forGetter(StageDeed::description))
                  .apply(instance, StageDeed::new));

  public boolean doneBy(final Act done, final boolean victimListed) {
    return act == done && victimListed;
  }
}
