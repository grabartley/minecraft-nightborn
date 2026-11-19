package com.grahambartley.nightborn.faith;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

public record PrayerGift(List<RegistryEntry<StatusEffect>> clears) {

  public static final Codec<PrayerGift> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Registries.STATUS_EFFECT
                          .getEntryCodec()
                          .listOf()
                          .fieldOf("clears")
                          .forGetter(PrayerGift::clears))
                  .apply(instance, PrayerGift::new));

  public PrayerGift {
    clears = List.copyOf(clears);
  }

  public static List<RegistryEntry<StatusEffect>> cleared(final Collection<PrayerGift> gifts) {
    return gifts.stream().flatMap(gift -> gift.clears().stream()).distinct().toList();
  }
}
