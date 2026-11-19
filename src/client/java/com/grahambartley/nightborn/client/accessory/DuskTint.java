package com.grahambartley.nightborn.client.accessory;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.LightType;

public final class DuskTint {

  static final int COLOUR = 0x2A1A33;
  static final int MOST_ALPHA = 0x78;
  static final int FULL_SKY = 15;
  static final int NIGHT_DARKNESS = 11;

  private DuskTint() {}

  public static void draw(final DrawContext context) {
    final MinecraftClient client = MinecraftClient.getInstance();
    final ClientPlayerEntity player = client.player;
    if (player == null
        || client.world == null
        || GiftAttributes.value(player, GiftAttributes.DUSK_SIGHT) <= 0.0) {
      return;
    }
    final int alpha =
        alpha(
            client.world.getLightLevel(LightType.SKY, BlockPos.ofFloored(player.getEyePos())),
            client.world.getAmbientDarkness());
    if (alpha > 0) {
      context.fill(
          0,
          0,
          context.getScaledWindowWidth(),
          context.getScaledWindowHeight(),
          ColorHelper.Argb.withAlpha(alpha, COLOUR));
    }
  }

  public static int alpha(final int skyLight, final int ambientDarkness) {
    final double sky = Math.clamp(skyLight / (double) FULL_SKY, 0.0, 1.0);
    final double day = Math.clamp(1.0 - ambientDarkness / (double) NIGHT_DARKNESS, 0.0, 1.0);
    return (int) Math.round(MOST_ALPHA * sky * day);
  }
}
