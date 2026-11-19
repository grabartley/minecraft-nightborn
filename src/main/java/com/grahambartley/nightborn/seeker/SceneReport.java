package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.skill.SkillNames;
import com.grahambartley.nightborn.skill.TrackingDirection;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public final class SceneReport {

  static final String WENT = "nightborn.seeker.scene";
  static final String COLD = "nightborn.seeker.scene_cold";
  static final String FAR = "nightborn.seeker.scene_far";

  private SceneReport() {}

  public static String key(final boolean known, final boolean found) {
    if (!known) {
      return FAR;
    }
    return found ? WENT : COLD;
  }

  public static String far() {
    return FAR;
  }

  public static void tell(final ServerPlayerEntity hunter, final Scene scene) {
    final LivingEntity vampire = Scenes.vampire(hunter.getServerWorld(), scene.vampire());
    hunter.sendMessage(text(hunter, scene, vampire).formatted(Formatting.GOLD), false);
  }

  private static MutableText text(
      final ServerPlayerEntity hunter, final Scene scene, @Nullable final LivingEntity vampire) {
    if (!scene.known()) {
      return Text.translatable(far());
    }
    final Text stage =
        scene
            .stage()
            .<Text>map(id -> Text.translatable(SkillNames.stage(id)))
            .orElseGet(() -> Text.translatable("nightborn.seeker.scene_vampire"));
    final Text lines =
        scene.lines().isEmpty()
            ? Text.translatable("nightborn.seeker.scene_no_bloodline")
            : SkillNames.lines(scene.lines());
    if (vampire == null) {
      return Text.translatable(key(true, false), stage, lines);
    }
    return Text.translatable(
        key(true, true),
        stage,
        lines,
        TrackingDirection.arrow(
            vampire.getX() - hunter.getX(), vampire.getZ() - hunter.getZ(), hunter.getYaw()));
  }
}
