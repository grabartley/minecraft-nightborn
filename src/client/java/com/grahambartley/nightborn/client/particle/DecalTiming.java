package com.grahambartley.nightborn.client.particle;

public final class DecalTiming {

  public static final int LIMIT = 600;
  public static final int FRESH_TICKS = 600;
  public static final int DRYING_TICKS = 1200;
  public static final int STALE_TICKS = 40;

  public static final int BLOOD_LIFE = 5400;
  public static final int BLOOD_LIFE_SPREAD = 1800;
  public static final float POOL_LIFE_SHARE = 1.15F;
  public static final int BLOOD_FADE_TICKS = 600;

  public static final int VOMIT_LIFE = 70;
  public static final int VOMIT_LIFE_SPREAD = 40;
  public static final int VOMIT_FADE_TICKS = 25;

  private DecalTiming() {}

  public static int bloodLife(final float goreShare, final boolean pool, final float roll) {
    return life(BLOOD_LIFE, BLOOD_LIFE_SPREAD, goreShare * (pool ? POOL_LIFE_SHARE : 1.0F), roll);
  }

  public static int vomitLife(final float roll) {
    return life(VOMIT_LIFE, VOMIT_LIFE_SPREAD, 1.0F, roll);
  }

  static int life(final int least, final int spread, final float share, final float roll) {
    final float clamped = Math.min(1.0F, Math.max(0.0F, roll));
    return Math.max(0, Math.round((least + spread * clamped) * Math.max(0.0F, share)));
  }
}
