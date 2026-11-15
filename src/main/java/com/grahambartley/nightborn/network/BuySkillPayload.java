package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BuySkillPayload(Identifier skill) implements CustomPayload {

  public static final int MAX_ID_LENGTH = 128;

  public static final CustomPayload.Id<BuySkillPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "buy_skill"));

  public static final PacketCodec<ByteBuf, BuySkillPayload> CODEC =
      PacketCodecs.string(MAX_ID_LENGTH)
          .xmap(BuySkillPayload::parse, Identifier::toString)
          .xmap(BuySkillPayload::new, BuySkillPayload::skill);

  static Identifier parse(final String id) {
    final Identifier parsed = Identifier.tryParse(id);
    if (parsed == null) {
      throw new DecoderException("Skill id is not a valid identifier");
    }
    return parsed;
  }

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
