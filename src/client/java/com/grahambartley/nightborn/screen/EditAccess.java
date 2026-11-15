package com.grahambartley.nightborn.screen;

public enum EditAccess {
  EDITABLE(""),
  READ_ONLY("nightborn.screen.settings.read_only"),
  NO_WORLD("nightborn.screen.settings.no_world");

  private final String bannerKey;

  EditAccess(final String bannerKey) {
    this.bannerKey = bannerKey;
  }

  public static EditAccess resolve(final boolean inWorld, final boolean canEdit) {
    if (!inWorld) {
      return NO_WORLD;
    }
    return canEdit ? EDITABLE : READ_ONLY;
  }

  public String bannerKey() {
    return bannerKey;
  }
}
