package com.grahambartley.nightborn.moment;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record MomentState(
    Moment moment, long startedAt, float yaw, int cameraTicks, int cardTicks) {

  public enum Phase {
    CAMERA,
    CARD,
    OVER
  }

  public static final PacketCodec<ByteBuf, MomentState> PACKET_CODEC =
      PacketCodec.tuple(
          Moment.PACKET_CODEC,
          MomentState::moment,
          PacketCodecs.VAR_LONG,
          MomentState::startedAt,
          PacketCodecs.FLOAT,
          MomentState::yaw,
          PacketCodecs.VAR_INT,
          MomentState::cameraTicks,
          PacketCodecs.VAR_INT,
          MomentState::cardTicks,
          MomentState::new);

  public int totalTicks() {
    return cameraTicks + cardTicks;
  }

  public float elapsed(final long now, final float tickDelta) {
    return Math.max(0.0F, now - startedAt + tickDelta);
  }

  public Phase phase(final float elapsed) {
    if (elapsed < cameraTicks) {
      return Phase.CAMERA;
    }
    return elapsed < totalTicks() ? Phase.CARD : Phase.OVER;
  }

  public float cameraProgress(final float elapsed) {
    return cameraTicks <= 0 ? 1.0F : Math.min(1.0F, elapsed / cameraTicks);
  }

  public float cardProgress(final float elapsed) {
    return cardTicks <= 0 ? 1.0F : Math.clamp((elapsed - cameraTicks) / cardTicks, 0.0F, 1.0F);
  }
}
