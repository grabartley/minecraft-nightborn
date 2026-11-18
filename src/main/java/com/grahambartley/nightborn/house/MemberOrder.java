package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.servant.Stance;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.util.Identifier;

public enum MemberOrder {
  FOLLOW("minecraft:lead", Stance.FOLLOW),
  STAY("minecraft:campfire", Stance.STAY),
  GUARD("minecraft:iron_sword", Stance.GUARD),
  FEED("nightborn:blood_splatter", null),
  BRING_BLOOD("nightborn:blood_bottle", null),
  LEAVE("minecraft:oak_door", null);

  private final Identifier icon;
  private final Stance stance;

  MemberOrder(final String icon, final Stance stance) {
    this.icon = Identifier.of(icon);
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
