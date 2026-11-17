package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.client.anim.VampirePlayerClips;
import com.grahambartley.nightborn.client.gore.BloodFx;
import com.grahambartley.nightborn.client.mirror.MirrorScreen;
import com.grahambartley.nightborn.client.name.StageNameView;
import com.grahambartley.nightborn.client.smoke.SmokeFx;
import com.grahambartley.nightborn.client.vomit.VomitFx;
import com.grahambartley.nightborn.network.ToggleLevitationPayload;
import com.grahambartley.nightborn.network.ToggleTurningBitePayload;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.lwjgl.glfw.GLFW;

public final class VampireClientFeatures {

  static final int BLOOD_TINT = 0xFF6E0A14;

  private VampireClientFeatures() {}

  public static void register() {
    SyncedServerConfig.register();
    ColorProviderRegistry.ITEM.register(
        (stack, tintIndex) -> tintIndex == 0 ? BLOOD_TINT : -1, BloodItems.BLOOD_BOTTLE);
    LivingEntityFeatureRendererRegistrationCallback.EVENT.register(
        (entityType, renderer, helper, context) -> {
          if (renderer instanceof PlayerEntityRenderer players) {
            helper.register(new VampireEyesFeatureRenderer(players));
          }
        });
    HudRenderCallback.EVENT.register(VampireHud::render);
    HudRenderCallback.EVENT.register(PowerBarHud::render);
    SkillKeys.register();
    PayloadKey.register(
        "key.nightborn.levitate", GLFW.GLFW_KEY_H, ToggleLevitationPayload.INSTANCE);
    PayloadKey.register(
        "key.nightborn.turning_bite", GLFW.GLFW_KEY_J, ToggleTurningBitePayload.INSTANCE);
    BatHangInput.register();
    BatHangView.register();
    VampirePlayerClips.register();
    StageNameView.register();
    MirrorScreen.register();
    VomitFx.register();
    BloodFx.register();
    SmokeFx.register();
    WorldRenderEvents.AFTER_ENTITIES.register(WeakenedSignRenderer::render);
  }
}
