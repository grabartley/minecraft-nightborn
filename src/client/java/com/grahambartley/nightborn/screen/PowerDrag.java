package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.network.SetPowerSlotPayload;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record PowerDrag(Identifier skill, int fromSlot) {

  public static PowerDrag fromTree(final Identifier skill) {
    return new PowerDrag(skill, PowerBarRow.NONE);
  }

  public Optional<SetPowerSlotPayload> drop(final int overSlot) {
    if (overSlot != PowerBarRow.NONE) {
      return overSlot == fromSlot
          ? Optional.empty()
          : Optional.of(new SetPowerSlotPayload(overSlot, Optional.of(skill)));
    }
    return fromSlot == PowerBarRow.NONE
        ? Optional.empty()
        : Optional.of(new SetPowerSlotPayload(fromSlot, Optional.empty()));
  }
}
