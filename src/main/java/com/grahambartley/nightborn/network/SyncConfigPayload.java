package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.NightbornConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncConfigPayload(NightbornConfig config) implements CustomPayload {

  public static final CustomPayload.Id<SyncConfigPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "sync_config"));

  public static final PacketCodec<ByteBuf, SyncConfigPayload> CODEC =
      ConfigPacketCodec.CODEC.xmap(SyncConfigPayload::new, SyncConfigPayload::config);

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
