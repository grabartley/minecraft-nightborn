package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.config.FeedingSettings;
import com.grahambartley.nightborn.config.SkillSettings;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillShop;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.weakened.Weakened;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class SkillGameTest implements FabricGameTest {

  static final String BATCH = "skills";
  static final String ARENA = "nightborn:vampire_arena";
  static final Identifier THICK_BLOOD = Identifier.of("nightborn", "core/thick_blood");
  static final Identifier BLOOD_RUSH = Identifier.of("nightborn", "core/blood_rush");
  static final Identifier NOBLE = Identifier.of("nightborn", "noble/noble_blood");
  static final Identifier BEAST = Identifier.of("nightborn", "beast/beast_blood");
  static final Identifier OCCULT = Identifier.of("nightborn", "occult/occult_blood");

  @BeforeBatch(batchId = BATCH)
  public void onePointPerBlood(final ServerWorld world) {
    FeedingSessions.clearAll();
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(FeedingSettings.RESIST_CHANCE, 0)
            .with(SkillSettings.BLOOD_PER_POINT, 1));
  }

  @AfterBatch(batchId = BATCH)
  public void restoreSettings(final ServerWorld world) {
    FeedingSessions.clearAll();
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(FeedingSettings.RESIST_CHANCE, FeedingSettings.RESIST_CHANCE.defaultValue())
            .with(SkillSettings.BLOOD_PER_POINT, SkillSettings.BLOOD_PER_POINT.defaultValue()));
  }

  @GameTest(templateName = EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void theShippedTreeLoadsAsData(final TestContext context) {
    final var registries = context.getWorld().getRegistryManager();
    for (final Identifier id : new Identifier[] {THICK_BLOOD, BLOOD_RUSH, NOBLE, BEAST, OCCULT}) {
      context.assertTrue(Skills.skill(registries, id) != null, "skill " + id + " loads");
    }
    context.assertTrue(
        Skills.branches(registries).orElseThrow().stream().filter(b -> b.bloodline()).count() == 3,
        "three bloodlines load");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void theServerRefusesAThirdBloodline(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    SkillAttachment.set(vampire, SkillTree.VAMPIRE, TreeProgress.EMPTY.withEarned(10));
    context.assertEquals(SkillShop.buy(vampire, NOBLE), SkillPurchase.Verdict.ALLOWED, "first");
    context.assertEquals(SkillShop.buy(vampire, BEAST), SkillPurchase.Verdict.ALLOWED, "second");
    context.assertEquals(
        SkillShop.buy(vampire, OCCULT),
        SkillPurchase.Verdict.BLOODLINE_LOCKED,
        "a third bloodline is refused");
    final TreeProgress after = SkillAttachment.get(vampire, SkillTree.VAMPIRE);
    context.assertFalse(after.owns(OCCULT), "the third bloodline skill is not owned");
    context.assertEquals(after.free(), 8, "the refused purchase cost nothing");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aHumanCannotBuyAVampireSkillWhateverItSends(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(3, 2, 4));
    SkillAttachment.set(human, SkillTree.VAMPIRE, TreeProgress.EMPTY.withEarned(10));
    context.assertEquals(
        SkillShop.buy(human, THICK_BLOOD), SkillPurchase.Verdict.WRONG_TREE, "wrong tree");
    context.assertEquals(
        SkillShop.buy(human, Identifier.of("nightborn", "made/up")),
        SkillPurchase.Verdict.UNKNOWN,
        "an invented skill is refused");
    context.assertTrue(
        SkillAttachment.get(human, SkillTree.VAMPIRE).bought().isEmpty(), "nothing was bought");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aBoughtSkillChangesThePlayerAndSurvivesDeath(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final float before = vampire.getMaxHealth();
    SkillAttachment.set(vampire, SkillTree.VAMPIRE, TreeProgress.EMPTY.withEarned(1));
    SkillShop.buy(vampire, THICK_BLOOD);
    context.assertEquals(vampire.getMaxHealth(), before + 4.0F, "Thick Blood adds two hearts");
    final ServerPlayerEntity respawned =
        context
            .getWorld()
            .getServer()
            .getPlayerManager()
            .respawnPlayer(vampire, false, Entity.RemovalReason.KILLED);
    context.assertTrue(
        SkillAttachment.get(respawned, SkillTree.VAMPIRE).owns(THICK_BLOOD),
        "the skill is kept through death");
    context.assertTrue(
        respawned
                .getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                .getModifier(Identifier.of("nightborn", "skill/core/thick_blood/0"))
            != null,
        "and still applies after respawn");
    TestPlayers.finish(context, respawned);
  }

  @GameTest(templateName = EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void boughtSkillsSurviveSaveAndLoad(final TestContext context) {
    final ServerPlayerEntity saved = context.createMockCreativeServerPlayerInWorld();
    final TreeProgress progress =
        TreeProgress.EMPTY.withEarned(5).buy(NOBLE, 1, Identifier.of("nightborn", "noble"));
    SkillAttachment.set(saved, SkillTree.VAMPIRE, progress);
    final NbtCompound nbt = saved.writeNbt(new NbtCompound());
    SkillAttachment.clear(saved);
    saved.readNbt(nbt);
    context.assertEquals(SkillAttachment.get(saved, SkillTree.VAMPIRE), progress, "after load");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 100)
  public void sippingEarnsPointsByBloodQuality(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    BloodAttachment.set(vampire, 5);
    final CowEntity cow = still(context.spawnEntity(EntityType.COW, new BlockPos(3, 2, 4)));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() <= 65) {
            hold(context, vampire, cow);
          }
        });
    context.runAtTick(
        85,
        () -> {
          context.assertTrue(cow.isAlive(), "the cow was only sipped");
          context.assertEquals(
              SkillAttachment.get(vampire, SkillTree.VAMPIRE).earned(),
              3,
              "three sips of animal blood at half worth give three points");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 80)
  public void drainingEarnsTheBonus(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    BloodAttachment.set(vampire, 5);
    final VillagerEntity victim =
        still(context.spawnEntity(EntityType.VILLAGER, new BlockPos(3, 2, 4)));
    victim.setHealth(3.0F);
    context.runAtEveryTick(
        () -> {
          if (victim.isAlive()) {
            hold(context, vampire, victim);
          }
        });
    context.runAtTick(
        70,
        () -> {
          context.assertFalse(victim.isAlive(), "the villager was drained");
          context.assertEquals(
              SkillAttachment.get(vampire, SkillTree.VAMPIRE).earned(),
              3 + SkillSettings.DRAIN_BONUS_POINTS.defaultValue(),
              "three villager blood plus the drain bonus");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void bloodRushCostsBloodAndCoolsDown(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    SkillAttachment.set(
        vampire, SkillTree.VAMPIRE, TreeProgress.EMPTY.withEarned(3).buy(THICK_BLOOD, 1, null));
    SkillShop.buy(vampire, BLOOD_RUSH);
    BloodAttachment.set(vampire, 10);
    context.assertEquals(Powers.use(vampire, 0), PowerUse.Verdict.READY, "first use");
    context.assertEquals(BloodAttachment.get(vampire), 7, "Blood Rush costs three blood");
    context.assertTrue(vampire.hasStatusEffect(StatusEffects.SPEED), "it gives speed");
    context.assertTrue(vampire.hasStatusEffect(StatusEffects.STRENGTH), "and strength");
    context.assertEquals(Powers.use(vampire, 0), PowerUse.Verdict.COOLING, "second use too soon");
    context.assertEquals(BloodAttachment.get(vampire), 7, "a refused use costs nothing");
    context.assertTrue(
        PowerCooldownAttachment.get(vampire).ticksLeft(BLOOD_RUSH, context.getWorld().getTime())
            > 0,
        "the cooldown is running");
    context.assertEquals(Powers.use(vampire, 1), PowerUse.Verdict.NO_POWER, "empty slot");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aWeakenedVampireCannotUsePowers(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    SkillAttachment.set(
        vampire,
        SkillTree.VAMPIRE,
        TreeProgress.EMPTY.withEarned(3).buy(THICK_BLOOD, 1, null).buy(BLOOD_RUSH, 2, null));
    Weakened.add(vampire, "test", 50, 200);
    context.runAtTick(
        3,
        () -> {
          context.assertEquals(Powers.use(vampire, 0), PowerUse.Verdict.WEAKENED, "refused");
          context.assertFalse(vampire.hasStatusEffect(StatusEffects.SPEED), "no effect given");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void anOperatorCanSetPointsAndRefundSkills(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final var server = context.getWorld().getServer();
    final var source = server.getCommandSource().withLevel(4).withSilent();
    final String name = vampire.getGameProfile().getName();
    server
        .getCommandManager()
        .executeWithPrefix(source, "nightborn player " + name + " skills vampire points 4");
    context.assertEquals(
        SkillAttachment.get(vampire, SkillTree.VAMPIRE).earned(), 4, "points are set");
    SkillShop.buy(vampire, THICK_BLOOD);
    SkillShop.buy(vampire, NOBLE);
    server
        .getCommandManager()
        .executeWithPrefix(source, "nightborn player " + name + " skills vampire reset");
    final TreeProgress after = SkillAttachment.get(vampire, SkillTree.VAMPIRE);
    context.assertTrue(after.bought().isEmpty(), "skills are refunded");
    context.assertTrue(after.bloodlines().isEmpty(), "bloodlines are reset");
    context.assertEquals(after.free(), 4, "every spent point comes back");
    context.assertTrue(
        vampire
                .getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                .getModifier(Identifier.of("nightborn", "skill/core/thick_blood/0"))
            == null,
        "a refunded skill stops applying");
    TestPlayers.finish(context, vampire);
  }

  private static <E extends MobEntity> E still(final E mob) {
    mob.setAiDisabled(true);
    return mob;
  }

  private static void hold(
      final TestContext context, final ServerPlayerEntity vampire, final LivingEntity victim) {
    vampire.setSneaking(true);
    UseEntityCallback.EVENT
        .invoker()
        .interact(vampire, context.getWorld(), Hand.MAIN_HAND, victim, null);
  }
}
