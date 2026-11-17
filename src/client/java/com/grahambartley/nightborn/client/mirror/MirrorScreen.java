package com.grahambartley.nightborn.client.mirror;

import com.grahambartley.nightborn.network.MirrorPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class MirrorScreen extends Screen {

  static final int FRAME_GOLD = 0xFFB08D3C;
  static final int FRAME_SHADE = 0xFF6B5320;
  static final int GLASS = 0xFF1B2226;
  static final int GLASS_SHEEN = 0xFF2A353B;
  static final float FIGURE_OFFSET = 0.0625F;

  @Nullable private final LivingEntity reflected;

  private MirrorScreen(@Nullable final LivingEntity reflected) {
    super(Text.translatable("item.nightborn.hand_mirror"));
    this.reflected = reflected;
  }

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(
        MirrorPayload.ID,
        (payload, context) -> {
          final Entity entity =
              context.client().world == null
                  ? null
                  : context.client().world.getEntityById(payload.entityId());
          context
              .client()
              .setScreen(
                  new MirrorScreen(
                      payload.reflects() && entity instanceof LivingEntity living ? living : null));
        });
  }

  @Override
  public void render(
      final DrawContext context, final int mouseX, final int mouseY, final float delta) {
    super.render(context, mouseX, mouseY, delta);
    final MirrorGlass glass = MirrorGlass.centred(width, height);
    context.fill(glass.left(), glass.top(), glass.right(), glass.bottom(), FRAME_SHADE);
    context.fill(
        glass.left() + 1, glass.top() + 1, glass.right() - 1, glass.bottom() - 1, FRAME_GOLD);
    context.fill(
        glass.glassLeft() - 1,
        glass.glassTop() - 1,
        glass.glassRight() + 1,
        glass.glassBottom() + 1,
        FRAME_SHADE);
    context.fillGradient(
        glass.glassLeft(),
        glass.glassTop(),
        glass.glassRight(),
        glass.glassBottom(),
        GLASS_SHEEN,
        GLASS);
    if (reflected != null && reflected.isAlive()) {
      InventoryScreen.drawEntity(
          context,
          glass.glassLeft(),
          glass.glassTop(),
          glass.glassRight(),
          glass.glassBottom(),
          MirrorGlass.FIGURE_SIZE,
          FIGURE_OFFSET,
          mouseX,
          mouseY,
          reflected);
    }
  }

  @Override
  public boolean shouldPause() {
    return false;
  }
}
