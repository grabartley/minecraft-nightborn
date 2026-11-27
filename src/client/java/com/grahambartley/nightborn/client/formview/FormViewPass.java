package com.grahambartley.nightborn.client.formview;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.client.state.ClientStateService;
import com.grahambartley.nightborn.formview.FormView;
import com.grahambartley.nightborn.formview.FormViewEcho;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class FormViewPass {

  private static final Identifier SHADER = Identifier.of(Nightborn.MOD_ID, "form_view");

  private static ShaderProgram program;
  private static SimpleFramebuffer depth;
  private static SimpleFramebuffer scratch;
  private static boolean broken;
  private static boolean captured;
  private static final Matrix4f INVERSE_VIEW_PROJECTION = new Matrix4f();
  private static Vec3d camera = Vec3d.ZERO;
  private static CreatureRects.Packed creatures = CreatureRects.pack(List.of(), new Matrix4f());

  private FormViewPass() {}

  public static void register() {
    CoreShaderRegistrationCallback.EVENT.register(
        context -> context.register(SHADER, VertexFormats.BLIT_SCREEN, loaded -> program = loaded));
    WorldRenderEvents.LAST.register(FormViewPass::capture);
  }

  private static Optional<FormView> look() {
    final FormViewFade fade = FormViewTracker.fade();
    if (broken || program == null || !fade.visible()) {
      return Optional.empty();
    }
    return fade.shown().flatMap(ClientStateService.current().formViews()::look);
  }

  private static void capture(final WorldRenderContext context) {
    captured = false;
    final Optional<FormView> view = look();
    if (view.isEmpty()) {
      return;
    }
    try {
      final Matrix4f viewProjection =
          new Matrix4f(context.projectionMatrix()).mul(context.positionMatrix());
      INVERSE_VIEW_PROJECTION.set(viewProjection).invert();
      camera = context.camera().getPos();
      creatures =
          CreatureRects.pack(
              view.get().echo().map(echo -> creatureBoxes(context, echo)).orElse(List.of()),
              viewProjection);
      final Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
      depth = fit(depth, main, true);
      depth.copyDepthFrom(main);
      main.beginWrite(false);
      captured = true;
    } catch (final RuntimeException e) {
      giveUp(e);
    }
  }

  private static List<Box> creatureBoxes(
      final WorldRenderContext context, final FormViewEcho echo) {
    final MinecraftClient client = MinecraftClient.getInstance();
    final Entity viewer = context.camera().getFocusedEntity();
    final float tickDelta = context.tickCounter().getTickDelta(false);
    final Box reach = new Box(camera, camera).expand(echo.maxRange());
    return context.world().getOtherEntities(viewer, reach, CreatureRects::heard).stream()
        .sorted(Comparator.comparingDouble(entity -> entity.squaredDistanceTo(camera)))
        .limit(CreatureRects.CAPACITY)
        .map(
            entity ->
                entity
                    .getBoundingBox()
                    .offset(entity.getLerpedPos(tickDelta).subtract(entity.getPos()))
                    .offset(camera.negate()))
        .toList();
  }

  public static void draw(final float tickDelta) {
    if (!captured) {
      return;
    }
    captured = false;
    final Optional<FormView> view = look();
    final float strength = FormViewTracker.fade().strength(tickDelta);
    if (view.isEmpty() || strength <= 0.0F) {
      return;
    }
    try {
      run(view.get(), strength, tickDelta);
    } catch (final RuntimeException e) {
      giveUp(e);
    }
  }

  private static void run(final FormView view, final float strength, final float tickDelta) {
    final Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
    scratch = fit(scratch, main, false);
    final Map<String, float[]> values =
        FormViewUniforms.of(
            view,
            strength,
            FormViewTracker.now(tickDelta),
            FormViewTracker.ripples(),
            camera,
            creatures);
    values.forEach((name, value) -> uniform(name).ifPresent(uniform -> uniform.set(value)));
    uniform("TexelSize")
        .ifPresent(uniform -> uniform.set(1.0F / main.textureWidth, 1.0F / main.textureHeight));
    uniform("InverseViewProj").ifPresent(uniform -> uniform.set(INVERSE_VIEW_PROJECTION));
    program.addSampler("SceneSampler", main.getColorAttachment());
    program.addSampler("DepthSampler", depth.getDepthAttachment());
    scratch.beginWrite(true);
    RenderSystem.disableBlend();
    RenderSystem.disableDepthTest();
    RenderSystem.depthMask(false);
    program.bind();
    final BufferBuilder quad =
        RenderSystem.renderThreadTesselator()
            .begin(VertexFormat.DrawMode.QUADS, VertexFormats.BLIT_SCREEN);
    quad.vertex(0.0F, 0.0F, 0.0F);
    quad.vertex(1.0F, 0.0F, 0.0F);
    quad.vertex(1.0F, 1.0F, 0.0F);
    quad.vertex(0.0F, 1.0F, 0.0F);
    BufferRenderer.draw(quad.end());
    program.unbind();
    RenderSystem.depthMask(true);
    main.beginWrite(true);
    scratch.draw(main.textureWidth, main.textureHeight, true);
    GlStateManager._enableDepthTest();
  }

  private static Optional<GlUniform> uniform(final String name) {
    return Optional.ofNullable(program.getUniform(name));
  }

  private static SimpleFramebuffer fit(
      final SimpleFramebuffer current, final Framebuffer main, final boolean withDepth) {
    if (current == null) {
      return new SimpleFramebuffer(
          main.textureWidth, main.textureHeight, withDepth, MinecraftClient.IS_SYSTEM_MAC);
    }
    if (current.textureWidth != main.textureWidth || current.textureHeight != main.textureHeight) {
      current.resize(main.textureWidth, main.textureHeight, MinecraftClient.IS_SYSTEM_MAC);
    }
    return current;
  }

  private static void giveUp(final RuntimeException e) {
    broken = true;
    captured = false;
    Nightborn.LOGGER.warn("Form views are off for this session: the screen pass failed", e);
  }
}
