package com.grahambartley.nightborn.batform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record WingedState(
    long since,
    long until,
    float damageTaken,
    float scale,
    Optional<Identifier> rig,
    List<String> forcedBy) {

  public static final Codec<WingedState> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.LONG.optionalFieldOf("since", 0L).forGetter(WingedState::since),
                      Codec.LONG.fieldOf("until").forGetter(WingedState::until),
                      Codec.FLOAT.fieldOf("damage_taken").forGetter(WingedState::damageTaken),
                      Codec.FLOAT.fieldOf("scale").forGetter(WingedState::scale),
                      Identifier.CODEC.optionalFieldOf("rig").forGetter(WingedState::rig),
                      Codec.STRING
                          .listOf()
                          .optionalFieldOf("forced_by", List.of())
                          .forGetter(WingedState::forcedBy))
                  .apply(instance, WingedState::new));

  public static final PacketCodec<ByteBuf, WingedState> PACKET_CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_LONG,
          WingedState::since,
          PacketCodecs.VAR_LONG,
          WingedState::until,
          PacketCodecs.FLOAT,
          WingedState::damageTaken,
          PacketCodecs.FLOAT,
          WingedState::scale,
          PacketCodecs.optional(Identifier.PACKET_CODEC),
          WingedState::rig,
          PacketCodecs.STRING.collect(PacketCodecs.toList()),
          WingedState::forcedBy,
          WingedState::new);

  public WingedState {
    forcedBy = List.copyOf(forcedBy);
  }

  public static WingedState of(final long now, final WingedFormPower power) {
    return new WingedState(
        now,
        now + power.durationTicks(),
        power.damageTaken(),
        power.scale(),
        power.rig(),
        power.forcedBy());
  }

  public boolean over(final long now) {
    return now >= until;
  }
}
