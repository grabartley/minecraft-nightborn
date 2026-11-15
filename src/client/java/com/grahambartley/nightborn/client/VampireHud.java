package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.sickness.BiteSicknessEffect;
import com.grahambartley.nightborn.vampire.VisibleState;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class VampireHud {

  static final Identifier VIGNETTE = Identifier.ofVanilla("textures/misc/vignette.png");
  static final int SIGN_X = 4;
  static final int SIGN_Y = 4;
  static final int ICON = 18;
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
    if (visible.isWeakened()) {
      context.drawSprite(
          SIGN_X,
          SIGN_Y,
          0,
          ICON,
          ICON,
          client.getStatusEffectSpriteManager().getSprite(StatusEffects.WEAKNESS));
      context.drawTextWithShadow(
          client.textRenderer,
          Text.translatable("nightborn.weakened.sign", visible.weakened()),
          SIGN_X + ICON + 4,
          SIGN_Y + 5,
          TEXT_COLOUR);
    }
  }

  private static void drawSicknessVignette(final DrawContext context, final float alpha) {
    RenderSystem.enableBlend();
    context.setShaderColor(0.45F, 0.02F, 0.06F, alpha);
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
    RenderSystem.disableBlend();
  }
}
