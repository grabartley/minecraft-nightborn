package com.grahambartley.nightborn.village;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.util.Identifier;

public enum SuspicionCause {
  SEEN_FEEDING("seen_feeding"),
  SEEN_LEVITATING("seen_levitating"),
  SEEN_IN_BAT_FORM("seen_in_bat_form"),
  SEEN_USING_A_POWER("seen_using_a_power"),
  SEEN_BURNING("seen_burning"),
  SEEN_BRINGING_UP_FOOD("seen_bringing_up_food"),
  BITTEN_VILLAGER("bitten_villager"),
  DRAINED_ANIMAL("drained_animal"),
  MISSING_VILLAGER("missing_villager"),
  TAKEN_NEIGHBOUR("taken_neighbour");

  private final String path;

  SuspicionCause(final String path) {
    this.path = path;
  }

  public Identifier id() {
    return Identifier.of(Nightborn.MOD_ID, path);
  }
}
