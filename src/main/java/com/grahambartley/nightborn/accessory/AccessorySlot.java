package com.grahambartley.nightborn.accessory;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public enum AccessorySlot {
  RING("hand", "ring", EquipmentSlot.CHEST),
  SECOND_RING("offhand", "ring", EquipmentSlot.CHEST),
  NECKLACE("chest", "necklace", EquipmentSlot.CHEST),
  BACK("chest", "back", EquipmentSlot.CHEST),
  GLOVES("hand", "glove", EquipmentSlot.CHEST),
  BELT("legs", "belt", EquipmentSlot.CHEST),
  FACE("head", "face", EquipmentSlot.HEAD),
  BROOCH("chest", "brooch", EquipmentSlot.CHEST);

  static final String TRINKETS = "trinkets";

  private final String group;
  private final String name;
  private final TagKey<Item> tag;
  private final EquipmentSlot drawnOn;

  AccessorySlot(final String group, final String name, final EquipmentSlot drawnOn) {
    this.group = group;
    this.name = name;
    this.drawnOn = drawnOn;
    this.tag = TagKey.of(RegistryKeys.ITEM, Identifier.of(TRINKETS, id()));
  }

  public static Optional<AccessorySlot> byId(final String id) {
    return Arrays.stream(values()).filter(slot -> slot.id().equals(id)).findFirst();
  }

  public static Optional<AccessorySlot> fitting(final ItemStack stack) {
    return Arrays.stream(values()).filter(slot -> stack.isIn(slot.tag)).findFirst();
  }

  public String id() {
    return this.group + "/" + this.name;
  }

  public String group() {
    return this.group;
  }

  public String slotName() {
    return this.name;
  }

  public TagKey<Item> tag() {
    return this.tag;
  }

  public boolean drawn() {
    return !this.name.equals("ring");
  }

  public EquipmentSlot drawnOn() {
    return this.drawnOn;
  }

  public EquipmentSlot body() {
    return bodyOf(this.group);
  }

  public static EquipmentSlot bodyOf(final String group) {
    return switch (group.toLowerCase(Locale.ROOT)) {
      case "head" -> EquipmentSlot.HEAD;
      case "legs" -> EquipmentSlot.LEGS;
      case "feet" -> EquipmentSlot.FEET;
      case "chest", "hand", "offhand" -> EquipmentSlot.CHEST;
      default -> throw new IllegalArgumentException("No body part for slot group " + group);
    };
  }
}
