package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.occult.FamiliarMode;
import com.grahambartley.nightborn.occult.FamiliarModeOrders;
import com.grahambartley.nightborn.occult.FamiliarPower;
import com.grahambartley.nightborn.occult.FogClouds;
import com.grahambartley.nightborn.occult.GhostlyFamiliarEntity;
import com.grahambartley.nightborn.occult.GhostlyFamiliars;
import com.grahambartley.nightborn.occult.goal.FamiliarScoutGoal;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerCooldowns;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillPower;
import com.grahambartley.nightborn.skill.Skills;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class OccultGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "occult";
  static final int SHORT_LIFE = 40;
  static final int SCOUT_TICKS = 20;
  static final BlockPos BEYOND_PICKUP_REACH = new BlockPos(0, 2, 4);

  @BeforeBatch(batchId = BATCH)
  public void clearFog(final ServerWorld world) {
    FogClouds.clearAll();
  }

  private static void end(final TestContext context, final Entity... owners) {
    for (final Entity owner : owners) {
      GhostlyFamiliars.owned(context.getWorld(), owner.getUuid()).forEach(Entity::discard);
      if (owner instanceof ServerPlayerEntity player) {
        TestPlayers.remove(context, player);
      }
    }
    context.complete();
  }

  private static List<GhostlyFamiliarEntity> familiarsOf(
      final TestContext context, final ServerPlayerEntity owner) {
    return GhostlyFamiliars.owned(context.getWorld(), owner.getUuid());
  }

  private static GhostlyFamiliarEntity familiar(
      final TestContext context, final ServerPlayerEntity owner, final FamiliarPower power) {
    final GhostlyFamiliarEntity familiar =
        context.spawnEntity(GhostlyFamiliars.GHOSTLY_FAMILIAR, new BlockPos(4, 2, 4));
    familiar.summon(owner.getUuid(), power, power.lifetimeTicks());
    familiar.setAiDisabled(true);
    return familiar;
  }

  private static FamiliarPower power(final int lifetime) {
    return new FamiliarPower(lifetime, 10.0F, 3.0F, 24.0F, 200, 8.0F, 200, 9);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void summoningCallsAFamiliarThatServesTheCaster(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    OccultSkills.learn(vampire, OccultSkills.SUMMON_FAMILIAR);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertEquals(
              Powers.cast(vampire, OccultSkills.SUMMON_FAMILIAR),
              PowerUse.Verdict.READY,
              "the summon was cast");
          final List<GhostlyFamiliarEntity> familiars = familiarsOf(context, vampire);
          context.assertEquals(familiars.size(), 1, "one familiar answered");
          context.assertTrue(familiars.get(0).isOwnedBy(vampire), "it serves the caster");
          context.assertEquals(BloodAttachment.get(vampire), 16, "the summon cost four blood");
          end(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void summoningPastTheLimitSendsTheOldestAway(final TestContext context) {
    final ServerPlayerEntity single = TestPlayers.vampire(context, new BlockPos(2, 2, 2));
    final ServerPlayerEntity pact = TestPlayers.vampire(context, new BlockPos(2, 2, 6));
    OccultSkills.learn(single, OccultSkills.SUMMON_FAMILIAR);
    OccultSkills.learn(pact, OccultSkills.SUMMON_FAMILIAR, OccultSkills.SPIRIT_PACT);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          for (final ServerPlayerEntity vampire : List.of(single, pact)) {
            for (int i = 0; i < 3; i++) {
              PowerCooldownAttachment.set(vampire, PowerCooldowns.NONE);
              BloodAttachment.set(vampire, 20);
              Powers.cast(vampire, OccultSkills.SUMMON_FAMILIAR);
            }
          }
          context.assertEquals(
              familiarsOf(context, single).size(), 1, "one familiar without a pact");
          context.assertEquals(familiarsOf(context, pact).size(), 2, "two familiars with a pact");
          end(context, single, pact);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void aFamiliarPassesThroughWallsOnlyWhileScouting(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    final GhostlyFamiliarEntity familiar = familiar(context, vampire, power(1200));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertFalse(familiar.noClip, "a fighting familiar is solid");
          familiar.order(FamiliarMode.SCOUT, vampire);
        });
    context.runAtTick(
        TestPlayers.READY + 2,
        () -> {
          context.assertTrue(familiar.noClip, "a scouting familiar passes through blocks");
          familiar.order(FamiliarMode.CARRY, vampire);
        });
    context.runAtTick(
        TestPlayers.READY + 4,
        () -> {
          context.assertFalse(familiar.noClip, "a carrying familiar is solid again");
          end(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void onlyTheOwnerOrdersAFamiliarAndHandsItThings(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    final ServerPlayerEntity stranger = TestPlayers.human(context, new BlockPos(6, 2, 4));
    final GhostlyFamiliarEntity familiar = familiar(context, vampire, power(1200));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          familiar.interact(stranger, Hand.MAIN_HAND);
          context.assertFalse(
              WheelUse.pick(
                  stranger, familiar, FamiliarModeOrders.ID, FamiliarMode.CARRY.ordinal()),
              "a stranger gets no wheel for it");
          context.assertEquals(familiar.mode(), FamiliarMode.FIGHT, "a stranger cannot order it");
          context.assertTrue(
              WheelUse.pick(vampire, familiar, FamiliarModeOrders.ID, FamiliarMode.CARRY.ordinal()),
              "its owner picks carry on the wheel");
          context.assertEquals(familiar.mode(), FamiliarMode.CARRY, "its owner sets it carrying");
          vampire.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BREAD, 5));
          familiar.interact(vampire, Hand.MAIN_HAND);
          context.assertTrue(
              vampire.getMainHandStack().isEmpty(), "the owner handed over the bread");
          context.assertEquals(familiar.carried().count(Items.BREAD), 5, "the familiar holds it");
          vampire.setSneaking(true);
          familiar.interact(vampire, Hand.MAIN_HAND);
          context.assertTrue(familiar.carried().isEmpty(), "a sneaking owner takes it back");
          context.assertEquals(vampire.getInventory().count(Items.BREAD), 5, "nothing was lost");
          end(context, vampire, stranger);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + SHORT_LIFE + 10)
  public void aFamiliarFadesWhenItsTimeIsUpAndDropsWhatItCarried(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, BEYOND_PICKUP_REACH);
    final GhostlyFamiliarEntity[] familiar = new GhostlyFamiliarEntity[1];
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          familiar[0] = familiar(context, vampire, power(SHORT_LIFE));
          familiar[0].carried().addStack(new ItemStack(Items.EMERALD, 3));
        });
    context.runAtTick(
        TestPlayers.READY + SHORT_LIFE + 5,
        () -> {
          context.assertTrue(familiar[0].isRemoved(), "the familiar faded");
          final Vec3d at = context.getAbsolute(Vec3d.ofBottomCenter(new BlockPos(4, 2, 4)));
          final int dropped =
              context
                  .getWorld()
                  .getEntitiesByClass(ItemEntity.class, new Box(at, at).expand(3.0), item -> true)
                  .stream()
                  .filter(item -> item.getStack().isOf(Items.EMERALD))
                  .mapToInt(item -> item.getStack().getCount())
                  .sum();
          context.assertEquals(dropped, 3, "what it carried fell where it faded");
          end(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 15)
  public void graveFogBlindsOthersButNotTheCaster(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(6, 2, 4));
    zombie.setAiDisabled(true);
    OccultSkills.learn(vampire, OccultSkills.GRAVE_FOG);
    context.runAtTick(
        TestPlayers.READY,
        () ->
            context.assertEquals(
                Powers.cast(vampire, OccultSkills.GRAVE_FOG),
                PowerUse.Verdict.READY,
                "the fog was breathed"));
    context.runAtTick(
        TestPlayers.READY + 12,
        () -> {
          context.assertTrue(
              zombie.hasStatusEffect(StatusEffects.BLINDNESS), "the zombie in the fog is blind");
          context.assertFalse(
              vampire.hasStatusEffect(StatusEffects.BLINDNESS), "the caster sees through it");
          end(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void shadowStepCarriesTheCasterToWhereTheyLook(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(1, 2, 4));
    context.setBlockState(new BlockPos(8, 2, 4), Blocks.STONE);
    context.setBlockState(new BlockPos(8, 3, 4), Blocks.STONE);
    OccultSkills.learn(vampire, OccultSkills.GRAVE_FOG, OccultSkills.SHADOW_STEP);
    final double startX = context.getAbsolute(Vec3d.ofBottomCenter(new BlockPos(1, 2, 4))).x;
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.setYaw(-90.0F);
          vampire.setPitch(0.0F);
          context.assertEquals(
              Powers.cast(vampire, OccultSkills.SHADOW_STEP),
              PowerUse.Verdict.READY,
              "the step was taken");
          context.assertTrue(
              vampire.getX() - startX >= 4.0,
              "the caster stepped well forward, not " + (vampire.getX() - startX));
          context.assertTrue(
              vampire.getX() - startX <= 7.0, "the caster stopped short of the wall");
          end(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aVampireMobSummonsThroughTheSamePower(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
    vampire.setAiDisabled(true);
    OccultSkills.learn(vampire, OccultSkills.SUMMON_FAMILIAR);
    context.runAtTick(
        5,
        () -> {
          context.assertEquals(
              Powers.cast(vampire, OccultSkills.SUMMON_FAMILIAR),
              PowerUse.Verdict.READY,
              "the mob cast the summon");
          context.assertEquals(
              GhostlyFamiliars.owned(context.getWorld(), vampire.getUuid()).size(),
              1,
              "a familiar answered the mob");
          end(context, vampire);
        });
  }

  private static void encase(final TestContext context, final BlockPos at) {
    for (final BlockPos pos : BlockPos.iterate(at.add(-1, 0, -1), at.add(1, 1, 1))) {
      context.setBlockState(pos, Blocks.STONE);
    }
  }

  private static void runOutScout(final GhostlyFamiliarEntity familiar) {
    final FamiliarScoutGoal goal = new FamiliarScoutGoal(familiar);
    goal.start();
    for (int i = 0; i < SCOUT_TICKS * 2 && familiar.mode() == FamiliarMode.SCOUT; i++) {
      goal.tick();
    }
  }

  private static FamiliarPower shortScout() {
    return new FamiliarPower(1200, 10.0F, 3.0F, 24.0F, SCOUT_TICKS, 8.0F, 200, 9);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void aScoutThatEndsInsideAWallRejoinsItsOwner(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(1, 2, 4));
    encase(context, new BlockPos(6, 2, 4));
    final GhostlyFamiliarEntity familiar =
        context.spawnEntity(GhostlyFamiliars.GHOSTLY_FAMILIAR, new BlockPos(6, 2, 4));
    familiar.summon(vampire.getUuid(), shortScout(), shortScout().lifetimeTicks());
    familiar.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          familiar.order(FamiliarMode.SCOUT, vampire);
          context.assertTrue(familiar.buried(), "the scout starts out inside the stone");
          runOutScout(familiar);
          context.assertEquals(familiar.mode(), FamiliarMode.FIGHT, "the scout is over");
          context.assertFalse(familiar.buried(), "it is out of the stone");
          context.assertTrue(familiar.distanceTo(vampire) < 2.0, "it rejoined its owner");
          end(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aScoutWithNoOwnerRisesOutOfTheStone(final TestContext context) {
    encase(context, new BlockPos(4, 2, 4));
    final GhostlyFamiliarEntity familiar =
        context.spawnEntity(GhostlyFamiliars.GHOSTLY_FAMILIAR, new BlockPos(4, 2, 4));
    familiar.summon(UUID.randomUUID(), shortScout(), shortScout().lifetimeTicks());
    familiar.setAiDisabled(true);
    context.runAtTick(
        5,
        () -> {
          familiar.order(FamiliarMode.SCOUT, null);
          context.assertTrue(familiar.buried(), "the scout starts out inside the stone");
          runOutScout(familiar);
          context.assertEquals(familiar.mode(), FamiliarMode.FIGHT, "the scout is over");
          context.assertFalse(familiar.buried(), "it rose clear of the stone");
          familiar.discard();
          context.complete();
        });
  }

  private static void stepOnto(
      final TestContext context, final BlockState floor, final int level, final double rise) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(1, 2, 4));
    for (int x = 3; x <= 7; x++) {
      context.setBlockState(new BlockPos(x, level, 4), floor);
    }
    context.setBlockState(new BlockPos(8, 2, 4), Blocks.STONE);
    context.setBlockState(new BlockPos(8, 3, 4), Blocks.STONE);
    OccultSkills.learn(vampire, OccultSkills.GRAVE_FOG, OccultSkills.SHADOW_STEP);
    final Vec3d start = context.getAbsolute(Vec3d.ofBottomCenter(new BlockPos(1, 2, 4)));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.setYaw(-90.0F);
          vampire.setPitch(0.0F);
          context.assertEquals(
              Powers.cast(vampire, OccultSkills.SHADOW_STEP),
              PowerUse.Verdict.READY,
              "the step was taken");
          context.assertTrue(
              vampire.getX() - start.x >= 4.0, "the caster stepped across the floor");
          context.assertTrue(
              Math.abs(vampire.getY() - (start.y + rise)) < 0.01,
              "the caster stands on top of the floor, at " + (vampire.getY() - start.y));
          end(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void shadowStepStopsShortOfAMagmaFloor(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(1, 2, 4));
    for (int x = 5; x <= 7; x++) {
      context.setBlockState(new BlockPos(x, 1, 4), Blocks.MAGMA_BLOCK);
    }
    context.setBlockState(new BlockPos(8, 2, 4), Blocks.STONE);
    context.setBlockState(new BlockPos(8, 3, 4), Blocks.STONE);
    OccultSkills.learn(vampire, OccultSkills.GRAVE_FOG, OccultSkills.SHADOW_STEP);
    final double startX = context.getAbsolute(Vec3d.ofBottomCenter(new BlockPos(1, 2, 4))).x;
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.setYaw(-90.0F);
          vampire.setPitch(0.0F);
          context.assertEquals(
              Powers.cast(vampire, OccultSkills.SHADOW_STEP),
              PowerUse.Verdict.READY,
              "the step was taken");
          final double east = vampire.getX() - startX;
          context.assertTrue(east >= 1.99, "the caster still stepped forward, by " + east);
          context.assertTrue(east <= 3.01, "the caster stopped before the magma, at " + east);
          end(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void shadowStepLandsOnAGlassFloor(final TestContext context) {
    stepOnto(context, Blocks.GLASS.getDefaultState(), 1, 0.0);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void shadowStepLandsOnACarpet(final TestContext context) {
    stepOnto(context, Blocks.RED_CARPET.getDefaultState(), 2, 1.0 / 16.0);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void shadowStepLandsOnASlab(final TestContext context) {
    stepOnto(context, Blocks.STONE_BRICK_SLAB.getDefaultState(), 1, -0.5);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void aScoutFliesOutAheadAndComesBack(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(1, 2, 4));
    final GhostlyFamiliarEntity familiar =
        context.spawnEntity(GhostlyFamiliars.GHOSTLY_FAMILIAR, new BlockPos(2, 2, 4));
    final FamiliarPower scout = new FamiliarPower(1200, 10.0F, 3.0F, 4.0F, 200, 8.0F, 200, 9);
    familiar.summon(vampire.getUuid(), scout, scout.lifetimeTicks());
    familiar.setAiDisabled(true);
    final FamiliarScoutGoal goal = new FamiliarScoutGoal(familiar);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.setYaw(-90.0F);
          vampire.setPitch(0.0F);
          context.assertFalse(goal.canStart(), "a familiar that was not sent out does not scout");
          familiar.order(FamiliarMode.SCOUT, vampire);
          context.assertTrue(goal.canStart(), "a familiar sent out starts scouting");
          final Vec3d ahead = familiar.scoutTarget();
          context.assertTrue(
              ahead.x - vampire.getX() >= 3.0, "the scout is sent out ahead of its owner");
          goal.start();
          goal.tick();
          context.assertTrue(heading(familiar).distanceTo(ahead) < 0.01, "it flies out ahead");
          familiar.setPosition(ahead);
          goal.tick();
          context.assertEquals(familiar.mode(), FamiliarMode.SCOUT, "at its range it is not done");
          context.assertTrue(
              heading(familiar)
                      .distanceTo(vampire.getPos().add(0.0, vampire.getHeight() * 0.6, 0.0))
                  < 0.01,
              "from its range it turns back to its owner");
          familiar.setPosition(vampire.getPos());
          goal.tick();
          context.assertEquals(familiar.mode(), FamiliarMode.FIGHT, "back home the scout is over");
          end(context, vampire);
        });
  }

  private static Vec3d heading(final GhostlyFamiliarEntity familiar) {
    return new Vec3d(
        familiar.getMoveControl().getTargetX(),
        familiar.getMoveControl().getTargetY(),
        familiar.getMoveControl().getTargetZ());
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 10)
  public void aSecondSummonComesBeforeTheFirstFamiliarFades(final TestContext context) {
    final SkillPower summon =
        Skills.skills(context.getWorld().getRegistryManager())
            .map(registry -> registry.get(OccultSkills.SUMMON_FAMILIAR))
            .flatMap(Skill::power)
            .orElseThrow();
    final int lifetime = summon.familiar().orElseThrow().lifetimeTicks();
    context.assertTrue(
        summon.cooldownTicks() * 2 <= lifetime,
        "the summon is ready again well inside a familiar's lifetime, so Spirit Pact keeps two: "
            + summon.cooldownTicks()
            + " of "
            + lifetime);
    context.complete();
  }
}
