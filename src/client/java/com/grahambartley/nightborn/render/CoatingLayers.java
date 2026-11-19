package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.client.LiveCoating;
import com.grahambartley.nightborn.coating.Coat;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import org.joml.Matrix4f;

public final class CoatingLayers extends RenderLayer {

  static final float ITEM_SCALE = 8.0F;
  static final float ENTITY_SCALE = 0.16F;

  private static final Map<WeaponSheen, RenderLayer> ITEM = new EnumMap<>(WeaponSheen.class);
  private static final Map<WeaponSheen, RenderLayer> TRANSLUCENT = new EnumMap<>(WeaponSheen.class);
  private static final Map<WeaponSheen, RenderLayer> ENTITY = new EnumMap<>(WeaponSheen.class);

  static {
    for (final WeaponSheen sheen : WeaponSheen.values()) {
      ITEM.put(sheen, layer("item", sheen, GLINT_PROGRAM, ITEM_SCALE, false));
      TRANSLUCENT.put(
          sheen, layer("translucent", sheen, TRANSLUCENT_GLINT_PROGRAM, ITEM_SCALE, true));
      ENTITY.put(sheen, layer("entity", sheen, DIRECT_ENTITY_GLINT_PROGRAM, ENTITY_SCALE, false));
    }
  }

  private CoatingLayers() {
    super(
        "nightborn_coating",
        VertexFormats.POSITION,
        VertexFormat.DrawMode.QUADS,
        0,
        false,
        false,
        () -> {},
        () -> {});
  }

  public static List<RenderLayer> all() {
    return Stream.of(ITEM, TRANSLUCENT, ENTITY)
        .flatMap(layers -> layers.values().stream())
        .toList();
  }

  public static VertexConsumer item(
      final VertexConsumer base,
      final ItemStack stack,
      final VertexConsumerProvider provider,
      final RenderLayer layer) {
    final boolean translucent =
        MinecraftClient.isFabulousGraphicsOrBetter()
            && layer == TexturedRenderLayers.getItemEntityTranslucentCull();
    return union(base, stack, provider, translucent ? TRANSLUCENT : ITEM);
  }

  public static VertexConsumer entity(
      final VertexConsumer base, final ItemStack stack, final VertexConsumerProvider provider) {
    return union(base, stack, provider, ENTITY);
  }

  private static VertexConsumer union(
      final VertexConsumer base,
      final ItemStack stack,
      final VertexConsumerProvider provider,
      final Map<WeaponSheen, RenderLayer> layers) {
    VertexConsumer consumer = base;
    for (final WeaponSheen sheen :
        WeaponSheen.of(LiveCoating.of(stack).map(Coat::coating), LiveCoating.burning(stack))) {
      consumer = VertexConsumers.union(provider.getBuffer(layers.get(sheen)), consumer);
    }
    return consumer;
  }

  private static RenderLayer layer(
      final String kind,
      final WeaponSheen sheen,
      final RenderPhase.ShaderProgram program,
      final float scale,
      final boolean translucent) {
    final MultiPhaseParameters.Builder builder =
        MultiPhaseParameters.builder()
            .program(program)
            .texture(new RenderPhase.Texture(sheen.texture(), true, false))
            .writeMaskState(COLOR_MASK)
            .cull(DISABLE_CULLING)
            .depthTest(EQUAL_DEPTH_TEST)
            .transparency(GLINT_TRANSPARENCY)
            .texturing(
                new RenderPhase.Texturing(
                    "nightborn_coating_texturing",
                    () -> texture(sheen, scale),
                    RenderSystem::resetTextureMatrix));
    if (translucent) {
      builder.target(ITEM_ENTITY_TARGET);
    }
    return of(
        "nightborn_" + sheen.sheenName() + "_" + kind,
        VertexFormats.POSITION_TEXTURE,
        VertexFormat.DrawMode.QUADS,
        1536,
        builder.build(false));
  }

  private static void texture(final WeaponSheen sheen, final float scale) {
    final long millis = Util.getMeasuringTimeMs();
    final double speed = MinecraftClient.getInstance().options.getGlintSpeed().getValue();
    RenderSystem.setTextureMatrix(
        new Matrix4f()
            .translation(-sheen.across(millis, speed), sheen.down(millis, speed), 0.0F)
            .rotateZ(sheen.tilt())
            .scale(scale));
  }
}
