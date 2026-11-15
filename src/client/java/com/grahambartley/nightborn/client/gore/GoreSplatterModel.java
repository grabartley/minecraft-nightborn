package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.client.state.ClientStateService;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

public final class GoreSplatterModel extends ForwardingBakedModel {

  private static final int CORNERS = 4;

  public GoreSplatterModel(final BakedModel wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public boolean isVanillaAdapter() {
    return false;
  }

  @Override
  public void emitBlockQuads(
      final BlockRenderView blockView,
      final BlockState state,
      final BlockPos pos,
      final Supplier<Random> randomSupplier,
      final RenderContext context) {
    final SplatterLook look = SplatterLook.of(ClientStateService.current().gore());
    if (!look.drawn()) {
      return;
    }
    if (!look.tinted()) {
      super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
      return;
    }
    context.pushTransform(
        quad -> {
          for (int corner = 0; corner < CORNERS; corner++) {
            quad.color(corner, look.apply(quad.color(corner)));
          }
          return true;
        });
    super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
    context.popTransform();
  }
}
