package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class RigArmorModelsTest {

  private static final ModelPart INNER = armour(0.5F);
  private static final ModelPart OUTER = armour(1.0F);

  private final RigArmorModels models = new RigArmorModels(INNER, OUTER);

  private static ModelPart armour(final float dilation) {
    return TexturedModelData.of(BipedEntityModel.getModelData(new Dilation(dilation), 0.0F), 64, 32)
        .createModel();
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = EquipmentSlot.class,
      names = {"HEAD", "CHEST", "LEGS", "FEET"})
  @DisplayName("leggings fit the inner armour model and every other slot the outer one")
  void picksTheArmourLayerForTheSlot(final EquipmentSlot slot) {
    final BipedEntityModel<LivingEntity> model = models.model(slot);
    assertSame(
        slot == EquipmentSlot.LEGS ? INNER.getChild("head") : OUTER.getChild("head"), model.head);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = EquipmentSlot.class,
      names = {"HEAD", "CHEST", "LEGS", "FEET"})
  @DisplayName("the fitted model is never a child, so armour is never drawn at baby scale")
  void neverDrawsAtBabyScale(final EquipmentSlot slot) {
    assertFalse(models.model(slot).child);
  }

  @ParameterizedTest(name = "{0} {1} -> {2}")
  @CsvSource({
    "HEAD, head, head",
    "CHEST, body, body",
    "CHEST, right_arm, right_arm",
    "CHEST, left_arm, left_arm",
    "LEGS, right_leg, right_leg",
    "LEGS, left_leg, left_leg",
    "FEET, right_leg, right_leg",
    "FEET, left_leg, left_leg"
  })
  @DisplayName("each anchor draws the matching part of the slot's armour model")
  void picksThePartForTheAnchor(final EquipmentSlot slot, final String name, final String child) {
    final ModelPart root = slot == EquipmentSlot.LEGS ? INNER : OUTER;
    assertSame(root.getChild(child), models.part(slot, name));
  }
}
