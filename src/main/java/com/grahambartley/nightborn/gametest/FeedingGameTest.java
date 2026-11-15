package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.config.FeedingSettings;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class FeedingGameTest implements FabricGameTest {

  static final String BATCH = "feeding";
  static final String RESISTING = "feeding-resisting";
  static final String ARENA = "nightborn:vampire_arena";
  static final double WALK_STEP = 0.15;

  @BeforeBatch(batchId = BATCH)
  public void neverResist(final ServerWorld world) {
    FeedingSessions.clearAll();
    ServerConfigService.update(
        world.getServer(), ServerConfigService.current().with(FeedingSettings.RESIST_CHANCE, 0));
  }

  @AfterBatch(batchId = BATCH)
  public void restoreResist(final ServerWorld world) {
    FeedingSessions.clearAll();
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(FeedingSettings.RESIST_CHANCE, FeedingSettings.RESIST_CHANCE.defaultValue()));
  }

  @BeforeBatch(batchId = RESISTING)
  public void alwaysResist(final ServerWorld world) {
    FeedingSessions.clearAll();
    ServerConfigService.update(
        world.getServer(), ServerConfigService.current().with(FeedingSettings.RESIST_CHANCE, 100));
  }

  @AfterBatch(batchId = RESISTING)
  public void restoreResistAfterResisting(final ServerWorld world) {
    restoreResist(world);
  }

  @GameTest(templateName = ARENA, batchId = RESISTING, tickLimit = 60)
  public void aVictimThatBreaksFreeIsNotHeldAnyMore(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    final CowEntity cow = still(context.spawnEntity(EntityType.COW, new BlockPos(3, 2, 4)));
    cow.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(200.0);
    cow.setHealth(200.0F);
    context.runAtEveryTick(
        () -> {
          if (context.getTick() <= 30) {
            hold(context, vampire, cow);
          }
        });
    context.runAtTick(
        15,
        () ->
            context.assertTrue(
                cow.hasStatusEffect(StatusEffects.SLOWNESS), "the cow is held before it resists"));
    context.runAtTick(
        36,
        () -> {
          context.assertFalse(FeedingSessions.isFeeding(vampire), "the cow broke free");
          context.assertFalse(
              cow.hasStatusEffect(StatusEffects.SLOWNESS), "a cow that broke free is not held");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 100)
  public void biteOnACowFillsTheBloodBar(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    BloodAttachment.set(vampire, 10);
    final CowEntity cow = still(context.spawnEntity(EntityType.COW, new BlockPos(3, 2, 4)));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() <= 65) {
            hold(context, vampire, cow);
          }
        });
    context.runAtTick(
        66,
        () -> {
          context.assertEquals(BloodAttachment.get(vampire), 13, "blood after three sips of cow");
          context.assertEquals(cow.getHealth(), 4.0F, "cow health after three sips");
          context.assertTrue(
              TestAdvancements.done(context, vampire, "first_blood"), "drinking earns First Blood");
          context.assertFalse(
              TestAdvancements.done(context, vampire, "fresh_off_the_neck"),
              "a cow is not a villager");
          context.assertTrue(
              VisibleStateAttachment.get(vampire).feeding(),
              "the vampire's synced state marks it feeding");
        });
    context.runAtTick(
        85,
        () -> {
          context.assertTrue(cow.isAlive(), "letting go leaves the cow alive");
          context.assertTrue(
              cow.hasStatusEffect(StatusEffects.WEAKNESS), "a sipped cow is left weak");
          context.assertFalse(
              VisibleStateAttachment.get(vampire).feeding(), "letting go ends the feeding pose");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 70)
  public void aBittenCowIsHeldInPlaceAndFreeAgainAfterTheBite(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    BloodAttachment.set(vampire, 10);
    final CowEntity held = still(context.spawnEntity(EntityType.COW, new BlockPos(3, 2, 4)));
    final CowEntity free = still(context.spawnEntity(EntityType.COW, new BlockPos(1, 2, 1)));
    final Vec3d[] start = new Vec3d[2];
    context.runAtTick(
        5,
        () -> {
          start[0] = held.getPos();
          start[1] = free.getPos();
        });
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= 5 && context.getTick() <= 45) {
            hold(context, vampire, held);
            walk(held);
            walk(free);
          }
        });
    context.runAtTick(
        46,
        () -> {
          context.assertTrue(
              held.getPos().distanceTo(start[0]) < 0.4,
              "the held cow was walking but moved " + held.getPos().distanceTo(start[0]));
          context.assertTrue(
              free.getPos().distanceTo(start[1]) > 3.0,
              "the same walk takes a free cow away, it moved "
                  + free.getPos().distanceTo(start[1]));
        });
    context.runAtTick(
        60,
        () -> {
          context.assertFalse(
              held.hasStatusEffect(StatusEffects.SLOWNESS), "the hold is gone after the bite");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 80)
  public void aBittenBatStaysWithinReach(final TestContext context) {
    heldFlyerStaysWithinReach(context, EntityType.BAT);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 80)
  public void aBittenParrotStaysWithinReach(final TestContext context) {
    heldFlyerStaysWithinReach(context, EntityType.PARROT);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 70)
  public void theHoldEndsWhenTheVampireLeavesMidBite(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    final CowEntity cow = still(context.spawnEntity(EntityType.COW, new BlockPos(3, 2, 4)));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() < 30) {
            hold(context, vampire, cow);
          }
        });
    context.runAtTick(
        29,
        () ->
            context.assertTrue(
                cow.hasStatusEffect(StatusEffects.SLOWNESS), "the cow is held mid-bite"));
    context.runAtTick(
        30,
        () -> {
          FeedingSessions.end(vampire);
          context.assertFalse(FeedingSessions.isFeeding(vampire), "the bite is over");
        });
    context.runAtTick(
        35,
        () -> {
          context.assertFalse(
              cow.hasStatusEffect(StatusEffects.SLOWNESS), "nothing holds the cow any more");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 60)
  public void aBittenRiderIsLiftedOffItsVehicle(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    final BoatEntity boat = context.spawnEntity(EntityType.BOAT, new BlockPos(3, 2, 4));
    final CowEntity cow = still(context.spawnEntity(EntityType.COW, new BlockPos(3, 2, 4)));
    cow.startRiding(boat, true);
    context.runAtTick(2, () -> context.assertTrue(cow.hasVehicle(), "the cow rides the boat"));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= 3 && context.getTick() <= 20) {
            hold(context, vampire, cow);
          }
        });
    context.runAtTick(
        25,
        () -> {
          context.assertFalse(cow.hasVehicle(), "a bitten cow is lifted off its boat");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 80)
  public void twoVampiresNeverTakeMoreThanTheVictimHas(final TestContext context) {
    final ServerPlayerEntity first = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    final ServerPlayerEntity second = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    BloodAttachment.set(first, 10);
    BloodAttachment.set(second, 10);
    final VillagerEntity victim =
        still(context.spawnEntity(EntityType.VILLAGER, new BlockPos(3, 2, 4)));
    victim.setHealth(3.0F);
    context.runAtEveryTick(
        () -> {
          if (victim.isAlive()) {
            hold(context, first, victim);
            hold(context, second, victim);
          }
        });
    context.runAtTick(
        60,
        () -> {
          context.assertFalse(victim.isAlive(), "holding on past the last health drains it");
          final int gained = BloodAttachment.get(first) - 10 + BloodAttachment.get(second) - 10;
          context.assertTrue(gained <= 3, "two vampires took " + gained + " from a victim with 3");
          TestPlayers.finish(context, first, second);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 140)
  public void bottlingConservesBottlesAndNeverCreatesBlood(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    BloodAttachment.set(vampire, 10);
    vampire.setStackInHand(Hand.OFF_HAND, new ItemStack(Items.GLASS_BOTTLE, 3));
    final VillagerEntity victim =
        still(context.spawnEntity(EntityType.VILLAGER, new BlockPos(3, 2, 4)));
    victim.setHealth(9.0F);
    context.runAtEveryTick(
        () -> {
          if (victim.isAlive()) {
            hold(context, vampire, victim);
          }
        });
    context.runAtTick(
        125,
        () -> {
          context.assertFalse(
              victim.isAlive(),
              "the villager was drained: health "
                  + victim.getHealth()
                  + ", feeding "
                  + FeedingSessions.isFeeding(vampire)
                  + ", "
                  + Diagnostics.of(vampire));
          final int glass = vampire.getInventory().count(Items.GLASS_BOTTLE);
          final int blood = vampire.getInventory().count(BloodItems.BLOOD_BOTTLE);
          context.assertEquals(glass + blood, 3, "bottles before and after");
          context.assertEquals(blood, 2, "nine blood fills two bottles of four");
          for (int slot = 0; slot < vampire.getInventory().size(); slot++) {
            final ItemStack stack = vampire.getInventory().getStack(slot);
            if (stack.isOf(BloodItems.BLOOD_BOTTLE)) {
              context.assertEquals(
                  BloodBottleItem.qualityOf(stack), BloodQuality.VILLAGER, "bottle quality");
            }
          }
          context.assertEquals(
              BloodAttachment.get(vampire), 11, "the one blood left over is drunk fresh");
          context.assertTrue(
              TestAdvancements.done(context, vampire, "fresh_off_the_neck"),
              "drinking from a villager earns Fresh Off The Neck");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 60)
  public void onlyAVillagerThatSeesTheBiteRaisesTheAlarm(final TestContext context) {
    for (int y = 2; y <= 3; y++) {
      for (int z = 0; z <= 8; z++) {
        context.setBlockState(new BlockPos(1, y, z), Blocks.BEDROCK);
      }
    }
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    final CowEntity cow = still(context.spawnEntity(EntityType.COW, new BlockPos(5, 2, 4)));
    final VillagerEntity witness =
        still(context.spawnEntity(EntityType.VILLAGER, new BlockPos(7, 2, 4)));
    final VillagerEntity hidden =
        still(context.spawnEntity(EntityType.VILLAGER, new BlockPos(0, 2, 4)));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() <= 30) {
            hold(context, vampire, cow);
          }
        });
    context.runAtTick(
        30,
        () -> {
          context.assertTrue(
              witness
                  .getBrain()
                  .getOptionalRegisteredMemory(MemoryModuleType.HURT_BY_ENTITY)
                  .isPresent(),
              "the villager that saw the bite panics: cow "
                  + cow.getHealth()
                  + ", sees "
                  + witness.canSee(vampire)
                  + ", "
                  + Diagnostics.of(vampire));
          context.assertTrue(witness.getReputation(vampire) < 0, "the witness gossips");
          context.assertTrue(
              TestAdvancements.done(context, vampire, "bad_taste"),
              "feeding in sight of a villager earns Bad Taste");
          context.assertFalse(
              hidden
                  .getBrain()
                  .getOptionalRegisteredMemory(MemoryModuleType.HURT_BY_ENTITY)
                  .isPresent(),
              "the villager behind the wall acts as normal");
          context.assertEquals(
              hidden.getReputation(vampire), 0, "the hidden villager does not gossip");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 40)
  public void aHumanCannotBite(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    final CowEntity cow = still(context.spawnEntity(EntityType.COW, new BlockPos(3, 2, 4)));
    context.runAtEveryTick(() -> hold(context, human, cow));
    context.runAtTick(
        30,
        () -> {
          context.assertEquals(cow.getHealth(), cow.getMaxHealth(), "a human's bite does nothing");
          context.assertFalse(FeedingSessions.isFeeding(human), "a human never feeds");
          TestPlayers.finish(context, human);
        });
  }

  private void heldFlyerStaysWithinReach(
      final TestContext context, final EntityType<? extends MobEntity> type) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    final MobEntity held = still(context.spawnEntity(type, new BlockPos(3, 2, 4)));
    final MobEntity free = still(context.spawnEntity(type, new BlockPos(1, 2, 1)));
    final Vec3d[] start = new Vec3d[2];
    context.runAtTick(
        5,
        () -> {
          start[0] = held.getPos();
          start[1] = free.getPos();
        });
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= 5 && context.getTick() <= 45) {
            hold(context, vampire, held);
            fly(held);
            fly(free);
          }
        });
    context.runAtTick(
        46,
        () -> {
          context.assertTrue(held.isAlive(), "the held flyer is still there");
          context.assertTrue(
              held.getPos().distanceTo(start[0]) < 0.4,
              "the held flyer was drifting but moved " + held.getPos().distanceTo(start[0]));
          context.assertTrue(
              free.getPos().distanceTo(start[1]) > 3.0,
              "the same drift takes a free flyer away, it moved "
                  + free.getPos().distanceTo(start[1]));
          TestPlayers.finish(context, vampire);
        });
  }

  private static void walk(final MobEntity mob) {
    mob.move(MovementType.SELF, new Vec3d(WALK_STEP, 0.0, 0.0));
  }

  private static void fly(final MobEntity mob) {
    mob.move(MovementType.SELF, new Vec3d(WALK_STEP, WALK_STEP / 2, 0.0));
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
