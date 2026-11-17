package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.config.FamiliarSettings;
import com.grahambartley.nightborn.familiar.ChestMark;
import com.grahambartley.nightborn.familiar.FamiliarHiring;
import com.grahambartley.nightborn.familiar.FamiliarOrders;
import com.grahambartley.nightborn.familiar.FamiliarRegistry;
import com.grahambartley.nightborn.familiar.FamiliarRelease;
import com.grahambartley.nightborn.familiar.FamiliarUpkeep;
import com.grahambartley.nightborn.familiar.Familiars;
import com.grahambartley.nightborn.familiar.HireRules;
import com.grahambartley.nightborn.familiar.HumanFamiliarEntity;
import com.grahambartley.nightborn.familiar.goal.BetrayOwnerGoal;
import com.grahambartley.nightborn.familiar.goal.RetaliateGoal;
import com.grahambartley.nightborn.familiar.goal.RunErrandGoal;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;

public class HumanFamiliarGameTest implements FabricGameTest {

  static final String ARENA = VampireMobGameTest.ARENA;
  static final String NOON_BATCH = "human-familiar-noon";
  static final BlockPos STAND = new BlockPos(3, 2, 3);
  static final FamiliarSettings ALWAYS_BETRAYS =
      new FamiliarSettings(60, 1, 7, 5, 15, 25, 10, 25, 100, 8, 32);

