package com.grahambartley.nightborn.boss;

import java.util.Optional;

public record ShownForm(String idle, String walk, String form, boolean blazing) {

  public static final ShownForm NONE = new ShownForm("", "", "", false);
  static final String SPLIT = "|";
  static final String BLAZE = "1";

  public String encode() {
    return idle + SPLIT + walk + SPLIT + form + SPLIT + (blazing ? BLAZE : "");
  }

  public static ShownForm decode(final String packed) {
    final String[] parts = packed.split("\\" + SPLIT, -1);
    return parts.length == 4
        ? new ShownForm(parts[0], parts[1], parts[2], BLAZE.equals(parts[3]))
        : NONE;
  }

  public ShownForm blazing(final boolean blaze) {
    return new ShownForm(idle, walk, form, blaze);
  }

  public Optional<String> clip(final boolean still, final boolean walking) {
    if (still && !idle.isEmpty()) {
      return Optional.of(idle);
    }
    return walking && !walk.isEmpty() ? Optional.of(walk) : Optional.empty();
  }
}
