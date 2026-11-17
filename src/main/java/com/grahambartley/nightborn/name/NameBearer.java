package com.grahambartley.nightborn.name;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.text.TextColor;

public interface NameBearer {

  default boolean bearsWomansName() {
    return false;
  }

  default String epithet() {
    return "";
  }

  default void bearEpithet(final String epithet) {}

  default Optional<TextColor> nameColor() {
    return Optional.empty();
  }

  default Optional<UUID> namedBy() {
    return Optional.empty();
  }
}
