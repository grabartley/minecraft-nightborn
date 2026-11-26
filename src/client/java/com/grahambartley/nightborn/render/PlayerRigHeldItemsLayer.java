package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.client.anim.PlayerClips;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public final class PlayerRigHeldItemsLayer extends BlockAndItemGeoLayer<PlayerRigAnimatable> {

  private final PlayerRigRenderer players;

  public PlayerRigHeldItemsLayer(final PlayerRigRenderer renderer) {
    super(renderer);
    this.players = renderer;
  }

  @Nullable
  @Override
  protected ItemStack getStackForBone(final GeoBone bone, final PlayerRigAnimatable animatable) {
    final var player = players.getCurrentEntity();
    if (player == null || !PlayerClips.showsHeldItems(player)) {
      return null;
    }
    return HandItem.arm(bone.getName())
        .map(
            hand ->
                HandItem.stack(
                    hand, player.getMainArm(), player.getMainHandStack(), player.getOffHandStack()))
        .filter(stack -> !stack.isEmpty())
        .orElse(null);
  }

  @Override
  protected ModelTransformationMode getTransformTypeForStack(
      final GeoBone bone, final ItemStack stack, final PlayerRigAnimatable animatable) {
    return HandItem.arm(bone.getName()).map(HandItem::mode).orElse(ModelTransformationMode.NONE);
  }

  @Override
  protected void renderStackForBone(
      final MatrixStack poseStack,
      final GeoBone bone,
      final ItemStack stack,
      final PlayerRigAnimatable animatable,
      final VertexConsumerProvider bufferSource,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    final var player = players.getCurrentEntity();
    final boolean left = HandItem.arm(bone.getName()).orElse(Arm.RIGHT) == Arm.LEFT;
    HandItem.pose(poseStack);
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
