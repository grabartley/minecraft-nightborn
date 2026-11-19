package com.grahambartley.nightborn.hold;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record HoldState(Optional<HoldKind> kind, long until, long immuneUntil, boolean opening) {

  public static final HoldState NONE = new HoldState(Optional.empty(), 0L, 0L, false);

  public static final Codec<HoldState> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      HoldKind.CODEC.optionalFieldOf("kind").forGetter(HoldState::kind),
                      Codec.LONG.optionalFieldOf("until", 0L).forGetter(HoldState::until),
                      Codec.LONG
                          .optionalFieldOf("immune_until", 0L)
                          .forGetter(HoldState::immuneUntil),
                      Codec.BOOL.optionalFieldOf("opening", false).forGetter(HoldState::opening))
                  .apply(instance, HoldState::new));

  public static final PacketCodec<ByteBuf, HoldState> PACKET_CODEC =
      PacketCodec.tuple(
          PacketCodecs.optional(PacketCodecs.indexed(i -> HoldKind.values()[i], HoldKind::ordinal)),
          HoldState::kind,
          PacketCodecs.VAR_LONG,
          HoldState::until,
          PacketCodecs.VAR_LONG,
          HoldState::immuneUntil,
          PacketCodecs.BOOL,
          HoldState::opening,
          HoldState::new);

  public Optional<HoldKind> active(final long now) {
    return kind.filter(held -> now < until);
  }

  public boolean held(final long now) {
    return active(now).isPresent();
  }

  public boolean opened(final long now) {
    return opening && active(now).filter(held -> held == HoldKind.STAGGER).isPresent();
  }
}
