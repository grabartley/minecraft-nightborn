package com.grahambartley.nightborn.structure;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public record FogZone(
    double x, double y, double z, int minX, int minY, int minZ, int endX, int endY, int endZ) {

  public static final int MARGIN = 24;

  public static final PacketCodec<ByteBuf, FogZone> PACKET_CODEC =
      PacketCodec.tuple(
          PacketCodecs.DOUBLE,
          FogZone::x,
          PacketCodecs.DOUBLE,
          FogZone::y,
          PacketCodecs.DOUBLE,
          FogZone::z,
          BlockPos.PACKET_CODEC,
          FogZone::min,
          BlockPos.PACKET_CODEC,
          FogZone::end,
          FogZone::new);

  private FogZone(
      final double x, final double y, final double z, final BlockPos min, final BlockPos end) {
    this(x, y, z, min.getX(), min.getY(), min.getZ(), end.getX(), end.getY(), end.getZ());
  }

  public static FogZone around(final BlockBox heart, final BlockBox whole) {
    return new FogZone(
        (heart.getMinX() + heart.getMaxX() + 1) / 2.0,
        (heart.getMinY() + heart.getMaxY() + 1) / 2.0,
        (heart.getMinZ() + heart.getMaxZ() + 1) / 2.0,
        whole.getMinX(),
        whole.getMinY(),
        whole.getMinZ(),
        whole.getMaxX() + 1,
        whole.getMaxY() + 1,
        whole.getMaxZ() + 1);
  }

  private BlockPos min() {
    return new BlockPos(minX, minY, minZ);
  }

  private BlockPos end() {
    return new BlockPos(endX, endY, endZ);
  }

  public double reach() {
    final double dx = Math.max(x - minX, endX - x);
    final double dy = Math.max(y - minY, endY - y);
    final double dz = Math.max(z - minZ, endZ - z);
    return Math.sqrt(dx * dx + dy * dy + dz * dz) + MARGIN;
  }

  public double outside(final double px, final double py, final double pz) {
    final double dx = Math.max(0.0, Math.max(minX - px, px - endX));
    final double dy = Math.max(0.0, Math.max(minY - py, py - endY));
    final double dz = Math.max(0.0, Math.max(minZ - pz, pz - endZ));
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public float closeness(final double px, final double py, final double pz) {
    final double dx = px - x;
    final double dy = py - y;
    final double dz = pz - z;
    final double towardsHeart =
        MathHelper.clamp(1.0 - Math.sqrt(dx * dx + dy * dy + dz * dz) / reach(), 0.0, 1.0);
    final double withinEdge = MathHelper.clamp(1.0 - outside(px, py, pz) / MARGIN, 0.0, 1.0);
    return (float) (towardsHeart * withinEdge);
  }

  public boolean encloses(final double px, final double py, final double pz) {
    return outside(px, py, pz) == 0.0;
  }

  public boolean contains(final double px, final double py, final double pz) {
    return closeness(px, py, pz) > 0.0F;
  }
}
