package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.age.Ageing;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.decor.ChaliceBlock;
import com.grahambartley.nightborn.decor.ChaliceBlood;
import com.grahambartley.nightborn.decor.ChaliceContents;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.house.BloodStore;
import com.grahambartley.nightborn.house.ChaliceInvitation;
import com.grahambartley.nightborn.house.House;
import com.grahambartley.nightborn.house.HouseFounding;
import com.grahambartley.nightborn.house.HouseRules;
import com.grahambartley.nightborn.house.Houses;
import com.grahambartley.nightborn.house.MemberOrder;
import com.grahambartley.nightborn.house.MemberOrders;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.servant.Stance;
import com.grahambartley.nightborn.structure.HouseholdRoll;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public final class HouseGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final int ADULT = 1;
  static final Identifier FLEDGLING = Identifier.of("nightborn", "fledgling");
  static final BlockPos LORD_AT = new BlockPos(4, 2, 4);
  static final BlockPos GUEST_AT = new BlockPos(5, 2, 5);
  static final BlockPos OLD_HOME = new BlockPos(6, 2, 6);

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void anAdultLyingInTheirOwnCoffinFoundsAHouse(final TestContext context) {
    final ServerPlayerEntity lord = TestPlayers.vampire(context, LORD_AT);
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(1, 2, 1), Direction.EAST);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          Ageing.setRank(lord, ADULT);
          final BlockPos absolute = context.getAbsolutePos(head);
          context.assertEquals(
              HouseFounding.lieDown(lord, absolute),
              HouseRules.Founding.FOUND,
              "an adult founds a house");
          context.assertEquals(
              Houses.get(context.getWorld().getServer())
                  .ledBy(lord.getUuid())
                  .flatMap(House::anchor),
              Optional.of(GlobalPos.create(context.getWorld().getRegistryKey(), absolute)),
              "the house stands at the coffin");
          TestPlayers.finish(context, lord);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aFullChaliceBringsAYoungerVampireIntoTheHouse(final TestContext context) {
    final ServerPlayerEntity lord = TestPlayers.vampire(context, LORD_AT);
    final VampireEntity guest = fledgling(context);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          Ageing.setRank(lord, ADULT);
          final World world = context.getWorld();
          final GlobalPos oldHome =
              GlobalPos.create(world.getRegistryKey(), context.getAbsolutePos(OLD_HOME));
          final HouseholdRoll roll = HouseholdRoll.get(context.getWorld().getServer());
          roll.enlist(oldHome, guest.getUuid());
          final Houses houses = Houses.get(context.getWorld().getServer());
          houses.put(
              House.founded(
                  lord.getUuid(),
                  GlobalPos.create(world.getRegistryKey(), context.getAbsolutePos(LORD_AT))));
          lord.setStackInHand(Hand.MAIN_HAND, fullChalice());
          context.assertEquals(invite(lord, guest), ActionResult.SUCCESS, "the chalice is used");
          context.assertTrue(
              houses.ledBy(lord.getUuid()).orElseThrow().npcs().contains(guest.getUuid()),
              "the vampire is counted in the house");
          context.assertTrue(roll.record(oldHome).gone(), "it is gone from its old household");
          context.assertEquals(
              guest.householdTie().lord(), Optional.of(lord.getUuid()), "it serves the lord");
          context.assertTrue(
              ChaliceBlock.contents(lord.getMainHandStack()).empty(), "the invitation empties it");
          TestPlayers.finish(context, lord);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aFledglingLordCannotTakeAMember(final TestContext context) {
    final ServerPlayerEntity lord = TestPlayers.vampire(context, LORD_AT);
    final VampireEntity guest = fledgling(context);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final World world = context.getWorld();
          final Houses houses = Houses.get(context.getWorld().getServer());
          houses.put(
              House.founded(
                  lord.getUuid(),
                  GlobalPos.create(world.getRegistryKey(), context.getAbsolutePos(LORD_AT))));
          guest.becomeStage(FLEDGLING);
          Ageing.setRank(lord, ADULT);
          lord.setStackInHand(Hand.MAIN_HAND, fullChalice());
          invite(lord, guest);
          final VampireEntity second = fledgling(context);
          lord.setStackInHand(Hand.MAIN_HAND, fullChalice());
          invite(lord, second);
          final VampireEntity third = fledgling(context);
          lord.setStackInHand(Hand.MAIN_HAND, fullChalice());
          invite(lord, third);
          context.assertEquals(
              houses.ledBy(lord.getUuid()).orElseThrow().size(), 2, "an adult's house holds two");
          context.assertTrue(third.householdTie().lord().isEmpty(), "the third stays where it was");
          context.assertFalse(
              ChaliceBlock.contents(lord.getMainHandStack()).empty(),
              "a refused chalice stays full");
          TestPlayers.finish(context, lord);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void theLordOrdersAMemberFromTheWheel(final TestContext context) {
    final ServerPlayerEntity lord = TestPlayers.vampire(context, LORD_AT);
    final ServerPlayerEntity stranger = TestPlayers.vampire(context, new BlockPos(3, 2, 5));
    final VampireEntity guest = fledgling(context);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          Ageing.setRank(lord, ADULT);
          final World world = context.getWorld();
          Houses.get(context.getWorld().getServer())
              .put(
                  House.founded(
                      lord.getUuid(),
                      GlobalPos.create(world.getRegistryKey(), context.getAbsolutePos(LORD_AT))));
          lord.setStackInHand(Hand.MAIN_HAND, fullChalice());
          invite(lord, guest);
          lord.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
          context.assertFalse(
              WheelUse.forge(stranger, guest, MemberOrders.ID, MemberOrder.STAY.ordinal()),
              "a stranger cannot order it");
          context.assertTrue(
              WheelUse.pick(lord, guest, MemberOrders.ID, MemberOrder.GUARD.ordinal()),
              "the lord orders it");
          context.assertEquals(
              guest.householdTie().stance(), Optional.of(Stance.GUARD), "it guards");
          TestPlayers.finish(context, lord, stranger);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void bloodBroughtHomeFillsTheMarkedChalice(final TestContext context) {
    final BlockPos chalice = new BlockPos(4, 2, 4);
    context.setBlockState(chalice, DecorBlocks.CHALICE);
    context.assertTrue(
        BloodStore.deposit(
            context.getWorld(), context.getAbsolutePos(chalice), BloodQuality.VILLAGER),
        "the chalice takes the bottle");
    context.checkBlockState(
        chalice,
        state ->
            state.get(ChaliceBlock.BLOOD) == ChaliceBlood.VILLAGER
                && ChaliceBlock.contents(state).bottles() == 1,
        () -> "the chalice holds one bottle of villager blood");
    context.complete();
  }

  private static VampireEntity fledgling(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, GUEST_AT);
    vampire.setAiDisabled(true);
    vampire.becomeStage(FLEDGLING);
    vampire.settleAt(context.getAbsolutePos(OLD_HOME));
    return vampire;
  }

  private static ItemStack fullChalice() {
    return ChaliceBlock.item(new ChaliceContents(ChaliceBlood.ANIMAL, ChaliceContents.CAPACITY));
  }

  private static ActionResult invite(final ServerPlayerEntity lord, final Entity guest) {
    return ChaliceInvitation.onUse(lord, lord.getWorld(), Hand.MAIN_HAND, guest, null);
  }
}
