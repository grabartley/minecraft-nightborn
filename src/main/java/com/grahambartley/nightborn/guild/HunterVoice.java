package com.grahambartley.nightborn.guild;

import java.util.Locale;

public enum HunterVoice {
  AMBIENT,
  HURT,
  DEATH,
  ALERT,
  STRIKE,
  CHEER;

  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }
}
