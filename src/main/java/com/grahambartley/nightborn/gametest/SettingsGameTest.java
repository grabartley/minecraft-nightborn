package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.config.ConfigFile;
import com.grahambartley.nightborn.config.ConfigPaths;
import com.grahambartley.nightborn.config.FactionSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.nio.file.Path;
import java.time.Clock;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;

public class SettingsGameTest implements FabricGameTest {

  static final String BATCH = "settings";

  @GameTest(templateName = "nightborn:vampire_arena", batchId = BATCH, tickLimit = 20)
  public void aChangedSettingIsSavedInTheWorldAndShowsFactions(final TestContext context) {
    final var server = context.getWorld().getServer();
    final Path file = ConfigPaths.serverConfig(server.getSavePath(WorldSavePath.ROOT).normalize());
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.assertTrue(
        ServerConfigService.update(
            server, ServerConfigService.current().with(FactionSettings.SHOW_IN_PLAYER_LIST, true)),
        "the setting saved");
    context.assertTrue(
        new ConfigFile(file, Clock.systemUTC()).load().factions().showInPlayerList(),
        "the world's settings file holds the change");
    final Text listed = vampire.getPlayerListName();
    context.assertTrue(
        listed != null && listed.toString().contains("nightborn.faction.vampire.tag"),
        "the player list shows the faction when turned on");
    ServerConfigService.update(
        server, ServerConfigService.current().with(FactionSettings.SHOW_IN_PLAYER_LIST, false));
    context.assertTrue(vampire.getPlayerListName() == null, "and hides it when turned off");
    TestPlayers.finish(context, vampire);
  }
}
