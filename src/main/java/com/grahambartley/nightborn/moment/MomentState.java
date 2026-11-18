package com.grahambartley.nightborn.moment;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3d;

public record MomentState(
    Moment moment, long startedAt, float yaw, Vec3d at, int cameraTicks, int cardTicks) {

  public enum Phase {
    CAMERA,
    CARD,
    OVER
  }

  static final double DRIFT = 1.0E-3;

  private static final PacketCodec<ByteBuf, Vec3d> POSITION_CODEC =
      PacketCodec.tuple(
          PacketCodecs.DOUBLE,
          Vec3d::getX,
          PacketCodecs.DOUBLE,
          Vec3d::getY,
          PacketCodecs.DOUBLE,
          Vec3d::getZ,
          Vec3d::new);

  public static final PacketCodec<ByteBuf, MomentState> PACKET_CODEC =
      PacketCodec.tuple(
          Moment.PACKET_CODEC,
          MomentState::moment,
          PacketCodecs.VAR_LONG,
          MomentState::startedAt,
          PacketCodecs.FLOAT,
          MomentState::yaw,
          POSITION_CODEC,
          MomentState::at,
          PacketCodecs.VAR_INT,
          MomentState::cameraTicks,
          PacketCodecs.VAR_INT,
          MomentState::cardTicks,
          MomentState::new);

  public boolean drifted(final Vec3d position) {
    return position.squaredDistanceTo(at) > DRIFT * DRIFT;
  }

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
