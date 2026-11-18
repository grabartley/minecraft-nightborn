package com.grahambartley.nightborn.accessory;

public enum Relocation {
  SLOT,
  INVENTORY,
  FEET;

  public static Relocation of(final boolean slotFree, final boolean inventoryRoom) {
    if (slotFree) {
      return SLOT;
    }
    return inventoryRoom ? INVENTORY : FEET;
  }
}
