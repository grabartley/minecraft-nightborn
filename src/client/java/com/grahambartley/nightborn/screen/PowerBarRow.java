package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.skill.PowerLayout;

public record PowerBarRow(int left, int top) {

  public static final int SLOT = 22;
  public static final int GAP = 2;
  public static final int WIDTH = PowerLayout.SLOTS * SLOT + (PowerLayout.SLOTS - 1) * GAP;
  public static final int NONE = -1;

  public static PowerBarRow centred(final int centre, final int top) {
    return new PowerBarRow(centre - WIDTH / 2, top);
  }

  public int slotX(final int slot) {
    return left + slot * (SLOT + GAP);
  }

  public int slotAt(final double mouseX, final double mouseY) {
    if (mouseY < top || mouseY >= top + SLOT) {
      return NONE;
    }
    for (int slot = 0; slot < PowerLayout.SLOTS; slot++) {
      if (mouseX >= slotX(slot) && mouseX < slotX(slot) + SLOT) {
        return slot;
      }
    }
    return NONE;
  }
}
