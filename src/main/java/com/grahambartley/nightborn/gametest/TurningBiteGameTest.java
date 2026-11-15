package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.feeding.TurningBite;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class TurningBiteGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireSwitchesTheirBiteBothWays(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.assertFalse(TurningBite.isOn(vampire), "a new vampire's bite feeds");
    context.assertTrue(TurningBite.toggle(vampire), "the switch is accepted");
    context.assertTrue(TurningBite.isOn(vampire), "the bite now turns");
    TurningBite.toggle(vampire);
    context.assertFalse(TurningBite.isOn(vampire), "and feeds again");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aHumanCannotSwitchABite(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    context.assertFalse(TurningBite.toggle(human), "a human has no bite to switch");
    context.assertFalse(TurningBite.isOn(human), "nothing is stored");
    TestPlayers.finish(context, human);
  }
}
