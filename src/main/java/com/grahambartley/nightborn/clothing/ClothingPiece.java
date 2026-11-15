package com.grahambartley.nightborn.clothing;

import java.util.Locale;
import net.minecraft.item.ArmorItem;

public enum ClothingPiece {
  TRICORNE(ArmorItem.Type.HELMET, ClothingFabric.CLOTH, true, 0x24201F),
  TOP_HAT(ArmorItem.Type.HELMET, ClothingFabric.CLOTH, true, 0x1C1A1D),
  MOURNING_VEIL(ArmorItem.Type.HELMET, ClothingFabric.CLOTH, true, 0x1A1719),
  FROCK_COAT(ArmorItem.Type.CHESTPLATE, ClothingFabric.CLOTH, true, 0x2B2326),
  HIGH_COLLARED_CAPE(ArmorItem.Type.CHESTPLATE, ClothingFabric.CLOTH, true, 0x5A1A24),
  CORSET(ArmorItem.Type.CHESTPLATE, ClothingFabric.CLOTH, true, 0x1F4A3A),
  DOUBLET(ArmorItem.Type.CHESTPLATE, ClothingFabric.CLOTH, true, 0x4A1C2A),
  BREECHES(ArmorItem.Type.LEGGINGS, ClothingFabric.CLOTH, true, 0x2A2427),
  TROUSERS(ArmorItem.Type.LEGGINGS, ClothingFabric.CLOTH, true, 0x252327),
  LONG_SKIRT(ArmorItem.Type.LEGGINGS, ClothingFabric.CLOTH, true, 0x1E1A1E),
  BUCKLED_SHOES(ArmorItem.Type.BOOTS, ClothingFabric.LEATHER, false, 0xFFFFFF),
  RIDING_BOOTS(ArmorItem.Type.BOOTS, ClothingFabric.LEATHER, false, 0xFFFFFF);

  static final int LEATHER_DURABILITY_MULTIPLIER = 5;

  private final ArmorItem.Type type;
  private final ClothingFabric fabric;
  private final boolean dyeable;
  private final int undyedColor;

  ClothingPiece(
      final ArmorItem.Type type,
      final ClothingFabric fabric,
      final boolean dyeable,
      final int undyedColor) {
    this.type = type;
    this.fabric = fabric;
    this.dyeable = dyeable;
    this.undyedColor = undyedColor;
  }

  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  public ArmorItem.Type type() {
    return this.type;
  }

  public ClothingFabric fabric() {
    return this.fabric;
  }

  public int durability() {
    return this.type.getMaxDamage(LEATHER_DURABILITY_MULTIPLIER);
  }

  public boolean dyeable() {
    return this.dyeable;
  }

  public int undyedColor() {
    return this.undyedColor;
  }
}
