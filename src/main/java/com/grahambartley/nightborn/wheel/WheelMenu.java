package com.grahambartley.nightborn.wheel;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record WheelMenu(int entityId, List<WheelEntry> entries) {

  public WheelMenu {
    entries = List.copyOf(entries);
  }

  public Optional<WheelEntry> find(
      final int chosenEntity, final Identifier action, final int argument) {
    if (chosenEntity != entityId) {
      return Optional.empty();
    }
    return entries.stream().filter(entry -> entry.names(action, argument)).findFirst();
  }
}
