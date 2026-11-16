package com.grahambartley.nightborn.familiar;

import java.util.Locale;

public enum ChestMark {
  FETCH,
  DELIVER,
  CLEARED;

  public String translationKey() {
    return "nightborn.familiar.mark." + name().toLowerCase(Locale.ROOT);
  }
}
