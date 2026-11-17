package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.batform.BatHang;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public final class BatHangView {

  static final float TURN_TICKS = 8.0F;

  private static float before;
  private static float now;

  private BatHangView() {}

  static void register() {
    ClientTickEvents.END_CLIENT_TICK.register(BatHangView::tick);
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
  }

  private static void tick(final MinecraftClient client) {
    before = now;
    now = step(now, client.player != null && BatHang.isHanging(client.player));
  }

  private static void reset() {
    before = 0.0F;
    now = 0.0F;
  }

  public static float roll(final boolean thirdPerson, final float tickDelta) {
    return angle(thirdPerson, MathHelper.lerp(tickDelta, before, now));
  }

  static float step(final float turned, final boolean hanging) {
    return MathHelper.clamp(turned + (hanging ? 1.0F : -1.0F) / TURN_TICKS, 0.0F, 1.0F);
  }

  static float angle(final boolean thirdPerson, final double turned) {
    if (thirdPerson) {
      return 0.0F;
    }
    return (float) (Math.PI * turned * turned * (3.0 - 2.0 * turned));
  }
}
