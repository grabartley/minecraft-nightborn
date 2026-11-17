package com.grahambartley.nightborn.boss;

public record ZoneShape(
    boolean line,
    double x,
    double z,
    double headingX,
    double headingZ,
    double radius,
    double length) {

  public static ZoneShape circle(final double x, final double z, final double radius) {
    return new ZoneShape(false, x, z, 0.0, 1.0, radius, 0.0);
  }

  public static ZoneShape line(
      final double fromX,
      final double fromZ,
      final double towardX,
      final double towardZ,
      final double halfWidth,
      final double length) {
    final double dx = towardX - fromX;
    final double dz = towardZ - fromZ;
    final double span = Math.sqrt(dx * dx + dz * dz);
    return span < 1.0E-6
        ? new ZoneShape(true, fromX, fromZ, 0.0, 1.0, halfWidth, length)
        : new ZoneShape(true, fromX, fromZ, dx / span, dz / span, halfWidth, length);
  }

  public boolean contains(final double px, final double pz) {
    final double dx = px - x;
    final double dz = pz - z;
    if (!line) {
      return dx * dx + dz * dz <= radius * radius;
    }
    final double along = dx * headingX + dz * headingZ;
    final double across = dx * -headingZ + dz * headingX;
    return along >= 0.0 && along <= length && Math.abs(across) <= radius;
  }

  public double midX() {
    return line ? x + headingX * length / 2.0 : x;
  }

  public double midZ() {
    return line ? z + headingZ * length / 2.0 : z;
  }

  public double endX() {
    return line ? x + headingX * length : x;
  }

  public double endZ() {
    return line ? z + headingZ * length : z;
  }

  public float yaw() {
    return (float) Math.toDegrees(Math.atan2(0.0 - headingX, headingZ));
  }
}