  @BeforeBatch(batchId = NOON_BATCH)
  public void pinNoon(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = NOON_BATCH)
  public void unpinNoon(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void contractHiresAVillagerAsAFamiliar(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    final VillagerEntity villager = villager(context, VillagerType.TAIGA);
    final ItemStack contract = new ItemStack(Familiars.CONTRACT, 2);
    vampire.setStackInHand(Hand.MAIN_HAND, contract);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertTrue(
              FamiliarHiring.hire(vampire, villager, contract) == HireRules.Outcome.HIRE,
              "a vampire with a contract hires a grown villager");
          final HumanFamiliarEntity familiar = onlyFamiliar(context);
          context.assertTrue(villager.isRemoved(), "the villager becomes the familiar");
          context.assertTrue(
              familiar.getVillagerData().getType() == VillagerType.TAIGA,
              "the familiar keeps the villager's biome look");
          context.assertTrue(familiar.isOwnedBy(vampire), "the vampire owns the familiar");
          context.assertTrue(
              FamiliarRegistry.of(context.getWorld().getServer())
                  .keeps(vampire.getUuid(), familiar.getUuid()),
              "the contract slot holds the familiar");
          context.assertEquals(contract.getCount(), 1, "hiring uses up one contract");
          context.assertTrue(
              NameGameTest.inFamiliarPools(context, familiar.getName().getString()),
              "the familiar holds a name from the familiar pools from the moment it is hired");
          context.assertTrue(
              TestAdvancements.done(context, vampire, "good_help_is_hard_to_find"),
              "hiring grants Good Help Is Hard To Find");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aSecondFamiliarOrAHumanHirerIsRefused(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    final ServerPlayerEntity human = TestPlayers.human(context, STAND.east());
    final ItemStack contract = new ItemStack(Familiars.CONTRACT, 3);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          FamiliarHiring.hire(vampire, villager(context, VillagerType.PLAINS), contract);
          final VillagerEntity second = villager(context, VillagerType.PLAINS);
          context.assertTrue(
              FamiliarHiring.hire(vampire, second, contract) == HireRules.Outcome.ALREADY_KEEPS_ONE,
              "a vampire keeps one familiar at a time");
          context.assertTrue(
              FamiliarHiring.hire(human, second, contract) == HireRules.Outcome.NOT_A_VAMPIRE,
              "a human cannot hire a familiar");
          context.assertFalse(second.isRemoved(), "a refused villager stays a villager");
          context.assertEquals(contract.getCount(), 2, "a refused hire keeps the contract");
          TestPlayers.finish(context, vampire, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void onlyTheOwnerPaysWagesAndOneEmeraldIsSpent(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    final ServerPlayerEntity stranger = TestPlayers.vampire(context, STAND.east());
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final HumanFamiliarEntity familiar = hired(context, vampire);
          final long before = familiar.paidThroughDay();
          stranger.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.EMERALD, 4));
          familiar.interact(stranger, Hand.MAIN_HAND);
          context.assertEquals(familiar.paidThroughDay(), before, "a stranger cannot pay");
          context.assertEquals(
              stranger.getMainHandStack().getCount(), 4, "a stranger keeps their emeralds");
          vampire.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.EMERALD, 4));
          familiar.interact(vampire, Hand.MAIN_HAND);
          context.assertEquals(familiar.paidThroughDay(), before + 1, "an emerald pays a day");
          context.assertEquals(
              vampire.getMainHandStack().getCount(), 3, "paying spends one emerald");
          TestPlayers.finish(context, vampire, stranger);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void feedingOnTheFamiliarCostsLoyalty(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final HumanFamiliarEntity familiar = hired(context, vampire);
          final int before = familiar.loyalty();
          familiar.damage(NightbornDamage.bite(context.getWorld(), vampire), 1.0F);
          context.assertEquals(
              familiar.loyalty(),
              before - ServerConfigService.current().familiars().bittenLoyalty(),
              "a bite from the owner costs loyalty");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aKilledFamiliarFreesTheSlotAndDropsWhatItCarried(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final HumanFamiliarEntity familiar = hired(context, vampire);
          familiar.getInventory().setStack(0, new ItemStack(Items.WHEAT, 12));
          familiar.kill();
          context.assertTrue(
              FamiliarRegistry.of(context.getWorld().getServer())
                  .bondOf(vampire.getUuid())
                  .isEmpty(),
              "a dead familiar frees the contract slot");
          final int dropped =
              context
                  .getWorld()
                  .getEntitiesByClass(
                      ItemEntity.class,
                      familiar.getBoundingBox().expand(3),
                      item -> item.getStack().isOf(Items.WHEAT))
                  .stream()
                  .mapToInt(item -> item.getStack().getCount())
                  .sum();
          context.assertEquals(dropped, 12, "a dead familiar drops everything it carried");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void anUnpaidDisloyalFamiliarTurnsAndWaitsForItsOwnerToSleep(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final HumanFamiliarEntity familiar = hired(context, vampire);
          final FamiliarRegistry registry = FamiliarRegistry.of(context.getWorld().getServer());
          familiar.loyalty(20);
          final long today = familiar.lastDay();
          FamiliarUpkeep.passDays(familiar, registry, today + 2, ALWAYS_BETRAYS);
          context.assertTrue(familiar.turned(), "a disloyal unpaid familiar turns");
          context.assertTrue(
              registry.bondOf(vampire.getUuid()).isEmpty(), "a turned familiar frees the slot");
          final BetrayOwnerGoal betrayal = new BetrayOwnerGoal(familiar);
          context.assertFalse(betrayal.canStart(), "it hides its betrayal while its owner wakes");
          vampire.sleep(vampire.getBlockPos());
          context.assertTrue(betrayal.canStart(), "it attacks its owner in their sleep");
          vampire.wakeUp();
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aReleasedFamiliarBecomesTheSameVillagerAgain(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final HumanFamiliarEntity familiar = hired(context, vampire);
          FamiliarRelease.freeOwner(context.getWorld().getServer(), vampire.getUuid());
          FamiliarUpkeep.check(familiar);
          context.assertTrue(familiar.isRemoved(), "a freed familiar stops being a familiar");
          final VillagerEntity villager =
              context
                  .getWorld()
                  .getEntitiesByType(
                      EntityType.VILLAGER, familiar.getBoundingBox().expand(1), v -> true)
                  .stream()
                  .findFirst()
                  .orElse(null);
          context.assertTrue(villager != null, "it becomes a villager again");
          context.assertTrue(
              villager.getVillagerData().getType() == VillagerType.SWAMP,
              "the villager keeps its biome look");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aFamiliarKeepsItsServiceThroughASave(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final HumanFamiliarEntity familiar = hired(context, vampire);
          familiar.loyalty(42);
          familiar.paidThroughDay(familiar.paidThroughDay() + 3);
          familiar.getInventory().setStack(2, new ItemStack(Items.BREAD, 5));
          final NbtCompound saved = familiar.writeNbt(new NbtCompound());
          final HumanFamiliarEntity loaded = Familiars.HUMAN_FAMILIAR.create(context.getWorld());
          loaded.readNbt(saved);
          context.assertTrue(loaded.isOwnedBy(vampire), "the owner is kept");
          context.assertEquals(loaded.loyalty(), 42, "loyalty is kept");
          context.assertEquals(
              loaded.paidThroughDay(), familiar.paidThroughDay(), "paid days are kept");
          context.assertEquals(
              loaded.getInventory().count(Items.BREAD), 5, "carried goods are kept");
          context.assertTrue(
              loaded.getVillagerData().getType() == VillagerType.SWAMP, "the look is kept");
          loaded.discard();
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = NOON_BATCH, tickLimit = TestPlayers.READY + 40)
  public void anErrandFetchesTradesAndDeliversWithoutLosingAnything(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    final BlockPos fetch = new BlockPos(2, 2, 2);
    final BlockPos deliver = new BlockPos(4, 2, 2);
    context.setBlockState(fetch, Blocks.CHEST);
    context.setBlockState(deliver, Blocks.CHEST);
    final ChestBlockEntity from = context.getBlockEntity(fetch);
    from.setStack(0, new ItemStack(Items.WHEAT, 30));
    final VillagerEntity farmer = villager(context, VillagerType.PLAINS);
    farmer.setAiDisabled(true);
    final TradeOfferList offers = new TradeOfferList();
    offers.add(
        new TradeOffer(
            new TradedItem(Items.WHEAT, 20), new ItemStack(Items.EMERALD), 12, 2, 0.05F));
    farmer.setOffers(offers);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final HumanFamiliarEntity familiar = hired(context, vampire);
          familiar.setAiDisabled(true);
          context.assertTrue(
              FamiliarOrders.mark(vampire, context.getAbsolutePos(fetch))
                  .equals(Optional.of(ChestMark.FETCH)),
              "the first marked chest is the fetch chest");
          context.assertTrue(
              FamiliarOrders.mark(vampire, context.getAbsolutePos(deliver))
                  .equals(Optional.of(ChestMark.DELIVER)),
              "the second marked chest is the deliver chest");
          final RunErrandGoal errand = new RunErrandGoal(familiar);
          context.assertTrue(errand.canStart(), "a familiar with both chests marked runs errands");
          errand.start();
          for (int step = 0; step < 3 && errand.shouldContinue(); step++) {
            errand.tick();
          }
          context.assertFalse(errand.shouldContinue(), "the errand ends after delivering");
          errand.tick();
          final ChestBlockEntity to = context.getBlockEntity(deliver);
          context.assertEquals(from.count(Items.WHEAT), 0, "the fetch chest is emptied");
          context.assertEquals(to.count(Items.WHEAT), 10, "what was not traded is delivered");
          context.assertEquals(to.count(Items.EMERALD), 1, "what was bought is delivered");
          context.assertEquals(offers.get(0).getUses(), 1, "the villager made the trade");
          context.assertTrue(familiar.getInventory().isEmpty(), "nothing is left carried");
          context.assertTrue(
              context
                  .getWorld()
                  .getEntitiesByClass(
                      ItemEntity.class, context.getTestBox().expand(2), item -> true)
                  .isEmpty(),
              "nothing is dropped on the way");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void onlyAnOpenChestOrBarrelCanBeMarked(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    final BlockPos locked = new BlockPos(1, 2, 1);
    final BlockPos furnace = new BlockPos(2, 2, 1);
    final BlockPos barrel = new BlockPos(4, 2, 1);
    context.setBlockState(locked, Blocks.CHEST);
    context.setBlockState(furnace, Blocks.FURNACE);
    context.setBlockState(barrel, Blocks.BARREL);
    final NbtCompound lock = new NbtCompound();
    lock.putString("Lock", "crypt key");
    context.getBlockEntity(locked).read(lock, context.getWorld().getRegistryManager());
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertTrue(
              FamiliarOrders.mark(vampire, context.getAbsolutePos(barrel)).isEmpty(),
              "a vampire without a familiar marks nothing");
          hired(context, vampire);
          context.assertTrue(
              FamiliarOrders.mark(vampire, context.getAbsolutePos(locked)).isEmpty(),
              "a chest the owner cannot open is refused");
          context.assertTrue(
              FamiliarOrders.mark(vampire, context.getAbsolutePos(furnace)).isEmpty(),
              "a furnace is not an errand chest");
          context.assertTrue(
              FamiliarOrders.mark(vampire, context.getAbsolutePos(barrel))
                  .equals(Optional.of(ChestMark.FETCH)),
              "a barrel can be marked");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aFamiliarNeverStrikesBackAtItsOwner(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    final ServerPlayerEntity stranger = TestPlayers.human(context, STAND.east());
    final HumanFamiliarEntity[] familiar = new HumanFamiliarEntity[1];
    context.runAtTick(TestPlayers.READY, () -> familiar[0] = hired(context, vampire));
    context.runAtTick(
        TestPlayers.READY + 5,
        () -> {
          familiar[0].damage(context.getWorld().getDamageSources().playerAttack(vampire), 1.0F);
          context.assertTrue(
              familiar[0].getAttacker() == vampire, "the owner's blow lands on the familiar");
          context.assertFalse(
              new RetaliateGoal(familiar[0]).canStart(), "a blow from its owner goes unanswered");
        });
    context.runAtTick(
        TestPlayers.READY + 10,
        () -> {
          familiar[0].timeUntilRegen = 0;
          familiar[0].damage(context.getWorld().getDamageSources().playerAttack(stranger), 1.0F);
          context.assertTrue(
              new RetaliateGoal(familiar[0]).canStart(), "a blow from a stranger is answered");
          TestPlayers.finish(context, vampire, stranger);
        });
  }

  @GameTest(templateName = ARENA, batchId = NOON_BATCH, tickLimit = TestPlayers.READY + 40)
  public void aChestLockedAfterMarkingIsLeftAlone(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    final BlockPos fetch = new BlockPos(2, 2, 2);
    final BlockPos deliver = new BlockPos(4, 2, 2);
    context.setBlockState(fetch, Blocks.CHEST);
    context.setBlockState(deliver, Blocks.CHEST);
    final ChestBlockEntity from = context.getBlockEntity(fetch);
    from.setStack(0, new ItemStack(Items.WHEAT, 30));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final HumanFamiliarEntity familiar = hired(context, vampire);
          familiar.setAiDisabled(true);
          FamiliarOrders.mark(vampire, context.getAbsolutePos(fetch));
          FamiliarOrders.mark(vampire, context.getAbsolutePos(deliver));
          final NbtCompound lock = new NbtCompound();
          lock.putString("Lock", "crypt key");
          from.read(lock, context.getWorld().getRegistryManager());
          from.setStack(0, new ItemStack(Items.WHEAT, 30));
          final RunErrandGoal errand = new RunErrandGoal(familiar);
          context.assertTrue(errand.canStart(), "the errand starts from its marks");
          errand.start();
          for (int step = 0; step < 3 && errand.shouldContinue(); step++) {
            errand.tick();
          }
          context.assertEquals(from.count(Items.WHEAT), 30, "nothing is taken from a locked chest");
          context.assertTrue(familiar.getInventory().isEmpty(), "the familiar carries nothing");
          context.assertTrue(
              FamiliarRegistry.of(context.getWorld().getServer())
                  .bondOf(vampire.getUuid())
                  .orElseThrow()
                  .marks()
                  .fetch()
                  .isEmpty(),
              "the locked chest is forgotten");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void theReleaseCommandFreesAPlayersSlotOrAFamiliar(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final MinecraftServer server = context.getWorld().getServer();
          final HumanFamiliarEntity familiar = hired(context, vampire);
          final ServerCommandSource operator = server.getCommandSource().withSilent();
          server
              .getCommandManager()
              .executeWithPrefix(
                  operator, "nightborn familiar " + familiar.getUuidAsString() + " release");
          context.assertTrue(familiar.isRemoved(), "a familiar named in the command is released");
          context.assertTrue(
              FamiliarRegistry.of(server).bondOf(vampire.getUuid()).isEmpty(),
              "releasing the familiar frees its owner's slot");
          final HumanFamiliarEntity second = hired(context, vampire);
          server
              .getCommandManager()
              .executeWithPrefix(
                  operator, "nightborn familiar " + vampire.getName().getString() + " release");
          context.assertTrue(
              FamiliarRegistry.of(server).bondOf(vampire.getUuid()).isEmpty(),
              "a player named in the command loses their familiar slot");
          FamiliarUpkeep.check(second);
          context.assertTrue(second.isRemoved(), "the freed familiar becomes a villager again");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = NOON_BATCH, tickLimit = TestPlayers.READY + 400)
  public void aWorkingFamiliarRunsItsOwnErrandWithoutLosingAnything(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, STAND);
    final BlockPos fetch = new BlockPos(2, 2, 2);
    final BlockPos deliver = new BlockPos(4, 2, 2);
    context.setBlockState(fetch, Blocks.CHEST);
    context.setBlockState(deliver, Blocks.CHEST);
    final ChestBlockEntity from = context.getBlockEntity(fetch);
    from.setStack(0, new ItemStack(Items.WHEAT, 30));
    final HumanFamiliarEntity[] familiar = new HumanFamiliarEntity[1];
    final long[] delivered = {-1L};
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          familiar[0] = hired(context, vampire);
          FamiliarOrders.mark(vampire, context.getAbsolutePos(fetch));
          FamiliarOrders.mark(vampire, context.getAbsolutePos(deliver));
        });
    context.runAtEveryTick(
        () -> {
          if (familiar[0] == null) {
            return;
          }
          final ChestBlockEntity to = context.getBlockEntity(deliver);
          final int carried = familiar[0].getInventory().count(Items.WHEAT);
          context.assertEquals(
              from.count(Items.WHEAT) + carried + to.count(Items.WHEAT),
              30,
              "every ear of wheat is in a chest or carried");
          if (to.count(Items.WHEAT) == 30 && delivered[0] < 0) {
            delivered[0] = context.getTick();
          }
          if (delivered[0] >= 0 && context.getTick() >= delivered[0] + 40) {
            context.assertTrue(
                familiar[0].isAlive(), "the familiar keeps working after its errand");
            TestPlayers.finish(context, vampire);
          }
        });
  }

  private static VillagerEntity villager(final TestContext context, final VillagerType type) {
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(3, 2, 2));
    villager.setVillagerData(new VillagerData(type, VillagerProfession.FARMER, 1));
    return villager;
  }

  private static HumanFamiliarEntity hired(
      final TestContext context, final ServerPlayerEntity owner) {
    FamiliarHiring.hire(
        owner, villager(context, VillagerType.SWAMP), new ItemStack(Familiars.CONTRACT));
    return onlyFamiliar(context);
  }

  private static HumanFamiliarEntity onlyFamiliar(final TestContext context) {
    return context
        .getWorld()
        .getEntitiesByType(
            Familiars.HUMAN_FAMILIAR, context.getTestBox(), familiar -> familiar.isAlive())
        .getFirst();
  }
}
