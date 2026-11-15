package com.grahambartley.nightborn.render;

import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public final class VampirePlayerArmorLayer extends GeoRenderLayer<VampirePlayerAnimatable> {

  static final int UNDYED = -6265536;

  record Piece(EquipmentSlot slot, String part) {}

  static final Map<String, Piece> PIECES =
      Map.of(
          "armor_head", new Piece(EquipmentSlot.HEAD, "head"),
          "armor_body", new Piece(EquipmentSlot.CHEST, "body"),
          "armor_right_arm", new Piece(EquipmentSlot.CHEST, "right_arm"),
          "armor_left_arm", new Piece(EquipmentSlot.CHEST, "left_arm"),
          "armor_right_leg", new Piece(EquipmentSlot.LEGS, "right_leg"),
          "armor_left_leg", new Piece(EquipmentSlot.LEGS, "left_leg"),
          "armor_right_boot", new Piece(EquipmentSlot.FEET, "right_leg"),
          "armor_left_boot", new Piece(EquipmentSlot.FEET, "left_leg"));

  private final VampirePlayerRenderer players;
  private BipedEntityModel<LivingEntity> inner;
  private BipedEntityModel<LivingEntity> outer;

  public VampirePlayerArmorLayer(final VampirePlayerRenderer renderer) {
    super(renderer);
    this.players = renderer;
  }

  @Override
  public void renderForBone(
      final MatrixStack poseStack,
      final VampirePlayerAnimatable animatable,
      final GeoBone bone,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    final Piece piece = PIECES.get(bone.getName());
    final var player = players.getCurrentEntity();
    if (piece == null || player == null) {
      return;
    }
    final ItemStack stack = player.getEquippedStack(piece.slot());
    if (!(stack.getItem() instanceof ArmorItem armor)) {
      return;
    }
    final BipedEntityModel<LivingEntity> model = model(piece.slot());
    final ModelPart part = part(model, piece.part());
    if (part.isEmpty()) {
      return;
    }
    poseStack.push();
    poseStack.scale(-1.0F, -1.0F, 1.0F);
    fit(poseStack, bone, part);
    render(
        poseStack,
        bufferSource,
        stack,
        armor.getMaterial(),
        piece.slot(),
        part,
        packedLight,
        packedOverlay);
    poseStack.pop();
  }

  private void render(
      final MatrixStack poseStack,
      final VertexConsumerProvider bufferSource,
      final ItemStack stack,
      final RegistryEntry<ArmorMaterial> material,
      final EquipmentSlot slot,
      final ModelPart part,
      final int light,
      final int overlay) {
    final boolean legs = slot == EquipmentSlot.LEGS;
    final int color =
        stack.isIn(ItemTags.DYEABLE) ? DyedColorComponent.getColor(stack, UNDYED) : -1;
    for (final ArmorMaterial.Layer layer : material.value().layers()) {
      part.render(
          poseStack,
          bufferSource.getBuffer(RenderLayer.getArmorCutoutNoCull(layer.getTexture(legs))),
          light,
          overlay,
          layer.isDyeable() ? color : -1);
    }
    final ArmorTrim trim = stack.get(DataComponentTypes.TRIM);
    if (trim != null) {
      final Sprite sprite =
          MinecraftClient.getInstance()
              .getBakedModelManager()
              .getAtlas(TexturedRenderLayers.ARMOR_TRIMS_ATLAS_TEXTURE)
              .getSprite(
                  legs ? trim.getLeggingsModelId(material) : trim.getGenericModelId(material));
      part.render(
          poseStack,
          sprite.getTextureSpecificVertexConsumer(
              bufferSource.getBuffer(
                  TexturedRenderLayers.getArmorTrims(trim.getPattern().value().decal()))),
          light,
          overlay);
    }
    if (stack.hasGlint()) {
      part.render(
          poseStack, bufferSource.getBuffer(RenderLayer.getArmorEntityGlint()), light, overlay);
    }
  }

  private static void fit(final MatrixStack poseStack, final GeoBone bone, final ModelPart part) {
    final GeoCube cube = bone.getCubes().getFirst();
    final ModelPart.Cuboid armor =
        part.getRandomCuboid(net.minecraft.util.math.random.Random.create());
    final float scaleX = (float) (cube.size().x / Math.abs(armor.maxX - armor.minX));
    final float scaleY = (float) (cube.size().y / Math.abs(armor.maxY - armor.minY));
    final float scaleZ = (float) (cube.size().z / Math.abs(armor.maxZ - armor.minZ));
    part.setPivot(
        -(bone.getPivotX() - ((bone.getPivotX() * scaleX) - bone.getPivotX()) / scaleX),
        -(bone.getPivotY() - ((bone.getPivotY() * scaleY) - bone.getPivotY()) / scaleY),
        bone.getPivotZ() - ((bone.getPivotZ() * scaleZ) - bone.getPivotZ()) / scaleZ);
    part.pitch = -bone.getRotX();
    part.yaw = -bone.getRotY();
    part.roll = bone.getRotZ();
    poseStack.scale(scaleX, scaleY, scaleZ);
  }

  private BipedEntityModel<LivingEntity> model(final EquipmentSlot slot) {
    if (inner == null) {
      final var models = MinecraftClient.getInstance().getEntityModelLoader();
      inner = new BipedEntityModel<>(models.getModelPart(EntityModelLayers.PLAYER_INNER_ARMOR));
      outer = new BipedEntityModel<>(models.getModelPart(EntityModelLayers.PLAYER_OUTER_ARMOR));
    }
    return slot == EquipmentSlot.LEGS ? inner : outer;
  }

  private static ModelPart part(final BipedEntityModel<LivingEntity> model, final String name) {
    return switch (name) {
      case "head" -> model.head;
      case "body" -> model.body;
      case "right_arm" -> model.rightArm;
      case "left_arm" -> model.leftArm;
      case "right_leg" -> model.rightLeg;
      default -> model.leftLeg;
    };
  }
}
