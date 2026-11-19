package com.grahambartley.nightborn.seeker;

import java.util.Collection;

public final class MarkShare {

  private MarkShare() {}

  public static double of(
      final boolean byHunter, final boolean byAlly, final double own, final double allied) {
    if (byHunter) {
      return Math.max(0.0, own);
    }
    return byAlly ? Math.max(0.0, allied) : 0.0;
  }

  public static double total(final Collection<Marker> markers) {
    final double own = markers.stream().filter(Marker::byHunter).mapToDouble(Marker::share).sum();
    final double allied =
        markers.stream()
            .filter(marker -> !marker.byHunter())
            .mapToDouble(Marker::share)
            .max()
            .orElse(0.0);
    return own + allied;
  }

  public static float raise(final float amount, final double share) {
    return (float) (amount * (1.0 + Math.max(0.0, share)));
  }
}
