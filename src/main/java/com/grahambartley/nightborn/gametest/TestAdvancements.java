package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

final class TestAdvancements {

  private TestAdvancements() {}

  static AdvancementEntry entry(final TestContext context, final String path) {
    final AdvancementEntry entry =
        context
            .getWorld()
            .getServer()
            .getAdvancementLoader()
            .get(Identifier.of(Nightborn.MOD_ID, path));
    context.assertTrue(entry != null, "advancement " + path + " is loaded");
    return entry;
  }

  static AdvancementProgress progress(
      final TestContext context, final ServerPlayerEntity player, final String path) {
    return player.getAdvancementTracker().getProgress(entry(context, path));
  }

  static boolean done(
      final TestContext context, final ServerPlayerEntity player, final String path) {
    return progress(context, player, path).isDone();
  }
}
