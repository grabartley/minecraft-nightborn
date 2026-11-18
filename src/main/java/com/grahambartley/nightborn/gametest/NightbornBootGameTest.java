package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public class NightbornBootGameTest implements FabricGameTest {

  static {
    TestPlayers.acceptSync();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void modIsLoadedOnTheServer(TestContext context) {
    context.assertTrue(
        FabricLoader.getInstance().isModLoaded(Nightborn.MOD_ID),
        Nightborn.MOD_ID + " should be loaded on the test server");
    context.complete();
  }
}
