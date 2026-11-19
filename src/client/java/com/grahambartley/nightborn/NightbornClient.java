package com.grahambartley.nightborn;

import com.grahambartley.nightborn.ammo.AmmoEntities;
import com.grahambartley.nightborn.boss.BossEntities;
import com.grahambartley.nightborn.client.CoatingTooltip;
import com.grahambartley.nightborn.client.VampireClientFeatures;
import com.grahambartley.nightborn.client.boss.ArenaWallRenderer;
import com.grahambartley.nightborn.client.boss.BossArenaView;
import com.grahambartley.nightborn.client.boss.EldestSetTooltip;
import com.grahambartley.nightborn.client.familiar.HumanFamiliarRenderer;
import com.grahambartley.nightborn.client.fog.StructureFog;
import com.grahambartley.nightborn.client.gore.SplatterModels;
import com.grahambartley.nightborn.client.guild.HunterRenderer;
import com.grahambartley.nightborn.client.name.NamingDialogue;
import com.grahambartley.nightborn.client.occult.GhostlyFamiliarRenderer;
import com.grahambartley.nightborn.client.ritual.RitualGloom;
import com.grahambartley.nightborn.client.state.ClientStateService;
import com.grahambartley.nightborn.client.structure.NearbyStructure;
import com.grahambartley.nightborn.client.structure.StructureAmbiencePlayer;
import com.grahambartley.nightborn.client.thrall.ThrallRenderer;
import com.grahambartley.nightborn.client.wheel.WheelDialogue;
import com.grahambartley.nightborn.clothing.ClothingRenderProviders;
import com.grahambartley.nightborn.coffin.CoffinBlockEntity;
import com.grahambartley.nightborn.coffin.Coffins;
import com.grahambartley.nightborn.decor.AnimatedFurniture;
import com.grahambartley.nightborn.decor.DecorBlockEntities;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.decor.DecorEntities;
import com.grahambartley.nightborn.decor.FurnitureBlockEntity;
import com.grahambartley.nightborn.familiar.Familiars;
import com.grahambartley.nightborn.gear.GearBlocks;
import com.grahambartley.nightborn.gramophone.GramophoneBlockEntity;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.party.TentBlocks;
import com.grahambartley.nightborn.house.crest.CrestBanners;
import com.grahambartley.nightborn.inquisitor.InquisitorMobs;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.occult.GhostlyFamiliars;
import com.grahambartley.nightborn.occult.RestlessDead;
import com.grahambartley.nightborn.oldblood.OldBloodBlocks;
import com.grahambartley.nightborn.render.AccessoryRenderer;
import com.grahambartley.nightborn.render.AmmoModels;
import com.grahambartley.nightborn.render.BoltEntityRenderer;
import com.grahambartley.nightborn.render.ClothingColors;
import com.grahambartley.nightborn.render.ClothingRenderProvider;
import com.grahambartley.nightborn.render.CoffinArt;
import com.grahambartley.nightborn.render.CoffinRenderer;
import com.grahambartley.nightborn.render.CrestBannerItemRenderer;
import com.grahambartley.nightborn.render.CrestBannerRenderer;
import com.grahambartley.nightborn.render.FurnitureBlockRenderer;
import com.grahambartley.nightborn.render.GeoBlockItemRenderer;
import com.grahambartley.nightborn.render.GramophoneRenderer;
import com.grahambartley.nightborn.render.HunterTentRenderer;
import com.grahambartley.nightborn.render.OldBloodFontRenderer;
import com.grahambartley.nightborn.render.RestlessDeadRenderer;
import com.grahambartley.nightborn.render.RitualAltarRenderer;
import com.grahambartley.nightborn.render.VampireBatRenderer;
import com.grahambartley.nightborn.render.VampireRenderer;
import com.grahambartley.nightborn.render.WeaponRackRenderer;
import com.grahambartley.nightborn.render.ZoneMarkRenderer;
import com.grahambartley.nightborn.ritual.RitualAltarBlockEntity;
import com.grahambartley.nightborn.ritual.RitualBlocks;
import com.grahambartley.nightborn.thrall.Thralls;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
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
    BlockEntityRendererFactories.register(DecorBlockEntities.WEAPON_RACK, WeaponRackRenderer::new);
    BlockEntityRendererFactories.register(RitualBlocks.ALTAR_ENTITY, RitualAltarRenderer::new);
    BlockEntityRendererFactories.register(OldBloodBlocks.FONT_ENTITY, OldBloodFontRenderer::new);
    BlockEntityRendererFactories.register(TentBlocks.HUNTER_TENT_ENTITY, HunterTentRenderer::new);
    BuiltinItemRendererRegistry.INSTANCE.register(
        RitualBlocks.ALTAR,
        new GeoBlockItemRenderer<>(
            RitualBlocks.ALTAR, RitualAltarRenderer::new, RitualAltarBlockEntity::new));
    BlockEntityRendererFactories.register(CrestBanners.BLOCK_ENTITY, CrestBannerRenderer::new);
    BuiltinItemRendererRegistry.INSTANCE.register(CrestBanners.ITEM, new CrestBannerItemRenderer());
    BlockEntityRendererFactories.register(DecorBlockEntities.GRAMOPHONE, GramophoneRenderer::new);
    BuiltinItemRendererRegistry.INSTANCE.register(
        DecorBlocks.GRAMOPHONE,
        new GeoBlockItemRenderer<>(
            DecorBlocks.GRAMOPHONE, GramophoneRenderer::new, GramophoneBlockEntity::new));
    SplatterModels.register();
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
        RenderLayer.getCutout(),
        DecorBlocks.ORNATE_CHAIR,
        DecorBlocks.ORNATE_TABLE,
        DecorBlocks.BLOOD_POOL,
        DecorBlocks.BLOOD_SPLATTER,
        DecorBlocks.CHALICE);
    BlockRenderLayerMap.INSTANCE.putBlocks(
        RenderLayer.getCutoutMipped(), GearBlocks.SILVER_ORE, GearBlocks.DEEPSLATE_SILVER_ORE);
    BlockRenderLayerMap.INSTANCE.putBlocks(
        RenderLayer.getCutout(), GearBlocks.GARLIC, GearBlocks.GARLIC_STRING);
    EntityRendererRegistry.register(DecorEntities.SEAT, EmptyEntityRenderer::new);
    EntityRendererRegistry.register(NightbornMobs.VAMPIRE, VampireRenderer::new);
    EntityRendererRegistry.register(NightbornMobs.VAMPIRE_LORD, VampireRenderer::new);
    EntityRendererRegistry.register(NightbornMobs.ELDEST, VampireRenderer::new);
    EntityRendererRegistry.register(NightbornMobs.VAMPIRE_BAT, VampireBatRenderer::new);
    EntityRendererRegistry.register(BossEntities.ZONE_MARK, ZoneMarkRenderer::new);
    EntityRendererRegistry.register(NightbornMobs.PET_BAT, VampireBatRenderer::new);
    EntityRendererRegistry.register(Familiars.HUMAN_FAMILIAR, HumanFamiliarRenderer::new);
    EntityRendererRegistry.register(Thralls.THRALL, ThrallRenderer::new);
    EntityRendererRegistry.register(
        GhostlyFamiliars.GHOSTLY_FAMILIAR, GhostlyFamiliarRenderer::new);
    EntityRendererRegistry.register(RestlessDead.TYPE, RestlessDeadRenderer::new);
    EntityRendererRegistry.register(GuildMobs.HUNTER, HunterRenderer::new);
    EntityRendererRegistry.register(GuildMobs.GUILD_MASTER, HunterRenderer::new);
    EntityRendererRegistry.register(InquisitorMobs.GRAND_INQUISITOR, HunterRenderer::new);
    EntityRendererRegistry.register(AmmoEntities.BOLT, BoltEntityRenderer::new);
    EntityRendererRegistry.register(AmmoEntities.GARLIC_BOMB, FlyingItemEntityRenderer::new);
    AmmoModels.register();
    ClothingRenderProviders.use(item -> new ClothingRenderProvider());
    ClothingColors.register();
    AccessoryRenderer.register();
    VampireClientFeatures.register();
    NearbyStructure.register();
    StructureFog.register();
    RitualGloom.register();
    BossArenaView.register();
    EldestSetTooltip.register();
    WorldRenderEvents.AFTER_ENTITIES.register(ArenaWallRenderer::render);
    NamingDialogue.register();
    WheelDialogue.register();
    StructureAmbiencePlayer.register();
    CoatingTooltip.register();
  }
}
