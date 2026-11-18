package com.grahambartley.nightborn.clothing;

import java.util.Locale;
import net.minecraft.item.ArmorItem;

public enum ClothingPiece {
  TRICORNE(ArmorItem.Type.HELMET, ClothingFabric.CLOTH, 0x24201F),
  TOP_HAT(ArmorItem.Type.HELMET, ClothingFabric.CLOTH, 0x1C1A1D),
  WIDE_BRIMMED_HAT(ArmorItem.Type.HELMET, ClothingFabric.CLOTH, 0x211D1E),
  FROCK_COAT(ArmorItem.Type.CHESTPLATE, ClothingFabric.CLOTH, 0x2B2326),
  CORSET(ArmorItem.Type.CHESTPLATE, ClothingFabric.CLOTH, 0x1F4A3A),
  DOUBLET(ArmorItem.Type.CHESTPLATE, ClothingFabric.CLOTH, 0x4A1C2A),
  BREECHES(ArmorItem.Type.LEGGINGS, ClothingFabric.CLOTH, 0x2A2427),
  TROUSERS(ArmorItem.Type.LEGGINGS, ClothingFabric.CLOTH, 0x252327),
  LONG_SKIRT(ArmorItem.Type.LEGGINGS, ClothingFabric.CLOTH, 0x1E1A1E),
  BUCKLED_SHOES(ArmorItem.Type.BOOTS, ClothingFabric.LEATHER, 0xFFFFFF),
  RIDING_BOOTS(ArmorItem.Type.BOOTS, ClothingFabric.LEATHER, 0xFFFFFF);

  static final int LEATHER_DURABILITY_MULTIPLIER = 5;

  private final ArmorItem.Type type;
  private final ClothingFabric fabric;
  private final int undyedColor;

  ClothingPiece(final ArmorItem.Type type, final ClothingFabric fabric, final int undyedColor) {
    this.type = type;
    this.fabric = fabric;
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
    return this.fabric.dyeable();
  }

  public int undyedColor() {
    return this.undyedColor;
  }
}
