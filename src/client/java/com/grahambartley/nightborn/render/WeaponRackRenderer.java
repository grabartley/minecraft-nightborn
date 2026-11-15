package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.decor.RackHooks;
import com.grahambartley.nightborn.decor.WallDecorBlock;
import com.grahambartley.nightborn.decor.WeaponRackBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class WeaponRackRenderer implements BlockEntityRenderer<WeaponRackBlockEntity> {

  static final float SCALE = 0.75f;
  static final float HANG_DEGREES = 135.0f;

  private final ItemRenderer items;

  public WeaponRackRenderer(final BlockEntityRendererFactory.Context context) {
    this.items = context.getItemRenderer();
  }

  @Override
  public void render(
      final WeaponRackBlockEntity rack,
      final float tickDelta,
      final MatrixStack matrices,
      final VertexConsumerProvider vertices,
      final int light,
      final int overlay) {
    final Direction facing = rack.getCachedState().get(WallDecorBlock.FACING);
    for (int hook = 0; hook < RackHooks.COUNT; hook++) {
      final ItemStack stack = rack.get(hook);
      if (stack.isEmpty()) {
        continue;
      }
      final Vec3d centre = RackHooks.centre(facing, hook);
      matrices.push();
      matrices.translate(centre.x, centre.y, centre.z);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - facing.asRotation()));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(HANG_DEGREES));
      matrices.scale(SCALE, SCALE, SCALE);
      this.items.renderItem(
          stack,
          ModelTransformationMode.FIXED,
          light,
          OverlayTexture.DEFAULT_UV,
          matrices,
          vertices,
          rack.getWorld(),
          (int) rack.getPos().asLong() + hook);
      matrices.pop();
    }
  }
}
