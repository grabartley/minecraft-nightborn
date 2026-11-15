package com.grahambartley.nightborn;

import com.grahambartley.nightborn.client.VampireClientFeatures;
import com.grahambartley.nightborn.client.state.ClientStateService;
import com.grahambartley.nightborn.decor.AnimatedFurniture;
import com.grahambartley.nightborn.decor.DecorBlockEntities;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.decor.DecorEntities;
import com.grahambartley.nightborn.render.FurnitureBlockRenderer;
import com.grahambartley.nightborn.render.FurnitureItemRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.EmptyEntityRenderer;

public class NightbornClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    ClientStateService.load(FabricLoader.getInstance().getConfigDir());
    BlockEntityRendererFactories.register(
        DecorBlockEntities.FURNITURE, FurnitureBlockRenderer::new);
    DecorBlocks.all().stream()
        .filter(AnimatedFurniture.class::isInstance)
        .forEach(
            block ->
                BuiltinItemRendererRegistry.INSTANCE.register(
                    block, new FurnitureItemRenderer(block)));
    BlockRenderLayerMap.INSTANCE.putBlocks(
        RenderLayer.getTranslucent(),
        DecorBlocks.GOTHIC_STAINED_GLASS,
        DecorBlocks.GOTHIC_STAINED_GLASS_PANE);
    BlockRenderLayerMap.INSTANCE.putBlocks(
        RenderLayer.getCutout(), DecorBlocks.ORNATE_CHAIR, DecorBlocks.ORNATE_TABLE);
    EntityRendererRegistry.register(DecorEntities.SEAT, EmptyEntityRenderer::new);
    VampireClientFeatures.register();
  }
}
