package com.grahambartley.nightborn.guild.veteran;

public final class VeteranFeatures {

  private VeteranFeatures() {}

  public static void register() {
    VeteranData.register();
    Grounding.register();
  }
}
