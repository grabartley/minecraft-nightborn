package com.grahambartley.nightborn.client.formview;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RippleField {

  public static final int CAPACITY = 32;

  private final List<Ripple> live = new ArrayList<>();

  public void add(final Ripple ripple, final double nowSeconds, final float lifeSeconds) {
    expire(nowSeconds, lifeSeconds);
    if (live.size() < CAPACITY) {
      live.add(ripple);
      return;
    }
    final Ripple faintest =
        live.stream()
            .min(Comparator.comparingDouble(each -> each.alpha(nowSeconds, lifeSeconds)))
            .orElseThrow();
    if (faintest.alpha(nowSeconds, lifeSeconds) < ripple.alpha(nowSeconds, lifeSeconds)) {
      live.set(live.indexOf(faintest), ripple);
    }
  }

  public void expire(final double nowSeconds, final float lifeSeconds) {
    live.removeIf(ripple -> ripple.over(nowSeconds, lifeSeconds));
  }

  public List<Ripple> live() {
    return List.copyOf(live);
  }

  public void clear() {
    live.clear();
  }
}
