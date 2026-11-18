package com.grahambartley.nightborn.client.mirror;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.network.MirrorPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class MirrorScreen extends Screen {

  static final Identifier FRAME = Identifier.of(Nightborn.MOD_ID, "mirror/frame");
  static final Identifier PANE = Identifier.of(Nightborn.MOD_ID, "mirror/pane");
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
    context.drawGuiTexture(
        PANE,
        glass.glassLeft(),
        glass.glassTop(),
        glass.glassRight() - glass.glassLeft(),
        glass.glassBottom() - glass.glassTop());
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
    context.drawGuiTexture(FRAME, glass.left(), glass.top(), MirrorGlass.WIDTH, MirrorGlass.HEIGHT);
  }

  @Override
  public boolean shouldPause() {
    return false;
  }
}
