package com.grahambartley.nightborn.lore;

public final class LoreFeatures {

  private LoreFeatures() {}

  public static void register() {
    LoreBooks.register();
    LecternBookProcessor.register();
  }
}
