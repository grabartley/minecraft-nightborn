package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.BloodSettings;
import com.grahambartley.nightborn.config.IntSetting;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.entity.EntityType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public enum BloodQuality implements StringIdentifiable {
  ANIMAL("animal", BloodSettings.ANIMAL_BLOOD, Formatting.GRAY),
  VILLAGER("villager", BloodSettings.VILLAGER_BLOOD, Formatting.GOLD),
  PLAYER("player", BloodSettings.PLAYER_BLOOD, Formatting.DARK_RED);

  public static final Codec<BloodQuality> CODEC =
      StringIdentifiable.createCodec(BloodQuality::values);

  private static final IntFunction<BloodQuality> BY_INDEX =
      ValueLists.createIdToValueFunction(
          BloodQuality::ordinal, values(), ValueLists.OutOfBoundsHandling.ZERO);

  public static final PacketCodec<ByteBuf, BloodQuality> PACKET_CODEC =
      PacketCodecs.indexed(BY_INDEX, BloodQuality::ordinal);

  private final String id;
  private final IntSetting worth;
  private final Formatting colour;
  private final TagKey<EntityType<?>> tag;

  BloodQuality(final String id, final IntSetting worth, final Formatting colour) {
    this.id = id;
    this.worth = worth;
    this.colour = colour;
    this.tag = TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Nightborn.MOD_ID, "blood/" + id));
  }

  public static Optional<BloodQuality> of(final EntityType<?> type) {
    for (int i = values().length - 1; i >= 0; i--) {
      if (type.isIn(values()[i].tag)) {
        return Optional.of(values()[i]);
      }
    }
    return Optional.empty();
  }

  public int worthPercent(final NightbornConfig config) {
    return worth.get(config);
  }

  public TagKey<EntityType<?>> tag() {
    return tag;
  }

  public Formatting colour() {
    return colour;
  }

  public String translationKey() {
    return "nightborn.blood_quality." + id;
  }

  @Override
  public String asString() {
    return id;
  }
}
