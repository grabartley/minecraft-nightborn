package com.grahambartley.nightborn.client.accessory;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.stage.VampireStaging;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public final class InsightReadout {

  static final String LINE = "hud.nightborn.insight";
  static final String HEALTH_ONLY = "hud.nightborn.insight.health";
  static final String STAGE = "vampire_stage";
  static final int COLOUR = 0xD8C8B0;
  static final int BELOW_CROSSHAIR = 12;

  private InsightReadout() {}

  public static void render(final DrawContext context, final RenderTickCounter tickCounter) {
    final MinecraftClient client = MinecraftClient.getInstance();
    final ClientPlayerEntity player = client.player;
    if (player == null
        || client.options.hudHidden
        || GiftAttributes.value(player, GiftAttributes.VAMPIRE_INSIGHT) <= 0.0
        || !(client.targetedEntity instanceof VampireEntity vampire)) {
      return;
    }
    final Text line = line(vampire.stageId(), vampire.getHealth(), vampire.getMaxHealth());
    context.drawCenteredTextWithShadow(
        client.textRenderer,
        line,
        context.getScaledWindowWidth() / 2,
        context.getScaledWindowHeight() / 2 + BELOW_CROSSHAIR,
        COLOUR);
  }

  public static Text line(final Identifier stage, final float health, final float maxHealth) {
    if (VampireStaging.NONE.equals(stage)) {
      return Text.translatable(HEALTH_ONLY, MathHelper.ceil(health), MathHelper.ceil(maxHealth));
    }
    return Text.translatable(
        LINE,
        Text.translatable(stage.toTranslationKey(STAGE)),
        MathHelper.ceil(health),
        MathHelper.ceil(maxHealth));
  }
}
