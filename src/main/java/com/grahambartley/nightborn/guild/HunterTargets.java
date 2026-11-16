package com.grahambartley.nightborn.guild;

public final class HunterTargets {

  private HunterTargets() {}

  public static boolean hunts(final boolean vampire, final boolean exempt) {
    return vampire && !exempt;
  }
}
