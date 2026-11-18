package com.grahambartley.nightborn.client.moment;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.client.state.ClientStateService;
import com.grahambartley.nightborn.moment.MomentAttachment;
import com.grahambartley.nightborn.moment.MomentState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class CeremonyView {

  static final int NIGHT_COLOUR = 0x0B0A1E;
  static final int CARD_WIDTH = 192;
  static final int CARD_HEIGHT = 48;
  static final int TITLE_COLOUR = 0xE8D9B0;
  static final float FAINTEST = 0.02F;

  @Nullable private static Perspective before;

  private CeremonyView() {}

  public static void register() {
    ClientTickEvents.END_CLIENT_TICK.register(CeremonyView::tick);
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> restore(client));
    HudRenderCallback.EVENT.register(CeremonyView::render);
  }

  @Nullable
  public static CeremonyCamera.Shot shot(final Entity focused, final float tickDelta) {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (focused != client.player || before == null) {
      return null;
    }
    final MomentState state = MomentAttachment.get(client.player);
    if (state == null) {
      return null;
    }
    final float elapsed = state.elapsed(client.player.getWorld().getTime(), tickDelta);
    return state.phase(elapsed) == MomentState.Phase.CAMERA
        ? CeremonyCamera.at(state.yaw(), state.cameraProgress(elapsed))
        : null;
  }

  private static void tick(final MinecraftClient client) {
    if (client.player == null) {
      restore(client);
      return;
    }
    final MomentState state = MomentAttachment.get(client.player);
    final boolean circling =
        state != null
            && !ClientStateService.current().skipCeremonyCamera()
            && state.phase(state.elapsed(client.player.getWorld().getTime(), 0.0F))
                == MomentState.Phase.CAMERA;
    if (!circling) {
      restore(client);
      return;
    }
    if (before == null) {
      before = client.options.getPerspective();
    }
    client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
  }

  private static void restore(final MinecraftClient client) {
    if (before != null) {
      client.options.setPerspective(before);
      before = null;
    }
  }

  private static void render(final DrawContext context, final RenderTickCounter counter) {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client.player == null) {
      return;
    }
    final MomentState state = MomentAttachment.get(client.player);
    if (state == null) {
      return;
    }
    final float elapsed =
        state.elapsed(client.player.getWorld().getTime(), counter.getTickDelta(false));
    final int width = context.getScaledWindowWidth();
    final int height = context.getScaledWindowHeight();
    final float night = CeremonyFade.night(state, elapsed);
    if (night > FAINTEST) {
      context.fill(0, 0, width, height, argb(night, NIGHT_COLOUR));
    }
    final float card = CeremonyFade.card(state, elapsed);
    if (card > FAINTEST) {
      drawCard(context, client, state, card, width, height);
    }
  }

  private static void drawCard(
      final DrawContext context,
      final MinecraftClient client,
      final MomentState state,
      final float alpha,
      final int width,
      final int height) {
    final int x = (width - CARD_WIDTH) / 2;
    final int y = (height - CARD_HEIGHT) / 2;
    RenderSystem.enableBlend();
    context.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
    context.drawGuiTexture(
        Identifier.of(Nightborn.MOD_ID, "moment/" + state.moment().asString()),
        x,
        y,
        CARD_WIDTH,
        CARD_HEIGHT);
    context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    final Text title = Text.translatable(state.moment().cardKey());
    context.getMatrices().push();
    context.getMatrices().translate(width / 2.0F, height / 2.0F, 0.0F);
    final float scale = CeremonyTitle.scale(client.textRenderer.getWidth(title), CARD_WIDTH);
    context.getMatrices().scale(scale, scale, 1.0F);
    context.drawCenteredTextWithShadow(
        client.textRenderer,
        title,
        0,
        -client.textRenderer.fontHeight / 2,
        argb(alpha, TITLE_COLOUR));
    context.getMatrices().pop();
    RenderSystem.disableBlend();
  }

  private static int argb(final float alpha, final int rgb) {
    return Math.round(alpha * 255.0F) << 24 | rgb;
  }
}
