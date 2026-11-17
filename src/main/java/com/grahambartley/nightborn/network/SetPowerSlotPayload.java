package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.skill.PowerLayout;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetPowerSlotPayload(int slot, Optional<Identifier> skill) implements CustomPayload {

  public static final CustomPayload.Id<SetPowerSlotPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "set_power_slot"));

  public static final PacketCodec<ByteBuf, SetPowerSlotPayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          SetPowerSlotPayload::slot,
          PacketCodecs.optional(
              PacketCodecs.string(BuySkillPayload.MAX_ID_LENGTH)
                  .xmap(BuySkillPayload::parse, Identifier::toString)),
          SetPowerSlotPayload::skill,
          SetPowerSlotPayload::new);

  public SetPowerSlotPayload {
    if (!PowerLayout.has(slot)) {
      throw new DecoderException("Power slot " + slot + " is outside the power bar");
    }
  }

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
