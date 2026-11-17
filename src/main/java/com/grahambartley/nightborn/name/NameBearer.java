package com.grahambartley.nightborn.name;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.text.TextColor;

public interface NameBearer {

  NameStyle nameStyle();

  boolean bearsWomansName();

  default String epithet() {
    return "";
  }

  default void bearEpithet(final String epithet) {}

  default Optional<TextColor> nameColour() {
    return nameStyle().colour();
  }

  default Optional<UUID> namedBy() {
    return Optional.empty();
  }
}
