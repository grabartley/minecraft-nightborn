package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.lord.Rally;
import com.grahambartley.nightborn.lord.RallyPower;
import com.grahambartley.nightborn.lord.SummonPower;
import com.grahambartley.nightborn.lord.Summons;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireBatEntity;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.VampireLordEntity;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ConstantIntProvider;

public class VampireLordGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final BlockPos CENTRE = new BlockPos(4, 2, 4);

  private static VampireLordEntity lord(final TestContext context) {
    final VampireLordEntity lord = context.spawnEntity(NightbornMobs.VAMPIRE_LORD, CENTRE);
    lord.setAiDisabled(true);
    lord.initialize(
        context.getWorld(),
        context.getWorld().getLocalDifficulty(lord.getBlockPos()),
        SpawnReason.SPAWN_EGG,
        null);
    return lord;
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aLordTakesTheLordStageRigAndTag(final TestContext context) {
    final VampireLordEntity lord = lord(context);
    context.assertTrue(
        lord.stageId().equals(VampireLordEntity.STAGE),
        "the lord is staged as a lord, not " + lord.stageId());
    context.assertTrue(
        lord.rig() == VampireRig.LORD, "the lord wears its own rig, not " + lord.rig());
    context.assertTrue(
        lord.getType()
            .isIn(TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("nightborn", "vampire_lords"))),
        "the lord counts for Lord Of The Manner");
    context.assertTrue(Vampires.isVampire(lord), "the lord is a vampire");
    context.assertTrue(
        lord.getMaxHealth() >= 150.0F, "the lord is far tougher than a household vampire");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aLordShowsItsBossBarOnlyToPlayersThatSeeIt(final TestContext context) {
    final VampireLordEntity lord = lord(context);
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    lord.onStartedTrackingBy(player);
    context.assertTrue(
        lord.presence().bar().getPlayers().contains(player), "a player near the lord sees its bar");
    lord.onStoppedTrackingBy(player);
    context.assertTrue(
        lord.presence().bar().getPlayers().isEmpty(), "a player who leaves no longer sees it");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aDownedLordsBarSaysSo(final TestContext context) {
    final VampireLordEntity lord = lord(context);
    lord.presence().update(lord);
    context.assertTrue(
        lord.presence().bar().getColor() == BossBar.Color.RED, "a standing lord's bar is red");
    Downed.down(lord, ServerConfigService.current().downed(), Optional.empty());
    lord.presence().update(lord);
    context.assertTrue(
        lord.presence().bar().getColor() == BossBar.Color.WHITE,
        "a downed lord's bar turns white, not " + lord.presence().bar().getColor());
    context.assertTrue(lord.isAlive(), "downing a lord does not end it");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aRallyTurnsTheCourtOnTheLordsFoe(final TestContext context) {
    final VampireLordEntity lord = lord(context);
    final ServerPlayerEntity foe = context.createMockCreativeServerPlayerInWorld();
    final VampireEntity first = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(1, 2, 1));
    final VampireEntity second = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(7, 2, 7));
    final VampireEntity stranger =
        context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(1, 2, 7));
    first.setAiDisabled(true);
    second.setAiDisabled(true);
    stranger.setAiDisabled(true);
    final BlockPos home = context.getAbsolutePos(CENTRE);
    lord.settleAt(home);
    first.settleAt(home);
    second.settleAt(home);
    stranger.settleAt(home.add(200, 0, 0));
    lord.setTarget(foe);
    final var answered = Rally.call(lord, new RallyPower(16));
    context.assertTrue(answered.size() == 2, "both housemates answer, not " + answered.size());
    context.assertTrue(
        first.getTarget() == foe && second.getTarget() == foe, "they turn on the foe");
    context.assertTrue(
        stranger.getTarget() == null, "a vampire from another household ignores the call");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aSwarmCallsVampireBatsBesideTheLord(final TestContext context) {
    final VampireLordEntity lord = lord(context);
    final List<Entity> bats =
        Summons.call(
            lord,
            new SummonPower(
                Registries.ENTITY_TYPE.getEntry(NightbornMobs.VAMPIRE_BAT),
                ConstantIntProvider.create(3),
                1.0F,
                1.0F,
                Optional.empty(),
                Optional.empty(),
                Optional.empty()));
    bats.forEach(bat -> ((VampireBatEntity) bat).setAiDisabled(true));
    context.assertTrue(bats.size() == 3, "three bats answer, not " + bats.size());
    for (final Entity bat : bats) {
      context.assertTrue(
          bat.isAlive() && bat.distanceTo(lord) < 3.0F, "a bat appears beside the lord");
    }
    context.complete();
  }
}
