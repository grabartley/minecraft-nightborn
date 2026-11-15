package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;

public record SkillAttribute(
    RegistryEntry<EntityAttribute> attribute,
    double amount,
    EntityAttributeModifier.Operation operation) {

  public static final Codec<SkillAttribute> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      EntityAttribute.CODEC
                          .fieldOf("attribute")
                          .forGetter(SkillAttribute::attribute),
                      Codec.DOUBLE.fieldOf("amount").forGetter(SkillAttribute::amount),
                      EntityAttributeModifier.Operation.CODEC
                          .optionalFieldOf("operation", EntityAttributeModifier.Operation.ADD_VALUE)
                          .forGetter(SkillAttribute::operation))
                  .apply(instance, SkillAttribute::new));
}
