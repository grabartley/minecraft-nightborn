package com.grahambartley.nightborn.trap;

import java.util.List;

public record TrapLoadout(int armTicks, float seenWithin, int limit, float extraDamage) {

  public static TrapLoadout of(final TrapPower power, final List<TrapGift> gifts) {
    return new TrapLoadout(
        TrapGifts.armTicks(power.armTicks(), gifts),
        TrapGifts.seenWithin(power.seenWithin(), gifts),
        TrapGifts.limit(power.limit(), gifts),
        TrapGifts.extraDamage(gifts));
  }
}
