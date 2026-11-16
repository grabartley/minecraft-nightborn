package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.occult.FamiliarMode;
import com.grahambartley.nightborn.occult.FamiliarPower;
import com.grahambartley.nightborn.occult.FogClouds;
import com.grahambartley.nightborn.occult.GhostlyFamiliarEntity;
import com.grahambartley.nightborn.occult.GhostlyFamiliars;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerCooldowns;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
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

  @BeforeBatch(batchId = BATCH)
  public void clearFog(final ServerWorld world) {
    FogClouds.clearAll();
  }

  private static List<GhostlyFamiliarEntity> familiarsOf(
      final TestContext context, final ServerPlayerEntity owner) {
    return GhostlyFamiliars.owned(context.getWorld(), owner.getUuid());
  }

  private static GhostlyFamiliarEntity familiar(
      final TestContext context, final ServerPlayerEntity owner, final FamiliarPower power) {
    final GhostlyFamiliarEntity familiar =
        context.spawnEntity(GhostlyFamiliars.GHOSTLY_FAMILIAR, new BlockPos(4, 2, 4));
    familiar.summon(owner.getUuid(), power);
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
          TestPlayers.finish(context, vampire);
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
          TestPlayers.finish(context, single, pact);
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
          TestPlayers.finish(context, vampire);
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
          context.assertEquals(familiar.mode(), FamiliarMode.FIGHT, "a stranger cannot order it");
          familiar.interact(vampire, Hand.MAIN_HAND);
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
          TestPlayers.finish(context, vampire, stranger);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + SHORT_LIFE + 10)
  public void aFamiliarFadesWhenItsTimeIsUpAndDropsWhatItCarried(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
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
          TestPlayers.finish(context, vampire);
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
          TestPlayers.finish(context, vampire);
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
          TestPlayers.finish(context, vampire);
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
          context.complete();
        });
  }
}
