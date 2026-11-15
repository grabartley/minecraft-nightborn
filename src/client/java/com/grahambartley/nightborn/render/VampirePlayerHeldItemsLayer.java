package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.client.anim.VampirePlayerClips;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public final class VampirePlayerHeldItemsLayer
    extends BlockAndItemGeoLayer<VampirePlayerAnimatable> {

  static final String RIGHT = "item_right";
  static final String LEFT = "item_left";

  private final VampirePlayerRenderer players;

  public VampirePlayerHeldItemsLayer(final VampirePlayerRenderer renderer) {
    super(renderer);
    this.players = renderer;
  }

  @Nullable
  @Override
  protected ItemStack getStackForBone(
      final GeoBone bone, final VampirePlayerAnimatable animatable) {
    final var player = players.getCurrentEntity();
    if (player == null
        || !(RIGHT.equals(bone.getName()) || LEFT.equals(bone.getName()))
        || !VampirePlayerClips.showsHeldItems(player)) {
      return null;
    }
    final Arm arm = RIGHT.equals(bone.getName()) ? Arm.RIGHT : Arm.LEFT;
    final ItemStack stack =
        arm == player.getMainArm() ? player.getMainHandStack() : player.getOffHandStack();
    return stack.isEmpty() ? null : stack;
  }

  @Override
  protected ModelTransformationMode getTransformTypeForStack(
      final GeoBone bone, final ItemStack stack, final VampirePlayerAnimatable animatable) {
    return RIGHT.equals(bone.getName())
        ? ModelTransformationMode.THIRD_PERSON_RIGHT_HAND
        : ModelTransformationMode.THIRD_PERSON_LEFT_HAND;
  }

  @Override
  protected void renderStackForBone(
      final MatrixStack poseStack,
      final GeoBone bone,
      final ItemStack stack,
      final VampirePlayerAnimatable animatable,
      final VertexConsumerProvider bufferSource,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    final var player = players.getCurrentEntity();
    final boolean left = LEFT.equals(bone.getName());
    poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
    poseStack.translate(0.0F, 0.125F, -0.0625F);
    MinecraftClient.getInstance()
        .getItemRenderer()
        .renderItem(
            player,
            stack,
            getTransformTypeForStack(bone, stack, animatable),
            left,
            poseStack,
            bufferSource,
            player.getWorld(),
            packedLight,
            packedOverlay,
            player.getId());
  }
}
