package com.grahambartley.nightborn.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

final class RigArmorModels {

  private final BipedEntityModel<LivingEntity> inner;
  private final BipedEntityModel<LivingEntity> outer;

  RigArmorModels(final ModelPart inner, final ModelPart outer) {
    this.inner = grown(inner);
    this.outer = grown(outer);
  }

  static RigArmorModels load() {
    final EntityModelLoader models = MinecraftClient.getInstance().getEntityModelLoader();
    return new RigArmorModels(
        models.getModelPart(EntityModelLayers.PLAYER_INNER_ARMOR),
        models.getModelPart(EntityModelLayers.PLAYER_OUTER_ARMOR));
  }

  BipedEntityModel<LivingEntity> model(final EquipmentSlot slot) {
    return slot == EquipmentSlot.LEGS ? inner : outer;
  }

  ModelPart part(final EquipmentSlot slot, final String name) {
    final BipedEntityModel<LivingEntity> model = model(slot);
    return switch (name) {
      case "head" -> model.head;
      case "body" -> model.body;
      case "right_arm" -> model.rightArm;
      case "left_arm" -> model.leftArm;
      case "right_leg" -> model.rightLeg;
      case "left_leg" -> model.leftLeg;
      default -> throw new IllegalArgumentException("No armour part named " + name);
    };
  }

  private static BipedEntityModel<LivingEntity> grown(final ModelPart root) {
    final BipedEntityModel<LivingEntity> model = new BipedEntityModel<>(root);
    model.child = false;
    return model;
  }
}
