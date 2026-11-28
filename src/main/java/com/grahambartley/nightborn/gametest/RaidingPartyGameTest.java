package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.age.AgeNight;
import com.grahambartley.nightborn.coffin.CoffinBlock;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.guild.raiding.RaidArms;
import com.grahambartley.nightborn.guild.raiding.RaidDispatch;
import com.grahambartley.nightborn.guild.raiding.RaidEnding;
import com.grahambartley.nightborn.guild.raiding.RaidOrders;
import com.grahambartley.nightborn.guild.raiding.RaidRules;
import com.grahambartley.nightborn.guild.raiding.RaidingData;
import com.grahambartley.nightborn.guild.raiding.RaidingParties;
import com.grahambartley.nightborn.guild.raiding.RaidingParty;
import com.grahambartley.nightborn.guild.veteran.Grounding;
import com.grahambartley.nightborn.guild.veteran.VeteranHunterEntity;
import com.grahambartley.nightborn.hunt.HuntAttachment;
import com.grahambartley.nightborn.hunt.Hunts;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;

public class RaidingPartyGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final long FAR_OFF = 100_000L;

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void anElderIsRaidedByEightHuntersLedByAVeteranWithTwiceTheHealth(
      final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(1, 2, 1));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final RaidArms arms =
              RaidingData.arms(
                      context.getWorld().getRegistryManager(), Identifier.of("nightborn", "elder"))
                  .orElseThrow();
          final BlockPos house = context.getAbsolutePos(new BlockPos(6, 2, 6));
          final RaidingParty raid =
              RaidDispatch.send(
                      context.getWorld(),
                      context.getAbsolutePos(new BlockPos(4, 2, 4)),
                      vampire,
                      Optional.of(GlobalPos.create(context.getWorld().getRegistryKey(), house)),
                      arms,
                      context.getWorld().getTimeOfDay())
                  .orElseThrow();
          final List<HunterEntity> raiders =
              raid.members().stream()
                  .map(context.getWorld()::getEntity)
                  .filter(HunterEntity.class::isInstance)
                  .map(HunterEntity.class::cast)
                  .toList();
          raiders.forEach(raider -> raider.setAiDisabled(true));
          context.assertEquals(raiders.size(), 8, "an elder faces eight hunters");
          final List<VeteranHunterEntity> veterans =
              raiders.stream()
                  .filter(VeteranHunterEntity.class::isInstance)
                  .map(VeteranHunterEntity.class::cast)
                  .toList();
          context.assertEquals(veterans.size(), 1, "one veteran leads the party");
          final double hunterHealth =
              DefaultAttributeRegistry.get(GuildMobs.HUNTER)
                  .getBaseValue(EntityAttributes.GENERIC_MAX_HEALTH);
          context.assertEquals(
              (double) veterans.getFirst().getMaxHealth(),
              hunterHealth * 2.0,
              "the veteran has twice a hunter's health");
          context.assertTrue(
              raiders.stream()
                  .allMatch(
                      raider ->
                          raider.raid().flatMap(RaidOrders::place).equals(Optional.of(house))),
              "every raider is sent to the house");
          RaidEnding.end(context.getWorld().getServer(), raid, RaidRules.End.CALLED_OFF);
          context.assertTrue(
              raiders.stream().allMatch(HunterEntity::isRemoved), "the party leaves when it ends");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, tickLimit = 40)
  public void aRaiderOpensTheLidOfAClosedCoffin(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 6), Direction.NORTH);
    final HunterEntity raider = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(5, 2, 4));
    raider.setAiDisabled(true);
    final UUID quarry = UUID.randomUUID();
    final long until = context.getWorld().getTimeOfDay() + FAR_OFF;
    final GlobalPos house =
        GlobalPos.create(context.getWorld().getRegistryKey(), context.getAbsolutePos(head));
    RaidingParties.get(context.getWorld().getServer())
        .start(
            new RaidingParty(
                quarry, List.of(raider.getUuid()), 1, until, Optional.of(house), Optional.empty()));
    raider.enlist(new RaidOrders(quarry, until, Optional.of(house.pos())), "");
    context.runAtEveryTick(
        () -> {
          if (context.getBlockState(head).get(CoffinBlock.OPEN)) {
            RaidingParties.get(context.getWorld().getServer()).remove(quarry);
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 40)
  public void theLastRaiderToFallEndsTheRaidAndBringsQuietNights(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(1, 2, 1));
    final HunterEntity raider = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(6, 2, 6));
    raider.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final long until = context.getWorld().getTimeOfDay() + FAR_OFF;
          raider.enlist(new RaidOrders(vampire.getUuid(), until, Optional.empty()), "");
          RaidingParties.get(context.getWorld().getServer())
              .start(
                  new RaidingParty(
                      vampire.getUuid(),
                      List.of(raider.getUuid()),
                      1,
                      until,
                      Optional.empty(),
                      Optional.empty()));
          raider.kill();
          context.assertTrue(
              RaidingParties.get(context.getWorld().getServer())
                  .against(vampire.getUuid())
                  .isEmpty(),
              "the raid ends with its last hunter");
        });
    context.runAtTick(
        TestPlayers.READY + 30,
        () -> {
          context.assertTrue(
              HuntAttachment.get(vampire).quiet(AgeNight.index(Hunts.time(vampire))),
              "the vampire has quiet nights after the raid");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aVampireCaughtByTheChainCannotJump(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(TestPlayers.READY, () -> Grounding.ground(vampire, 60));
    context.runAtTick(
        TestPlayers.READY + 5,
        () -> {
          context.assertTrue(
              vampire.getAttributeValue(EntityAttributes.GENERIC_JUMP_STRENGTH) == 0.0,
              "a grounded vampire cannot jump");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 80)
  public void aVeteransBrainWeighsItsOwnAttacks(final TestContext context) {
    final VeteranHunterEntity veteran =
        context.spawnEntity(GuildMobs.VETERAN_HUNTER, new BlockPos(1, 2, 4));
    veteran.setAiDisabled(true);
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(7, 2, 4));
    vampire.setAiDisabled(true);
    veteran.setTarget(vampire);
    final Identifier chain = Identifier.of("nightborn", "silver_chain");
    context.runAtEveryTick(
        () -> {
          veteran.fight();
          if (veteran
              .mobFighting()
              .lastThink()
              .filter(
                  think ->
                      think.ranked().stream()
                          .anyMatch(scored -> scored.option().id().equals(chain)))
              .isPresent()) {
            context.complete();
          }
        });
  }
}
