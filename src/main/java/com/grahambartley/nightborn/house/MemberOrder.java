package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.servant.Stance;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.util.Identifier;

public enum MemberOrder {
  FOLLOW("follow", Stance.FOLLOW),
  STAY("stay", Stance.STAY),
  GUARD("guard", Stance.GUARD),
  FEED("feed", null),
  BRING_BLOOD("bring_blood", null),
  LEAVE("leave", null);

  private final Identifier icon;
  private final Stance stance;

  MemberOrder(final String icon, final Stance stance) {
    this.icon = WheelEntry.icon(icon);
    this.stance = stance;
  }

  public Identifier icon() {
    return icon;
  }

  public Optional<Stance> stance() {
    return Optional.ofNullable(stance);
  }

  public String labelKey() {
    return "nightborn.house.order." + name().toLowerCase(Locale.ROOT);
  }

  public String cueKey() {
    return "nightborn.house.ordered." + name().toLowerCase(Locale.ROOT);
  }

  public boolean current(final Optional<Stance> held, final boolean providing) {
    return this == BRING_BLOOD
        ? providing
        : stance != null && held.filter(stance::equals).isPresent();
  }

  public static Optional<MemberOrder> byIndex(final int index) {
    return index >= 0 && index < values().length ? Optional.of(values()[index]) : Optional.empty();
  }
}
