package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.blood.GiftModifier;
import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.mob.look.HexColor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

public record Accessory(
    Optional<Faction> worksFor, List<GiftModifier> gifts, List<AccessoryHold> holds, int color) {

  public static final int UNDYED = 0xFFFFFF;

  public static final Codec<Accessory> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Faction.CODEC.optionalFieldOf("works_for").forGetter(Accessory::worksFor),
                      GiftModifier.CODEC
                          .listOf()
                          .optionalFieldOf("gifts", List.of())
                          .forGetter(Accessory::gifts),
                      AccessoryHold.CODEC
                          .listOf()
                          .optionalFieldOf("holds", List.of())
                          .forGetter(Accessory::holds),
                      HexColor.CODEC.optionalFieldOf("color", UNDYED).forGetter(Accessory::color))
                  .apply(instance, Accessory::new));

  public boolean worksFor(final Faction wearer) {
    return this.worksFor.map(wearer::equals).orElse(true);
  }

  public boolean plain() {
    return this.gifts.isEmpty() && this.holds.isEmpty();
  }
}
