package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.name.MobNaming;
import com.grahambartley.nightborn.nemesis.Nemeses;
import com.grahambartley.nightborn.nemesis.NemesisAttachment;
import com.grahambartley.nightborn.nemesis.NemesisEntry;
import com.grahambartley.nightborn.nemesis.NemesisGrowth;
import com.grahambartley.nightborn.nemesis.NemesisItems;
import com.grahambartley.nightborn.nemesis.NemesisLedger;
import com.grahambartley.nightborn.nemesis.NemesisMaking;
import com.grahambartley.nightborn.nemesis.NemesisMark;
import com.grahambartley.nightborn.nemesis.NemesisVisits;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class NemesisGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final BlockPos PLAYER = new BlockPos(4, 2, 4);

  private static HunterEntity hunter(final TestContext context, final int x) {
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(x, 2, 1));
    hunter.setAiDisabled(true);
    MobNaming.ensure(hunter);
    return hunter;
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aVampireHoldsThreeNemesesAndNeverAFourth(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, PLAYER);
    final List<HunterEntity> hunters =
        List.of(hunter(context, 1), hunter(context, 3), hunter(context, 5), hunter(context, 7));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final List<Boolean> made =
              hunters.stream()
                  .map(hunter -> NemesisMaking.make(hunter, vampire.getUuid()))
                  .toList();
          context.assertEquals(made, List.of(true, true, true, false), "three made, never four");
          context.assertEquals(
              NemesisLedger.of(context.getWorld().getServer()).of(vampire.getUuid()).size(),
              3,
              "the ledger holds three");
          context.assertTrue(hunters.get(0).isPersistent(), "a nemesis never leaves the world");
          context.assertTrue(
              NemesisAttachment.get(hunters.get(3)).isEmpty(), "the fourth stays ordinary");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aNemesisKeepsItsGrudgeAndGrowthThroughASave(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, PLAYER);
    final HunterEntity hunter = hunter(context, 1);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          NemesisMaking.make(hunter, vampire.getUuid());
          NemesisGrowth.grow(
              context.getWorld(), hunter, NemesisAttachment.get(hunter).orElseThrow());
          final NbtCompound saved = new NbtCompound();
          hunter.writeNbt(saved);
          final HunterEntity loaded = GuildMobs.HUNTER.create(context.getWorld());
          loaded.readNbt(saved);
          final Optional<NemesisMark> mark = NemesisAttachment.get(loaded);
          context.assertEquals(
              mark.map(NemesisMark::quarry), Optional.of(vampire.getUuid()), "the grudge");
          context.assertEquals(mark.map(NemesisMark::growths), Optional.of(1), "the growth");
          context.assertEquals(loaded.getMaxHealth(), 30.0F, "a better kit, a quarter more");
          loaded.discard();
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aVampireThatEscapesItsHunterBecomesTheirNemesis(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, PLAYER);
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(2, 2, 2));
    vampire.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          MobNaming.ensure(vampire);
          vampire.setAttached(
              Downed.TYPE,
              new DownedState(DownedState.Phase.ESCAPING, 1, Optional.of(human.getUuid())));
          Downed.tick(vampire, ServerConfigService.current().downed());
          context.assertEquals(
              NemesisAttachment.get(vampire).map(NemesisMark::quarry),
              Optional.of(human.getUuid()),
              "the human who downed it is its quarry");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void endingANemesisDropsAKeepsakeBearingItsName(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, PLAYER);
    final HunterEntity hunter = hunter(context, 1);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          NemesisMaking.make(hunter, vampire.getUuid());
          final String name = hunter.getName().getString();
          hunter.damage(vampire.getDamageSources().playerAttack(vampire), 1000.0F);
          final List<ItemEntity> keepsakes =
              context
                  .getWorld()
                  .getEntitiesByClass(
                      ItemEntity.class,
                      context.getTestBox(),
                      item -> item.getStack().isOf(NemesisItems.BROKEN_STAKE));
          context.assertEquals(keepsakes.size(), 1, "one keepsake");
          context.assertTrue(
              keepsakes.get(0).getStack().getName().getString().contains(name),
              "the keepsake bears its name");
          context.assertTrue(
              NemesisLedger.of(context.getWorld().getServer()).of(vampire.getUuid()).isEmpty(),
              "the grudge is over");
          context.assertTrue(
              SkillAttachment.get(vampire, SkillTree.VAMPIRE).experience() > 0.0F
                  || SkillAttachment.get(vampire, SkillTree.VAMPIRE).earned() > 0,
              "the quarry is paid in vampire experience");
          keepsakes.forEach(ItemEntity::discard);
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 60)
  public void anOlderCopyLeavesOnceANewerOneHasVisited(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, PLAYER);
    final HunterEntity hunter = hunter(context, 1);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          NemesisMaking.make(hunter, vampire.getUuid());
          final NemesisLedger ledger = NemesisLedger.of(context.getWorld().getServer());
          final NemesisEntry entry =
              ledger.entry(vampire.getUuid(), hunter.getUuid()).orElseThrow();
          ledger.put(vampire.getUuid(), entry.visited(entry.nextVisit(), entry.token() + 1));
        });
    context.runAtTick(
        TestPlayers.READY + 40,
        () -> {
          context.assertTrue(hunter.isRemoved(), "the stale copy is gone");
          context.assertTrue(
              NemesisLedger.of(context.getWorld().getServer())
                  .entry(vampire.getUuid(), hunter.getUuid())
                  .isPresent(),
              "the grudge itself is kept");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aNemesisLeavingWithItsChunkRefreshesItsSavedState(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, PLAYER);
    final HunterEntity hunter = hunter(context, 1);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          NemesisMaking.make(hunter, vampire.getUuid());
          hunter.setHealth(7.0F);
          hunter.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
          final NbtCompound saved =
              NemesisLedger.of(context.getWorld().getServer())
                  .entry(vampire.getUuid(), hunter.getUuid())
                  .orElseThrow()
                  .snapshot();
          context.assertEquals(saved.getFloat("Health"), 7.0F, "the health it left with");
          TestPlayers.finish(context, vampire);
        });
  }

  private static void clearHunters(final TestContext context) {
    context
        .getWorld()
        .getEntitiesByClass(HunterEntity.class, context.getTestBox().expand(6.0), hunter -> true)
        .forEach(HunterEntity::discard);
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aNemesisAwayFromHomeComesBackFromItsSavedState(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, PLAYER);
    final HunterEntity hunter = hunter(context, 1);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          NemesisMaking.make(hunter, vampire.getUuid());
          NemesisGrowth.grow(
              context.getWorld(), hunter, NemesisAttachment.get(hunter).orElseThrow());
          hunter.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
          final NemesisLedger ledger = NemesisLedger.of(context.getWorld().getServer());
          final NemesisEntry entry =
              ledger.entry(vampire.getUuid(), hunter.getUuid()).orElseThrow();
          final BlockPos at = context.getAbsolutePos(new BlockPos(6, 2, 6));
          final MobEntity copy = NemesisVisits.comeTo(ledger, vampire, entry, at, 0L).orElseThrow();
          context.assertTrue(copy != hunter, "a copy comes from its saved state");
          context.assertEquals(copy.getUuid(), hunter.getUuid(), "the same nemesis");
          context.assertEquals(copy.getBlockPos(), at, "at the arrival spot");
          context.assertEquals(copy.getMaxHealth(), 30.0F, "its growth came with it");
          context.assertEquals(
              NemesisAttachment.get(copy).map(NemesisMark::token), Optional.of(1), "a new token");
          final NemesisEntry visited =
              ledger.entry(vampire.getUuid(), hunter.getUuid()).orElseThrow();
          context.assertEquals(visited.token(), 1, "the ledger follows the new copy");
          context.assertTrue(visited.nextVisit() >= 5L, "the next visit is nights away");
          context.assertTrue(copy.getTarget() == vampire, "it comes for its quarry");
          clearHunters(context);
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aLoadedNemesisIsMovedToItsQuarry(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, PLAYER);
    final HunterEntity hunter = hunter(context, 1);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          NemesisMaking.make(hunter, vampire.getUuid());
          final NemesisLedger ledger = NemesisLedger.of(context.getWorld().getServer());
          final BlockPos at = context.getAbsolutePos(new BlockPos(6, 2, 6));
          final MobEntity moved =
              NemesisVisits.comeTo(
                      ledger,
                      vampire,
                      ledger.entry(vampire.getUuid(), hunter.getUuid()).orElseThrow(),
                      at,
                      0L)
                  .orElseThrow();
          context.assertTrue(moved == hunter, "the loaded nemesis itself comes");
          context.assertEquals(moved.getBlockPos(), at, "at the arrival spot");
          context.assertEquals(
              ledger.entry(vampire.getUuid(), hunter.getUuid()).orElseThrow().token(),
              0,
              "no copy, so no new token");
          clearHunters(context);
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 60)
  public void anOlderCopyOfAnEndedNemesisLeavesAndAForgottenOneStays(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, PLAYER);
    final ServerPlayerEntity other = TestPlayers.vampire(context, PLAYER.east(2));
    final HunterEntity ended = hunter(context, 1);
    final HunterEntity forgotten = hunter(context, 3);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          NemesisMaking.make(ended, vampire.getUuid());
          NemesisMaking.make(forgotten, other.getUuid());
          NemesisLedger.of(context.getWorld().getServer()).end(vampire.getUuid(), ended.getUuid());
          final NemesisMark mark = NemesisAttachment.get(forgotten).orElseThrow();
          Nemeses.forget(other);
          forgotten.setAttached(NemesisAttachment.TYPE, mark);
        });
    context.runAtTick(
        TestPlayers.READY + 40,
        () -> {
          context.assertTrue(ended.isRemoved(), "an ended nemesis leaves no copy behind");
          context.assertFalse(forgotten.isRemoved(), "a forgotten nemesis lives on");
          context.assertTrue(NemesisAttachment.get(forgotten).isEmpty(), "as an ordinary hunter");
          TestPlayers.finish(context, vampire, other);
        });
  }
}
