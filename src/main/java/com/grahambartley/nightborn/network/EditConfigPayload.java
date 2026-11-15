package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.NightbornConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EditConfigPayload(NightbornConfig config) implements CustomPayload {

  public static final CustomPayload.Id<EditConfigPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "edit_config"));

  public static final PacketCodec<ByteBuf, EditConfigPayload> CODEC =
      ConfigPacketCodec.CODEC.xmap(EditConfigPayload::new, EditConfigPayload::config);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
