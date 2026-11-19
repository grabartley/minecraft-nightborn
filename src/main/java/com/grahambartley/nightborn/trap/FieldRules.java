package com.grahambartley.nightborn.trap;

public final class FieldRules {

  private FieldRules() {}

  public static boolean bars(final boolean listed, final boolean inside, final boolean reaches) {
    return listed && inside && reaches;
  }
}
