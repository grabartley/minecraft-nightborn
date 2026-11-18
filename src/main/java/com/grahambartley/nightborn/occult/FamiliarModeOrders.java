package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.servant.OrderCue;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.wheel.CommandWheel;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.Arrays;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class FamiliarModeOrders implements WheelAction<GhostlyFamiliarEntity> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "familiar_mode");
  static final float WHISPER_PITCH = 1.2F;

  static void register() {
    CommandWheel.subject(GhostlyFamiliarEntity.class, SkillTree.VAMPIRE)
        .add(new FamiliarModeOrders());
  }

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final GhostlyFamiliarEntity familiar) {
    return familiar.isOwnedBy(player) && !familiar.fading();
  }

  @Override
  public List<WheelEntry> entries(
      final ServerPlayerEntity player, final GhostlyFamiliarEntity familiar) {
    return Arrays.stream(FamiliarMode.values())
        .map(
            mode ->
                new WheelEntry(
                    ID,
                    mode.ordinal(),
                    mode.icon(),
                    Text.translatable(mode.labelKey()),
                    familiar.mode() == mode))
        .toList();
  }

  @Override
  public void perform(
      final ServerPlayerEntity player,
      final GhostlyFamiliarEntity familiar,
      final WheelEntry chosen) {
    final FamiliarMode mode = FamiliarMode.byIndex(chosen.argument());
    familiar.order(mode, player);
    OrderCue.give(
        familiar,
        player,
        Text.translatable(mode.translationKey(), familiar.getDisplayName()),
        SoundEvents.PARTICLE_SOUL_ESCAPE.value(),
        WHISPER_PITCH);
  }

  static void whisper(final GhostlyFamiliarEntity familiar) {
    familiar
        .getWorld()
        .playSound(
            null,
            familiar.getBlockPos(),
            SoundEvents.PARTICLE_SOUL_ESCAPE.value(),
            SoundCategory.NEUTRAL,
            0.6F,
            WHISPER_PITCH);
  }
}
