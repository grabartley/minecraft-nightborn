package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.faction.Oath;
import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.guild.ContractItems;
import com.grahambartley.nightborn.guild.ContractProgress;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.GuildRestock;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.structure.RareSpreadPlacement;
import com.grahambartley.nightborn.structure.StructureRarity;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import com.grahambartley.nightborn.wheel.CommandWheel;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;

public class HunterCampGameTest implements FabricGameTest {

  static final Identifier GEAR = Identifier.of("nightborn", "guild_gear");
  static final Identifier CONTRACTS = Identifier.of("nightborn", "guild_contracts");

  static final String ARENA = "nightborn:vampire_arena";
  static final int START = 2;
  static final String RESTOCK = "guild-restock";
  static final String PLACED_CAMP = "placed-camp";
  static final int CLEAR_OF_OTHER_TESTS = 64;
  static final Identifier STAKE_THREE = Identifier.of("nightborn", "stake_three");
  static final Identifier CLEAR_HOUSEHOLD = Identifier.of("nightborn", "clear_household");

  static ItemStack contract(final Identifier id, final int done) {
    final ItemStack stack = new ItemStack(ContractItems.HUNTER_CONTRACT);
    stack.set(ContractItems.PROGRESS, new ContractProgress(id, done));
    return stack;
  }

  static int done(final ServerPlayerEntity player, final int slot) {
    final ContractProgress progress =
        player.getInventory().getStack(slot).get(ContractItems.PROGRESS);
    return progress == null ? -1 : progress.done();
  }

  static int count(final ServerPlayerEntity player, final Item item) {
    return player.getInventory().count(item);
  }

