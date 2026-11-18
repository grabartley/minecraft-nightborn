package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.age.Ageing;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.house.Challenge;
import com.grahambartley.nightborn.house.House;
import com.grahambartley.nightborn.house.HouseIdentity;
import com.grahambartley.nightborn.house.HouseSuccession;
import com.grahambartley.nightborn.house.Houses;
import com.grahambartley.nightborn.house.crest.Crest;
import com.grahambartley.nightborn.house.crest.CrestBannerBlock;
import com.grahambartley.nightborn.house.crest.CrestBannerBlockEntity;
import com.grahambartley.nightborn.house.crest.CrestBanners;
import com.grahambartley.nightborn.house.crest.CrestCharge;
import com.grahambartley.nightborn.house.duel.HouseDuels;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.GameMode;

public final class HouseSuccessionGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final int ADULT = 1;
  static final BlockPos LORD_AT = new BlockPos(3, 2, 3);
  static final BlockPos MEMBER_AT = new BlockPos(5, 2, 3);
  static final BlockPos OUTSIDER_AT = new BlockPos(1, 2, 1);
  static final int DUEL_TICKS = 40;
  static final String DUEL_BATCH = "house-duel";

  private static House house(
      final TestContext context, final ServerPlayerEntity lord, final ServerPlayerEntity member) {
    final House house =
        House.founded(
                lord.getUuid(),
                GlobalPos.create(
                    context.getWorld().getRegistryKey(), context.getAbsolutePos(LORD_AT)))
            .withPlayer(member.getUuid());
    Houses.get(context.getWorld().getServer()).put(house);
    return house;
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void anEndedLordLeavesTheHouseToTheirAdultMember(final TestContext context) {
    final ServerPlayerEntity lord = TestPlayers.vampire(context, LORD_AT);
    final ServerPlayerEntity member = TestPlayers.vampire(context, MEMBER_AT);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          Ageing.setRank(lord, ADULT);
          Ageing.setRank(member, ADULT);
          house(context, lord, member);
          final Houses houses = Houses.get(context.getWorld().getServer());
          HouseSuccession.lordLost(context.getWorld().getServer(), lord.getUuid());
          context.assertEquals(
              houses.of(member.getUuid()).map(House::lord),
              Optional.of(member.getUuid()),
              "the adult member is lord now");
          context.assertTrue(
              houses.of(lord.getUuid()).isEmpty(), "the ended lord is no longer in the house");
          TestPlayers.finish(context, lord, member);
        });
  }

  @GameTest(
      templateName = ARENA,
      batchId = DUEL_BATCH,
      tickLimit = TestPlayers.READY + DUEL_TICKS + 20)
  public void aLordDownedInTheDuelLosesTheHouseAndGetsUp(final TestContext context) {
    final ServerPlayerEntity lord = TestPlayers.vampire(context, LORD_AT);
    final ServerPlayerEntity challenger = TestPlayers.vampire(context, MEMBER_AT);
    final ServerPlayerEntity outsider = TestPlayers.vampire(context, OUTSIDER_AT);
    final boolean pvp = context.getWorld().getServer().isPvpEnabled();
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.getWorld().getServer().setPvpEnabled(false);
          outsider.changeGameMode(GameMode.SPECTATOR);
          Ageing.setRank(lord, ADULT);
          Ageing.setRank(challenger, ADULT);
          final House house = house(context, lord, challenger);
          Houses.get(context.getWorld().getServer())
              .put(
                  house.withChallenges(
                      house
                          .challenges()
                          .declared(
                              Challenge.declared(
                                  challenger.getUuid(),
                                  HouseDuels.night(context.getWorld().getServer())))));
        });
    context.runAtTick(
        TestPlayers.READY + DUEL_TICKS,
        () -> {
          context.assertTrue(
              HouseDuels.of(lord.getUuid()).isPresent(),
              "the duel begins when both stand together");
          final float before = lord.getHealth();
          context.getWorld().getServer().setPvpEnabled(true);
          lord.damage(context.getWorld().getDamageSources().playerAttack(outsider), 4.0F);
          context.getWorld().getServer().setPvpEnabled(false);
          context.assertTrue(
              lord.getHealth() == before, "nobody outside the duel can hurt a fighter");
          lord.damage(
              context.getWorld().getDamageSources().playerAttack(challenger), Float.MAX_VALUE);
          context.assertTrue(lord.isAlive(), "the loser is not ended");
          context.assertTrue(Downed.state(lord) == null, "the loser gets up at once");
          context.assertEquals(
              Houses.get(context.getWorld().getServer()).of(lord.getUuid()).map(House::lord),
              Optional.of(challenger.getUuid()),
              "the challenger is lord and the loser stays a member");
          context.assertTrue(HouseDuels.of(lord.getUuid()).isEmpty(), "the circle opens");
          context.getWorld().getServer().setPvpEnabled(pvp);
          TestPlayers.finish(context, lord, challenger, outsider);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aBannerHungByAMemberShowsTheHouseCrest(final TestContext context) {
    final ServerPlayerEntity lord = TestPlayers.vampire(context, LORD_AT);
    final ServerPlayerEntity member = TestPlayers.vampire(context, MEMBER_AT);
    final Crest crest = new Crest(CrestCharge.RAVEN, DyeColor.PURPLE, DyeColor.YELLOW);
    final BlockPos wall = new BlockPos(1, 2, 1);
    final BlockPos banner = wall.south();
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final House house = house(context, lord, member);
          Houses.get(context.getWorld().getServer())
              .put(
                  house.withIdentity(
                      new HouseIdentity(Optional.of("Valcourt"), Optional.of(crest))));
          context.setBlockState(wall, Blocks.STONE);
          final BlockState state =
              CrestBanners.BLOCK.getDefaultState().with(CrestBannerBlock.FACING, Direction.SOUTH);
          context.setBlockState(banner, state);
          CrestBanners.BLOCK.onPlaced(
              context.getWorld(),
              context.getAbsolutePos(banner),
              state,
              member,
              new ItemStack(CrestBanners.ITEM));
          final CrestBannerBlockEntity hung = context.getBlockEntity(banner);
          context.assertEquals(
              hung.crest(), Optional.of(crest), "the banner shows the house crest");
          TestPlayers.finish(context, lord, member);
        });
  }
}
