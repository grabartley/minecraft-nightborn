package com.grahambartley.nightborn.structure;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.MathHelper;

public record FogZone(double x, double y, double z, double reach) {

  public static final int MARGIN = 24;

  public static final PacketCodec<ByteBuf, FogZone> PACKET_CODEC =
      PacketCodec.tuple(
          PacketCodecs.DOUBLE,
          FogZone::x,
          PacketCodecs.DOUBLE,
          FogZone::y,
          PacketCodecs.DOUBLE,
          FogZone::z,
          PacketCodecs.DOUBLE,
          FogZone::reach,
          FogZone::new);

  public static FogZone around(final BlockBox heart, final BlockBox whole) {
    final double x = (heart.getMinX() + heart.getMaxX() + 1) / 2.0;
    final double y = (heart.getMinY() + heart.getMaxY() + 1) / 2.0;
    final double z = (heart.getMinZ() + heart.getMaxZ() + 1) / 2.0;
    final double dx = Math.max(Math.abs(whole.getMinX() - x), Math.abs(whole.getMaxX() + 1 - x));
    final double dy = Math.max(Math.abs(whole.getMinY() - y), Math.abs(whole.getMaxY() + 1 - y));
    final double dz = Math.max(Math.abs(whole.getMinZ() - z), Math.abs(whole.getMaxZ() + 1 - z));
    return new FogZone(x, y, z, Math.sqrt(dx * dx + dy * dy + dz * dz) + MARGIN);
  }

  public double distanceTo(final double px, final double py, final double pz) {
    final double dx = px - x;
    final double dy = py - y;
    final double dz = pz - z;
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public boolean contains(final double px, final double py, final double pz) {
    return distanceTo(px, py, pz) < reach;
  }

  public float closeness(final double px, final double py, final double pz) {
    return (float) MathHelper.clamp(1.0 - distanceTo(px, py, pz) / reach, 0.0, 1.0);
  }
}
