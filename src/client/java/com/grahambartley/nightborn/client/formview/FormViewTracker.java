package com.grahambartley.nightborn.client.formview;

import com.grahambartley.nightborn.formview.FormView;
import com.grahambartley.nightborn.formview.FormViewEcho;
import com.grahambartley.nightborn.formview.FormViews;
import com.grahambartley.nightborn.formview.Forms;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

public final class FormViewTracker {

  private static final double TICKS_PER_SECOND = 20.0;

  private static final RippleField RIPPLES = new RippleField();
  private static FormViewFade fade = FormViewFade.NONE;
  private static ClientWorld world;
  private static int pulseTicks;

  private FormViewTracker() {}

  public static void register() {
    ClientTickEvents.END_CLIENT_TICK.register(FormViewTracker::tick);
    ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
    ClientLifecycleEvents.CLIENT_STARTED.register(
        client ->
            client
                .getSoundManager()
                .registerListener(
                    (sound, sounds, range) -> heard(client, sound, Float.isFinite(range))));
  }

  public static FormViewFade fade() {
    return fade;
  }

  public static List<Ripple> ripples() {
    return RIPPLES.live();
  }

  public static double now(final float tickDelta) {
    return world == null ? 0.0 : (world.getTime() + tickDelta) / TICKS_PER_SECOND;
  }

  private static void tick(final MinecraftClient client) {
    final ClientPlayerEntity player = client.player;
    if (player == null || client.world == null) {
      reset();
      return;
    }
    if (client.world != world) {
      RIPPLES.clear();
      world = client.world;
    }
    final Optional<FormView> target =
        FormViews.pick(
            FormViews.all(client.world.getRegistryManager()), id -> Forms.on(id, player));
    fade = fade.step(target);
    final Optional<FormViewEcho> echo = fade.shown().flatMap(FormView::echo);
    if (echo.isEmpty()) {
      RIPPLES.clear();
      pulseTicks = 0;
      return;
    }
    final double now = now(0.0F);
    RIPPLES.expire(now, echo.get().rippleSeconds());
    if (echo.get().pulsesOn(pulseTicks++)) {
      SoundRipple.of(
              echo.get(), player.getEyePos(), echo.get().selfPulseVolume(), player.getEyePos(), now)
          .ifPresent(ripple -> RIPPLES.add(ripple, now, echo.get().rippleSeconds()));
    }
  }

  private static void heard(
      final MinecraftClient client, final SoundInstance sound, final boolean placed) {
    final ClientPlayerEntity player = client.player;
    final Optional<FormViewEcho> echo = fade.shown().flatMap(FormView::echo);
    if (!placed || player == null || echo.isEmpty() || world == null) {
      return;
    }
    final double now = now(client.getRenderTickCounter().getTickDelta(false));
    SoundRipple.of(
            echo.get(),
            new Vec3d(sound.getX(), sound.getY(), sound.getZ()),
            sound.getVolume(),
            player.getEyePos(),
            now)
        .ifPresent(ripple -> RIPPLES.add(ripple, now, echo.get().rippleSeconds()));
  }

  private static void reset() {
    fade = FormViewFade.NONE;
    RIPPLES.clear();
    world = null;
    pulseTicks = 0;
  }
}
