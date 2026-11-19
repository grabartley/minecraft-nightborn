package com.grahambartley.nightborn.clothing;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.downed.DownedTargeting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public final class Noticing {

  private Noticing() {}

  public static double byHunter(final double noticeDistance, final double hunterSight) {
    return noticeDistance * hunterSight;
  }

  public static int range(final int range, final double noticeDistance) {
    return (int) Math.round(range * noticeDistance);
  }

  public static double byHunter(final LivingEntity wearer) {
    return byHunter(
        WornAttributes.value(wearer, WornAttributes.NOTICE_DISTANCE),
        WornAttributes.value(wearer, WornAttributes.HUNTER_SIGHT));
  }

  public static double towards(final LivingEntity wearer, final Entity observer) {
    return observer != null && observer.getType().isIn(DownedTargeting.HUNTERS)
        ? byHunter(wearer)
        : 1.0;
  }

  public static int byVillagers(final LivingEntity wearer, final int range) {
    return range(range, WornAttributes.value(wearer, WornAttributes.NOTICE_DISTANCE));
  }

  public static int feeding(final LivingEntity feeder, final int range) {
    return feeding(
        range,
        WornAttributes.value(feeder, WornAttributes.NOTICE_DISTANCE),
        GiftAttributes.value(feeder, GiftAttributes.FEEDING_NOTICE));
  }

  public static int feeding(
      final int range, final double noticeDistance, final double feedingNotice) {
    return range(range, noticeDistance * feedingNotice);
  }
}
