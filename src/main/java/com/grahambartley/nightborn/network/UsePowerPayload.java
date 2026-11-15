package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.skill.PowerBar;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UsePowerPayload(int slot) implements CustomPayload {

  public static final CustomPayload.Id<UsePowerPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "use_power"));

  public static final PacketCodec<ByteBuf, UsePowerPayload> CODEC =
      PacketCodecs.VAR_INT.xmap(UsePowerPayload::new, UsePowerPayload::slot);

  public UsePowerPayload {
    if (slot < 0 || slot >= PowerBar.SLOTS) {
      throw new DecoderException("Power slot " + slot + " is outside the power bar");
    }
  }

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
