package com.grahambartley.nightborn.boss;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public record Arena(BlockPos centre, int radius, int below, int above) {

  public static final int ABOVE = 16;
  public static final int BELOW = 6;
  public static final int SMALLEST = 4;
  public static final int ROOM_BELOW = 1;
  public static final int ROOM_ABOVE = 8;

  static final String CENTRE_KEY = "ArenaCentre";
  static final String RADIUS_KEY = "ArenaRadius";
  static final String BELOW_KEY = "ArenaBelow";
  static final String ABOVE_KEY = "ArenaAbove";

  public static final PacketCodec<ByteBuf, Arena> PACKET_CODEC =
      PacketCodec.tuple(
          BlockPos.PACKET_CODEC,
          Arena::centre,
          PacketCodecs.VAR_INT,
          Arena::radius,
          PacketCodecs.VAR_INT,
          Arena::below,
          PacketCodecs.VAR_INT,
          Arena::above,
          Arena::new);

  public Arena {
    centre = centre.toImmutable();
    radius = Math.max(SMALLEST, radius);
    below = Math.max(0, below);
    above = Math.max(1, above);
  }

  public Arena(final BlockPos centre, final int radius) {
    this(centre, radius, BELOW, ABOVE);
  }

  public static Arena room(final BlockPos centre, final int radius) {
    return new Arena(centre, radius, ROOM_BELOW, ROOM_ABOVE);
  }

  public double midX() {
    return centre.getX() + 0.5;
  }

  public double midZ() {
    return centre.getZ() + 0.5;
  }

  public boolean holds(final double x, final double y, final double z) {
    final double dx = x - midX();
    final double dz = z - midZ();
    return dx * dx + dz * dz <= (double) radius * radius
        && y >= centre.getY() - below
        && y <= centre.getY() + above;
  }

  public Vec3d pullIn(final Vec3d pos, final double margin) {
    final double dx = pos.x - midX();
    final double dz = pos.z - midZ();
    final double reach = Math.max(0.0, radius - margin);
    final double out = Math.sqrt(dx * dx + dz * dz);
    final double scale = out <= reach || out < 1.0E-6 ? 1.0 : reach / out;
    final double y =
        Math.max(centre.getY() - (double) below, Math.min(centre.getY() + (double) above, pos.y));
    return new Vec3d(midX() + dx * scale, y, midZ() + dz * scale);
  }

  public void write(final NbtCompound nbt) {
    nbt.put(CENTRE_KEY, NbtHelper.fromBlockPos(centre));
    nbt.putInt(RADIUS_KEY, radius);
    nbt.putInt(BELOW_KEY, below);
    nbt.putInt(ABOVE_KEY, above);
  }

  public static Optional<Arena> read(final NbtCompound nbt) {
    return NbtHelper.toBlockPos(nbt, CENTRE_KEY)
        .map(
            centre ->
                new Arena(
                    centre, nbt.getInt(RADIUS_KEY), nbt.getInt(BELOW_KEY), nbt.getInt(ABOVE_KEY)));
  }
}
