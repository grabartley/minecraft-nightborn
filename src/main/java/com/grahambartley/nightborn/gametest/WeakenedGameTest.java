package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import com.grahambartley.nightborn.weakened.Weakened;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class WeakenedGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void sourcesAddUpToTheCapAndEveryoneSeesIt(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 1, 4));
    Weakened.add(vampire, "garlic", 30, 200);
    Weakened.add(vampire, "cross", 20, 200);
    context.runAtTick(
        2,
        () -> {
          context.assertEquals(VisibleStateAttachment.get(vampire).weakened(), 50, "30 + 20");
          Weakened.add(vampire, "holy_water", 80, 200);
        });
    context.runAtTick(
        4,
        () -> {
          context.assertEquals(VisibleStateAttachment.get(vampire).weakened(), 100, "capped");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aDownedVampireIsWeakenedAtTheCap(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 1, 4));
    vampire.setAttached(Downed.TYPE, new DownedState(DownedState.Phase.ESCAPING, 200));
    context.runAtTick(
        2,
        () -> {
          context.assertEquals(VisibleStateAttachment.get(vampire).weakened(), 100, "at the cap");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void beingWeakenedNeverHurts(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 1, 4));
    Weakened.add(vampire, "garlic", 100, 200);
    context.runAtTick(
        10,
        () -> {
          context.assertFalse(vampire.getHealth() < 20.0F, "weakened alone does no damage");
          TestPlayers.finish(context, vampire);
        });
  }
}
