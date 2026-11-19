package com.grahambartley.nightborn.trap;

import java.util.List;
import net.minecraft.util.Identifier;

public final class TrapGifts {

  private TrapGifts() {}

  public static List<TrapGift> forPower(final List<TrapGift> gifts, final Identifier power) {
    return gifts.stream().filter(gift -> gift.covers(power)).toList();
  }

  public static int armTicks(final int base, final List<TrapGift> gifts) {
    final double cut = gifts.stream().mapToDouble(TrapGift::armCut).sum();
    return (int) Math.round(base * Math.max(0.0, 1.0 - cut));
  }

  public static float seenWithin(final float base, final List<TrapGift> gifts) {
    return gifts.stream().flatMap(gift -> gift.seenWithin().stream()).reduce(base, Math::min);
  }

  public static int limit(final int base, final List<TrapGift> gifts) {
    return base + gifts.stream().mapToInt(TrapGift::extraLimit).sum();
  }

  public static float extraDamage(final List<TrapGift> gifts) {
    return (float) gifts.stream().mapToDouble(TrapGift::extraDamage).sum();
  }

  public static boolean senses(final List<TrapGift> gifts) {
    return gifts.stream().anyMatch(TrapGift::senses);
  }

  public static double pinnedDamage(final List<TrapGift> gifts) {
    return gifts.stream().mapToDouble(TrapGift::pinnedDamage).sum();
  }
}
