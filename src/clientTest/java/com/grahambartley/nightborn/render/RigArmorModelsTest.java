package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class RigArmorModelsTest {

  private static final ModelPart INNER = armour(0.5F);
  private static final ModelPart OUTER = armour(1.0F);

  private final RigArmorModels models = new RigArmorModels(INNER, OUTER);

  private static ModelPart armour(final float dilation) {
    return TexturedModelData.of(BipedEntityModel.getModelData(new Dilation(dilation), 0.0F), 64, 32)
        .createModel();
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"HEAD, outer", "CHEST, outer", "LEGS, inner", "FEET, outer"})
  @DisplayName("leggings fit the inner armour model and every other slot the outer one")
  void picksTheArmourLayerForTheSlot(final EquipmentSlot slot, final String layer) {
    assertSame(root(layer).getChild("head"), models.model(slot).head);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = EquipmentSlot.class,
      names = {"HEAD", "CHEST", "LEGS", "FEET"})
  @DisplayName("the fitted model is never a child, so armour is never drawn at baby scale")
  void neverDrawsAtBabyScale(final EquipmentSlot slot) {
    assertFalse(models.model(slot).child);
  }

  @ParameterizedTest(name = "{0} {1} -> {2} {3}")
  @CsvSource({
    "HEAD, head, outer, head",
    "CHEST, body, outer, body",
    "CHEST, right_arm, outer, right_arm",
    "CHEST, left_arm, outer, left_arm",
    "LEGS, right_leg, inner, right_leg",
    "LEGS, left_leg, inner, left_leg",
    "FEET, right_leg, outer, right_leg",
    "FEET, left_leg, outer, left_leg"
  })
  @DisplayName("each anchor draws the matching part of the slot's armour model")
  void picksThePartForTheAnchor(
      final EquipmentSlot slot, final String name, final String layer, final String child) {
    assertSame(root(layer).getChild(child), models.part(slot, name));
  }

  @ParameterizedTest(name = "part named [{0}]")
  @ValueSource(strings = {"", "hat", "leftleg"})
  @DisplayName("an unknown part name is refused instead of drawing some other part")
  void refusesUnknownParts(final String name) {
    assertThrows(IllegalArgumentException.class, () -> models.part(EquipmentSlot.HEAD, name));
  }

  private static ModelPart root(final String layer) {
    return "inner".equals(layer) ? INNER : OUTER;
  }
}