  private GuildMasterEntity guildMaster(final TestContext context) {
    final GuildMasterEntity master =
        context.spawnEntity(GuildMobs.GUILD_MASTER, new BlockPos(4, 2, 4));
    master.setAiDisabled(true);
    return master;
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = 20)
  public void hunterCampsGenerateRarelyFromTheCampRarity(final TestContext context) {
    final var registries = context.getWorld().getRegistryManager();
    context.assertTrue(
        registries
            .get(RegistryKeys.STRUCTURE)
            .containsId(Identifier.of("nightborn", "hunter_camp")),
        "the hunter camp structure loads");
    final var set =
        registries.get(RegistryKeys.STRUCTURE_SET).get(Identifier.of("nightborn", "hunter_camp"));
    context.assertTrue(
        set != null
            && set.placement() instanceof RareSpreadPlacement placement
            && placement.rarity() == StructureRarity.HUNTER_CAMPS,
        "camps are spread by the camp rarity setting");
    context.assertTrue(
        context
            .getWorld()
            .getStructureTemplateManager()
            .getTemplate(Identifier.of("nightborn", "hunter_camp/camp"))
            .isPresent(),
        "the camp template loads");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void theGuildMasterTurnsAVampireAway(final TestContext context) {
    final GuildMasterEntity master = guildMaster(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    vampire.setStackInHand(Hand.MAIN_HAND, contract(STAKE_THREE, 3));
    context.runAtTick(
        START,
        () -> {
          master.interactMob(vampire, Hand.MAIN_HAND);
          context.assertFalse(master.hasCustomer(), "a vampire cannot trade");
          context.assertFalse(CommandWheel.open(vampire, master), "nor gets the wheel");
          context.assertEquals(done(vampire, vampire.getInventory().selectedSlot), 3, "no claim");
          context.assertEquals(count(vampire, Items.EMERALD), 0, "nothing is paid");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void theGuildMasterSellsGearAndContractsToAHunterAtSeparateCounters(
      final TestContext context) {
    final GuildMasterEntity master = guildMaster(context);
    final ServerPlayerEntity human = TestPlayers.hunter(context, new BlockPos(2, 2, 4));
    OathAttachment.set(human, OathAttachment.get(human).withRank(Oath.TOP_RANK));
    context.runAtTick(
        START,
        () -> {
          context.assertTrue(
              master.interactMob(human, Hand.MAIN_HAND).isAccepted(), "using him opens the wheel");
          context.assertFalse(master.hasCustomer(), "the wheel comes before any trade");
          context.assertTrue(
              WheelUse.pick(human, master, GEAR, WheelEntry.NO_ARGUMENT), "the human picks trade");
          context.assertTrue(master.getCustomer() == human, "the human is trading");
          context.assertTrue(
              offered(human).stream()
                  .map(TradeOffer::getSellItem)
                  .anyMatch(stack -> stack.isOf(GearItems.WOODEN_STAKE)),
              "stakes are for sale");
          context.assertTrue(
              offered(human).stream()
                  .map(TradeOffer::getSellItem)
                  .noneMatch(stack -> stack.contains(ContractItems.PROGRESS)),
              "no contracts at the gear counter");
          human.closeHandledScreen();
          context.assertTrue(
              WheelUse.pick(human, master, CONTRACTS, WheelEntry.NO_ARGUMENT),
              "the human picks contracts");
          context.assertTrue(
              !offered(human).isEmpty()
                  && offered(human).stream()
                      .map(TradeOffer::getSellItem)
                      .allMatch(stack -> stack.contains(ContractItems.PROGRESS)),
              "only contracts at the contract counter");
          context.assertEquals(
              offered(human).size() + gear(master),
              master.getOffers().size(),
              "between them the counters offer everything he has");
          TestPlayers.finish(context, human);
        });
  }

  private static TradeOfferList offered(final ServerPlayerEntity player) {
    return player.currentScreenHandler instanceof MerchantScreenHandler trading
        ? trading.getRecipes()
        : new TradeOfferList();
  }

  private static int gear(final GuildMasterEntity master) {
    return (int)
        master.getOffers().stream()
            .filter(offer -> !offer.getSellItem().contains(ContractItems.PROGRESS))
            .count();
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aFulfilledContractPaysOnceAndIsHandedIn(final TestContext context) {
    final GuildMasterEntity master = guildMaster(context);
    final ServerPlayerEntity human = TestPlayers.hunter(context, new BlockPos(2, 2, 4));
    human.setStackInHand(Hand.MAIN_HAND, contract(STAKE_THREE, 3));
    context.runAtTick(
        START,
        () -> {
          final TreeProgress before = SkillAttachment.get(human, SkillTree.HUNTER);
          master.interactMob(human, Hand.MAIN_HAND);
          context.assertEquals(
              count(human, ContractItems.HUNTER_CONTRACT), 0, "the contract is handed in");
          context.assertTrue(count(human, Items.EMERALD) > 0, "the contract pays emeralds");
          context.assertTrue(count(human, GearItems.SILVER_INGOT) > 0, "and silver");
          TestPlayers.assertPaid(
              context,
              human,
              SkillTree.HUNTER,
              before,
              ServerConfigService.current().skills().contractExperience(),
              "and hunter points");
          final int emeralds = count(human, Items.EMERALD);
          context.assertTrue(
              master.interactMob(human, Hand.MAIN_HAND).isAccepted(),
              "a second visit opens the wheel");
          context.assertEquals(count(human, Items.EMERALD), emeralds, "it pays only once");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aContractNotYetFulfilledIsNotPaid(final TestContext context) {
    final GuildMasterEntity master = guildMaster(context);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    human.setStackInHand(Hand.MAIN_HAND, contract(STAKE_THREE, 2));
    context.runAtTick(
        START,
        () -> {
          master.interactMob(human, Hand.MAIN_HAND);
          context.assertEquals(
              count(human, ContractItems.HUNTER_CONTRACT), 1, "the contract is kept");
          context.assertEquals(count(human, Items.EMERALD), 0, "nothing is paid");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void stakingAVampireMarksOnlyTheFirstOpenContract(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    human.getInventory().setStack(0, new ItemStack(GearItems.WOODEN_STAKE));
    human.getInventory().setStack(1, contract(STAKE_THREE, 0));
    human.getInventory().setStack(2, contract(STAKE_THREE, 0));
    human.getInventory().selectedSlot = 0;
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
    vampire.setAiDisabled(true);
    context.runAtTick(
        START,
        () -> {
          vampire.damage(vampire.getDamageSources().generic(), 1000.0F);
          vampire.damage(human.getDamageSources().playerAttack(human), 1.0F);
          context.assertFalse(vampire.isAlive(), "the vampire is staked");
          context.assertEquals(done(human, 1), 1, "the first contract moves on");
          context.assertEquals(done(human, 2), 0, "the second does not");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void clearingAHouseholdCountsOnlyTheLastOfThem(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    human.getInventory().setStack(0, new ItemStack(GearItems.WOODEN_STAKE));
    human.getInventory().setStack(1, contract(CLEAR_HOUSEHOLD, 0));
    human.getInventory().selectedSlot = 0;
    final BlockPos home = context.getAbsolutePos(new BlockPos(4, 2, 4));
    final VampireEntity first = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
    final VampireEntity last = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(6, 2, 4));
    for (final VampireEntity vampire : new VampireEntity[] {first, last}) {
      vampire.setAiDisabled(true);
      vampire.settleAt(home);
    }
    context.runAtTick(
        START,
        () -> {
          stake(first, human);
          context.assertEquals(done(human, 1), 0, "a housemate still lives");
          stake(last, human);
          context.assertEquals(done(human, 1), 1, "the household is cleared");
          TestPlayers.finish(context, human);
        });
  }

  private static void stake(final VampireEntity vampire, final ServerPlayerEntity hunter) {
    vampire.damage(vampire.getDamageSources().generic(), 1000.0F);
    vampire.damage(hunter.getDamageSources().playerAttack(hunter), 1.0F);
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void huntersHuntVampiresAndLeaveHumansAlone(final TestContext context) {
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(2, 2, 2));
    hunter.setAiDisabled(true);
    final VampireEntity mob = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(6, 2, 6));
    mob.setAiDisabled(true);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 6));
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(6, 2, 2));
    context.runAtTick(
        START,
        () -> {
          context.assertTrue(hunter.canTarget(mob), "a vampire mob is hunted");
          context.assertTrue(hunter.canTarget(vampire), "a vampire player is hunted");
          context.assertFalse(hunter.canTarget(human), "a human is left alone");
          TestPlayers.finish(context, human, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aHuntersStakeFinishesADownedVampire(final TestContext context) {
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(3, 2, 4));
    hunter.setAiDisabled(true);
    hunter.equipStack(
        net.minecraft.entity.EquipmentSlot.MAINHAND, new ItemStack(GearItems.WOODEN_STAKE));
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
    vampire.setAiDisabled(true);
    context.runAtTick(
        START,
        () -> {
          vampire.damage(vampire.getDamageSources().generic(), 1000.0F);
          context.assertTrue(vampire.isAlive(), "the vampire is only downed");
          hunter.tryAttack(vampire);
          context.assertFalse(vampire.isAlive(), "the hunter's stake finishes it");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aContractInTheOffhandIsClaimedToo(final TestContext context) {
    final GuildMasterEntity master = guildMaster(context);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    human.setStackInHand(Hand.OFF_HAND, contract(STAKE_THREE, 3));
    context.runAtTick(
        START,
        () -> {
          master.interactMob(human, Hand.MAIN_HAND);
          context.assertTrue(human.getOffHandStack().isEmpty(), "the offhand contract is taken");
          context.assertTrue(count(human, Items.EMERALD) > 0, "and paid");
          context.assertFalse(master.hasCustomer(), "claiming does not open the trades");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aPlayerWhoTurnsWhileTradingIsShutOut(final TestContext context) {
    final GuildMasterEntity master = guildMaster(context);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    context.runAtTick(
        START,
        () -> {
          WheelUse.pick(human, master, GEAR, WheelEntry.NO_ARGUMENT);
          context.assertTrue(master.getCustomer() == human, "the human is trading");
          VampireTransitions.turn(human);
        });
    context.runAtTick(
        START + 2,
        () -> {
          context.assertFalse(master.hasCustomer(), "the new vampire is turned away");
          context.assertTrue(
              human.currentScreenHandler == human.playerScreenHandler, "the trade screen closes");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, batchId = RESTOCK, tickLimit = TestPlayers.READY + 20)
  public void theGuildMastersStockRenewsEachDay(final TestContext context) {
    final GuildMasterEntity master = guildMaster(context);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    context.runAtTick(
        START,
        () -> {
          context.getWorld().setTimeOfDay(1000L);
          WheelUse.pick(human, master, GEAR, WheelEntry.NO_ARGUMENT);
          final TradeOffer first = master.getOffers().getFirst();
          while (!first.isDisabled()) {
            first.use();
          }
          human.closeHandledScreen();
          WheelUse.pick(human, master, GEAR, WheelEntry.NO_ARGUMENT);
          context.assertTrue(
              master.getOffers().getFirst().isDisabled(), "stock stays sold out the same day");
          human.closeHandledScreen();
          context.getWorld().setTimeOfDay(1000L + GuildRestock.DAY_TICKS);
          WheelUse.pick(human, master, GEAR, WheelEntry.NO_ARGUMENT);
          context.assertFalse(
              master.getOffers().getFirst().isDisabled(), "a new day brings new stock");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void huntersKeepTheirCampAcrossASave(final TestContext context) {
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(4, 2, 4));
    hunter.setAiDisabled(true);
    context.runAtTick(
        START,
        () -> {
          context.assertTrue(hunter.hasPositionTarget(), "a hunter settles where it first stands");
          context.assertEquals(
              hunter.getPositionTarget(),
              context.getAbsolutePos(new BlockPos(4, 2, 4)),
              "at its spawn");
          final NbtCompound saved = new NbtCompound();
          hunter.writeNbt(saved);
          final HunterEntity loaded = GuildMobs.HUNTER.create(context.getWorld());
          loaded.readNbt(saved);
          context.assertEquals(
              loaded.getPositionTarget(), hunter.getPositionTarget(), "the camp is kept");
          context.assertEquals(loaded.rig(), hunter.rig(), "and so is its rig");
          context.assertFalse(hunter.look().isEmpty(), "it has a look");
          context.assertEquals(loaded.look(), hunter.look(), "which is kept");
          context.assertFalse(hunter.kit().isEmpty(), "it has a kit");
          context.assertEquals(loaded.kit(), hunter.kit(), "which is kept too");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = PLACED_CAMP, tickLimit = 20)
  public void aPlacedCampHoldsKittedHuntersAndAGuildMaster(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final BlockPos origin = context.getAbsolutePos(new BlockPos(0, 1, 0)).up(CLEAR_OF_OTHER_TESTS);
    final StructureTemplate camp =
        world
            .getStructureTemplateManager()
            .getTemplate(Identifier.of("nightborn", "hunter_camp/camp"))
            .orElseThrow();
    camp.place(world, origin, origin, new StructurePlacementData(), world.getRandom(), 2);
    final Box area = new Box(origin).stretch(camp.getSize().getX(), 8, camp.getSize().getZ());
    context.runAtTick(
        START,
        () -> {
          final List<HunterEntity> hunters =
              world.getEntitiesByClass(HunterEntity.class, area, hunter -> true);
          final List<GuildMasterEntity> masters =
              world.getEntitiesByClass(GuildMasterEntity.class, area, master -> true);
          context.assertEquals(hunters.size(), 3, "three hunters live in a camp");
          hunters.forEach(HunterEntity::tick);
          context.assertEquals(masters.size(), 1, "with one Guild Master");
          hunters.forEach(
              hunter -> context.assertFalse(hunter.kit().isEmpty(), "every hunter has a kit"));
          context.assertTrue(
              world.getBlockState(origin.add(8, 1, 10)).isOf(Blocks.CAMPFIRE),
              "a campfire burns at the heart of the camp");
          hunters.forEach(Entity::discard);
          masters.forEach(Entity::discard);
          BlockPos.iterate(origin, origin.add(camp.getSize()).add(-1, -1, -1))
              .forEach(pos -> world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2));
          context.complete();
        });
  }
}
