package com.grahambartley.nightborn.dismember;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Arm;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public enum BodyPart implements StringIdentifiable {
  HEAD("head", false),
  RIGHT_ARM("right_arm", true),
  LEFT_ARM("left_arm", true),
  RIGHT_LEG("right_leg", true),
  LEFT_LEG("left_leg", true);

  public static final Codec<BodyPart> CODEC = StringIdentifiable.createCodec(BodyPart::values);

  private static final IntFunction<BodyPart> BY_INDEX =
      ValueLists.createIdToValueFunction(
          BodyPart::ordinal, values(), ValueLists.OutOfBoundsHandling.ZERO);

  public static final PacketCodec<ByteBuf, BodyPart> PACKET_CODEC =
      PacketCodecs.indexed(BY_INDEX, BodyPart::ordinal);

  private final String name;
  private final boolean limb;

  BodyPart(final String name, final boolean limb) {
    this.name = name;
    this.limb = limb;
  }

  @Override
  public String asString() {
    return name;
  }

  public boolean limb() {
    return limb;
  }

  public static List<BodyPart> limbs() {
    return Arrays.stream(values()).filter(BodyPart::limb).toList();
  }

  public static Optional<BodyPart> named(final String name) {
    return Arrays.stream(values()).filter(part -> part.asString().equals(name)).findFirst();
  }

  public static BodyPart arm(final Arm arm) {
    return arm == Arm.RIGHT ? RIGHT_ARM : LEFT_ARM;
  }

  public static BodyPart arm(final boolean right) {
    return right ? RIGHT_ARM : LEFT_ARM;
  }

  public static BodyPart leg(final boolean right) {
    return right ? RIGHT_LEG : LEFT_LEG;
  }
}
