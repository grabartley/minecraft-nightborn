package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.feeding.TurningBite;
import com.grahambartley.nightborn.sickness.BiteSicknessEffect;
import com.grahambartley.nightborn.vampire.VisibleState;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class VampireHud {

  static final Identifier VIGNETTE = Identifier.ofVanilla("textures/misc/vignette.png");
  static final Identifier TURNING_BITE = Identifier.of(Nightborn.MOD_ID, "hud/turning_bite");
  static final int TEXT_COLOUR = 0xD8C8B0;

  private VampireHud() {}

  public static void render(final DrawContext context, final RenderTickCounter tickCounter) {
    final MinecraftClient client = MinecraftClient.getInstance();
    final ClientPlayerEntity player = client.player;
    if (player == null || client.options.hudHidden) {
      return;
    }
    final StatusEffectInstance sickness = player.getStatusEffect(BiteSicknessEffect.ENTRY);
    if (sickness != null) {
      drawSicknessVignette(context, SicknessVignette.alpha(sickness.getAmplifier()));
    }
    final VisibleState visible = VisibleStateAttachment.get(player);
    int signs = 0;
    if (visible.isWeakened()) {
      drawSign(
          context,
          client,
          WeakenedSignRenderer.SPRITE,
          Text.translatable("nightborn.weakened.sign", visible.weakened()),
          signs++);
    }
    if (TurningBite.isOn(player)) {
      drawSign(
          context, client, TURNING_BITE, Text.translatable("nightborn.turning_bite.sign"), signs);
    }
  }

  private static void drawSign(
      final DrawContext context,
      final MinecraftClient client,
      final Identifier sprite,
      final Text text,
      final int index) {
    final int y = HudSigns.y(index);
    context.drawGuiTexture(sprite, HudSigns.X, y, HudSigns.ICON, HudSigns.ICON);
    context.drawTextWithShadow(
        client.textRenderer,
        text,
        HudSigns.X + HudSigns.TEXT_OFFSET_X,
        y + HudSigns.TEXT_OFFSET_Y,
        TEXT_COLOUR);
  }

  private static void drawSicknessVignette(final DrawContext context, final float alpha) {
    RenderSystem.enableBlend();
    RenderSystem.blendFunc(
        GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR);
    context.setShaderColor(alpha * 0.15F, alpha, alpha * 0.9F, 1.0F);
    context.drawTexture(
        VIGNETTE,
        0,
        0,
        -90,
        0.0F,
        0.0F,
        context.getScaledWindowWidth(),
        context.getScaledWindowHeight(),
        context.getScaledWindowWidth(),
        context.getScaledWindowHeight());
    context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.defaultBlendFunc();
    RenderSystem.disableBlend();
  }
}
