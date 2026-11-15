package com.grahambartley.nightborn.clothing;

import java.util.Locale;

public enum ClothingCut {
  MALE,
  FEMALE;

  public static ClothingCut fitting(final boolean slimBody) {
    return slimBody ? FEMALE : MALE;
  }

  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }
}
