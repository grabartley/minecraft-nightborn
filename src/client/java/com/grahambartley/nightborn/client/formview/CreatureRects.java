package com.grahambartley.nightborn.client.formview;

import java.util.List;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class CreatureRects {

  public static final int CAPACITY = 16;

  static final float BEHIND = 0.05F;

  private CreatureRects() {}

  public record Packed(float[] rects, float[] depths) {}

  public static Packed pack(final List<Box> boxes, final Matrix4f viewProjection) {
    final float[] rects = new float[CAPACITY * 4];
    final float[] depths = new float[CAPACITY * 2];
    final int count = Math.min(CAPACITY, boxes.size());
    for (int i = 0; i < count; i++) {
      final Box box = boxes.get(i);
      final float[] rect = screenRect(box, viewProjection);
      System.arraycopy(rect, 0, rects, i * 4, 4);
      depths[i * 2] = (float) nearest(box);
      depths[i * 2 + 1] = (float) farthest(box);
    }
    return new Packed(rects, depths);
  }

  static float[] screenRect(final Box box, final Matrix4f viewProjection) {
    float minU = Float.POSITIVE_INFINITY;
    float minV = Float.POSITIVE_INFINITY;
    float maxU = Float.NEGATIVE_INFINITY;
    float maxV = Float.NEGATIVE_INFINITY;
    for (int corner = 0; corner < 8; corner++) {
      final Vector4f clip =
          viewProjection.transform(
              new Vector4f(
                  (float) ((corner & 1) == 0 ? box.minX : box.maxX),
                  (float) ((corner & 2) == 0 ? box.minY : box.maxY),
                  (float) ((corner & 4) == 0 ? box.minZ : box.maxZ),
                  1.0F));
      if (clip.w < BEHIND) {
        return new float[] {0.0F, 0.0F, 1.0F, 1.0F};
      }
      final float u = (clip.x / clip.w + 1.0F) * 0.5F;
      final float v = (clip.y / clip.w + 1.0F) * 0.5F;
      minU = Math.min(minU, u);
      minV = Math.min(minV, v);
      maxU = Math.max(maxU, u);
      maxV = Math.max(maxV, v);
    }
    return new float[] {minU, minV, maxU, maxV};
  }

  static double nearest(final Box box) {
    final double x = Math.clamp(0.0, box.minX, box.maxX);
    final double y = Math.clamp(0.0, box.minY, box.maxY);
    final double z = Math.clamp(0.0, box.minZ, box.maxZ);
    return Math.sqrt(x * x + y * y + z * z);
  }

  static double farthest(final Box box) {
    final double x = Math.max(Math.abs(box.minX), Math.abs(box.maxX));
    final double y = Math.max(Math.abs(box.minY), Math.abs(box.maxY));
    final double z = Math.max(Math.abs(box.minZ), Math.abs(box.maxZ));
    return Math.sqrt(x * x + y * y + z * z);
  }
}
