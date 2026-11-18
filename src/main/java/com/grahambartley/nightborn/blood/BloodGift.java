package com.grahambartley.nightborn.blood;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;

public record BloodGift(
    RegistryEntryList<EntityType<?>> prey, String name, int seconds, List<GiftModifier> modifiers) {

  public static final float FRESH = 1.0F;
  public static final float BOTTLED = 0.5F;
  public static final int MOST_MODIFIERS = 8;
  static final int MOST_SECONDS = 3600;

  public static final Codec<BloodGift> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      RegistryCodecs.entryList(RegistryKeys.ENTITY_TYPE)
                          .fieldOf("prey")
                          .forGetter(BloodGift::prey),
                      Codec.STRING.fieldOf("name").forGetter(BloodGift::name),
                      Codec.intRange(1, MOST_SECONDS)
                          .fieldOf("seconds")
                          .forGetter(BloodGift::seconds),
                      Codec.list(GiftModifier.CODEC, 1, MOST_MODIFIERS)
                          .fieldOf("modifiers")
                          .forGetter(BloodGift::modifiers))
                  .apply(instance, BloodGift::new));

  public BloodGift {
    modifiers = List.copyOf(modifiers);
  }

  public boolean feeds(final EntityType<?> type) {
    return prey.contains(type.getRegistryEntry());
  }

  public int ticks() {
    return seconds * 20;
  }

  public static float strength(final boolean fresh) {
    return fresh ? FRESH : BOTTLED;
  }
}
