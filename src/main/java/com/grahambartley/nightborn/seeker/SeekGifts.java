package com.grahambartley.nightborn.seeker;

import java.util.Collection;

public final class SeekGifts {

  private SeekGifts() {}

  public static int trackRange(final int base, final Collection<SeekGift> gifts) {
    return base + gifts.stream().mapToInt(SeekGift::trackRange).sum();
  }

  public static int trackTicks(final int base, final Collection<SeekGift> gifts) {
    return base + gifts.stream().mapToInt(SeekGift::trackTicks).sum();
  }

  public static boolean bloodTrails(final Collection<SeekGift> gifts) {
    return gifts.stream().anyMatch(SeekGift::bloodTrails);
  }

  public static float senseRange(final Collection<SeekGift> gifts) {
    return (float) gifts.stream().mapToDouble(SeekGift::senseRange).max().orElse(0.0);
  }

  public static float listenRange(final Collection<SeekGift> gifts) {
    return (float) gifts.stream().mapToDouble(SeekGift::listenRange).max().orElse(0.0);
  }

  public static int listenColour(final Collection<SeekGift> gifts) {
    return gifts.stream()
        .filter(gift -> gift.listenRange() > 0.0F)
        .mapToInt(SeekGift::listenColour)
        .findFirst()
        .orElse(0);
  }

  public static double alliedShare(final Collection<SeekGift> gifts) {
    return gifts.stream().mapToDouble(SeekGift::alliedShare).sum();
  }
}
