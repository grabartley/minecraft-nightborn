package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.config.DownedSettings;
import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.grahambartley.nightborn.feeding.TurningBite;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillProgress;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HardcoreVampireDeathGameTest implements FabricGameTest {

  static final String BATCH = "hardcore";

  @BeforeBatch(batchId = BATCH)
  public void hardcoreOn(final ServerWorld world) {
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(DownedSettings.HARDCORE_VAMPIRE_DEATH, true));
  }

  @AfterBatch(batchId = BATCH)
  public void hardcoreOff(final ServerWorld world) {
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(DownedSettings.HARDCORE_VAMPIRE_DEATH, false));
  }

  @GameTest(
      templateName = "nightborn:vampire_arena",
      batchId = BATCH,
      tickLimit = TestPlayers.READY + 20)
  public void aVampireWhoDiesComesBackAsANewHuman(final TestContext context) {
    final ServerPlayerEntity dead = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    dead.getEnderChestInventory().setStack(0, new ItemStack(Items.DIAMOND, 4));
    SkillAttachment.set(
        dead,
        SkillTree.VAMPIRE,
        TreeProgress.EMPTY
            .withEarned(5)
            .buy(
                Identifier.of("nightborn", "noble/noble_blood"),
                1,
                Identifier.of("nightborn", "noble")));
    SkillAttachment.set(dead, SkillTree.HUNTER, TreeProgress.EMPTY.withEarned(2));
    TurningBite.set(dead, true);
    dead.setSpawnPoint(
        World.OVERWORLD, context.getAbsolutePos(new BlockPos(4, 2, 4)), 0.0F, true, false);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          dead.damage(NightbornDamage.sun(context.getWorld()), 1000.0F);
          context.assertTrue(dead.isDead(), "the sun killed the vampire");
          final ServerPlayerEntity respawned =
              context
                  .getWorld()
                  .getServer()
                  .getPlayerManager()
                  .respawnPlayer(dead, false, Entity.RemovalReason.KILLED);
          context.assertEquals(FactionAttachment.get(respawned), Faction.HUMAN, "comes back human");
          context.assertFalse(TurningBite.isOn(respawned), "the turning bite is cleared");
          context.assertTrue(
              respawned.getEnderChestInventory().isEmpty(), "ender chest is emptied");
          context.assertTrue(respawned.getSpawnPointPosition() == null, "no respawn point is kept");
          context.assertEquals(
              SkillAttachment.get(respawned),
              SkillProgress.EMPTY,
              "no skills, bloodlines or hunter tree");
          TestPlayers.finish(context, respawned);
        });
  }
}
