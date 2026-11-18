package com.grahambartley.nightborn.house.crest;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Locale;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public enum CrestCharge implements StringIdentifiable {
  BAT,
  CHALICE,
  ROSE,
  TOWER,
  KEY,
  CRESCENT_MOON,
  RAVEN,
  SERPENT,
  CROWN,
  DAGGER,
  THORN_BRANCH,
  SKULL;

  public static final Codec<CrestCharge> CODEC =
      StringIdentifiable.createCodec(CrestCharge::values);

  private static final IntFunction<CrestCharge> BY_INDEX =
      ValueLists.createIdToValueFunction(
          CrestCharge::ordinal, values(), ValueLists.OutOfBoundsHandling.WRAP);

  public static final PacketCodec<ByteBuf, CrestCharge> PACKET_CODEC =
      PacketCodecs.indexed(BY_INDEX, CrestCharge::ordinal);

  @Override
  public String asString() {
    return name().toLowerCase(Locale.ROOT);
  }

  public String translationKey() {
    return "nightborn.crest.charge." + asString();
  }

  public CrestCharge step(final int by) {
    return BY_INDEX.apply(ordinal() + by);
  }
}
