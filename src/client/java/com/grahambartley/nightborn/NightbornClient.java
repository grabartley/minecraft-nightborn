package com.grahambartley.nightborn;

import com.grahambartley.nightborn.ammo.AmmoEntities;
import com.grahambartley.nightborn.client.CoatingTooltip;
import com.grahambartley.nightborn.client.VampireClientFeatures;
import com.grahambartley.nightborn.client.fog.StructureFog;
import com.grahambartley.nightborn.client.state.ClientStateService;
import com.grahambartley.nightborn.clothing.ClothingRenderProviders;
import com.grahambartley.nightborn.coffin.CoffinBlockEntity;
import com.grahambartley.nightborn.coffin.Coffins;
import com.grahambartley.nightborn.decor.AnimatedFurniture;
import com.grahambartley.nightborn.decor.DecorBlockEntities;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.decor.DecorEntities;
import com.grahambartley.nightborn.decor.FurnitureBlockEntity;
import com.grahambartley.nightborn.gear.GearBlocks;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.render.AmmoModels;
import com.grahambartley.nightborn.render.BoltEntityRenderer;
import com.grahambartley.nightborn.render.ClothingColors;
import com.grahambartley.nightborn.render.ClothingRenderProvider;
import com.grahambartley.nightborn.render.CoffinArt;
import com.grahambartley.nightborn.render.CoffinRenderer;
import com.grahambartley.nightborn.render.FurnitureBlockRenderer;
import com.grahambartley.nightborn.render.GeoBlockItemRenderer;
import com.grahambartley.nightborn.render.VampireBatRenderer;
import com.grahambartley.nightborn.render.VampireRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class NightbornClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    ClientStateService.load(FabricLoader.getInstance().getConfigDir());
    BlockEntityRendererFactories.register(
        DecorBlockEntities.FURNITURE, FurnitureBlockRenderer::new);
    BlockEntityRendererFactories.register(Coffins.BLOCK_ENTITY, CoffinRenderer::new);
    Coffins.all()
        .forEach(
            block ->
                BuiltinItemRendererRegistry.INSTANCE.register(
                    block,
                    new GeoBlockItemRenderer<>(
                        block,
                        CoffinRenderer::new,
                        CoffinBlockEntity::new,
                        -CoffinArt.MODEL_CENTRE_Z)));
    DecorBlocks.all().stream()
        .filter(AnimatedFurniture.class::isInstance)
        .forEach(
            block ->
                BuiltinItemRendererRegistry.INSTANCE.register(
                    block,
                    new GeoBlockItemRenderer<>(
                        block, FurnitureBlockRenderer::new, FurnitureBlockEntity::new)));
    BlockRenderLayerMap.INSTANCE.putBlocks(
        RenderLayer.getTranslucent(),
        DecorBlocks.GOTHIC_STAINED_GLASS,
        DecorBlocks.GOTHIC_STAINED_GLASS_PANE);
    BlockRenderLayerMap.INSTANCE.putBlocks(
        RenderLayer.getCutout(), DecorBlocks.ORNATE_CHAIR, DecorBlocks.ORNATE_TABLE);
    BlockRenderLayerMap.INSTANCE.putBlocks(
        RenderLayer.getCutoutMipped(), GearBlocks.SILVER_ORE, GearBlocks.DEEPSLATE_SILVER_ORE);
    BlockRenderLayerMap.INSTANCE.putBlocks(
        RenderLayer.getCutout(), GearBlocks.GARLIC, GearBlocks.GARLIC_STRING);
    EntityRendererRegistry.register(DecorEntities.SEAT, EmptyEntityRenderer::new);
    EntityRendererRegistry.register(NightbornMobs.VAMPIRE, VampireRenderer::new);
    EntityRendererRegistry.register(NightbornMobs.VAMPIRE_BAT, VampireBatRenderer::new);
    EntityRendererRegistry.register(AmmoEntities.BOLT, BoltEntityRenderer::new);
    EntityRendererRegistry.register(AmmoEntities.GARLIC_BOMB, FlyingItemEntityRenderer::new);
    AmmoModels.register();
    ClothingRenderProviders.use(item -> new ClothingRenderProvider());
    ClothingColors.register();
    VampireClientFeatures.register();
    StructureFog.register();
    CoatingTooltip.register();
  }
}
