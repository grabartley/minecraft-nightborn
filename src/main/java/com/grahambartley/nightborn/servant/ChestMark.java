package com.grahambartley.nightborn.servant;

import java.util.Locale;

public enum ChestMark {
  FETCH,
  DELIVER,
  CLEARED;

  public String translationKey() {
    return "nightborn.servant.mark." + name().toLowerCase(Locale.ROOT);
  }
}
