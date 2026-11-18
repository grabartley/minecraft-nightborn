package com.grahambartley.nightborn.network;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.boss.Arena;
import com.grahambartley.nightborn.boss.ArenaWall;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BossArenaPayload(
    int boss,
    Optional<Arena> arena,
    boolean fighting,
    Optional<Identifier> music,
    Optional<ArenaWall> wall)
    implements CustomPayload {

  public static final CustomPayload.Id<BossArenaPayload> ID =
      new CustomPayload.Id<>(Identifier.of(Nightborn.MOD_ID, "boss_arena"));

  public static final PacketCodec<ByteBuf, BossArenaPayload> CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          BossArenaPayload::boss,
          PacketCodecs.optional(Arena.PACKET_CODEC),
          BossArenaPayload::arena,
          PacketCodecs.BOOL,
          BossArenaPayload::fighting,
          PacketCodecs.optional(Identifier.PACKET_CODEC),
          BossArenaPayload::music,
          PacketCodecs.optional(ArenaWall.PACKET_CODEC),
          BossArenaPayload::wall,
          BossArenaPayload::new);

  public static BossArenaPayload left(final int boss) {
    return new BossArenaPayload(boss, Optional.empty(), false, Optional.empty(), Optional.empty());
  }

  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
