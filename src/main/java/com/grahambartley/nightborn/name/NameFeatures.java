package com.grahambartley.nightborn.name;

public final class NameFeatures {

  private NameFeatures() {}

  public static void register() {
    NamePools.register();
    ServantNaming.register();
  }
}
