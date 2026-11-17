package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.Nightborn;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public record GiftModifier(
    RegistryEntry<EntityAttribute> attribute,
    double amount,
    EntityAttributeModifier.Operation operation) {

  public static final Codec<GiftModifier> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      EntityAttribute.CODEC.fieldOf("attribute").forGetter(GiftModifier::attribute),
                      Codec.DOUBLE.fieldOf("amount").forGetter(GiftModifier::amount),
                      EntityAttributeModifier.Operation.CODEC
                          .fieldOf("operation")
                          .forGetter(GiftModifier::operation))
                  .apply(instance, GiftModifier::new));

  public static Identifier id(final int index) {
    return Identifier.of(Nightborn.MOD_ID, "blood_gift/" + index);
  }

  public EntityAttributeModifier modifier(final int index, final float strength) {
    return new EntityAttributeModifier(id(index), amount * strength, operation);
  }
}
