package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.downed.DownedState.Phase;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class DownedGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void swordFireAndFallDownAVampireInsteadOfKillingIt(final TestContext context) {
    final List<Function<DamageSources, DamageSource>> sources =
        List.of(
            DamageSources::generic,
            DamageSources::inFire,
            DamageSources::fall,
            DamageSources::lava);
    final List<ServerPlayerEntity> vampires = new ArrayList<>();
    for (int i = 0; i < sources.size(); i++) {
      vampires.add(TestPlayers.vampire(context, new BlockPos(2 + i, 2, 4)));
    }
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          for (int i = 0; i < sources.size(); i++) {
            final ServerPlayerEntity vampire = vampires.get(i);
            vampire.damage(sources.get(i).apply(vampire.getDamageSources()), 1000.0F);
            context.assertTrue(vampire.isAlive(), "a vampire survives source " + i);
            context.assertEquals(vampire.getHealth(), 1.0F, "a vampire stops at its last health");
            final DownedState state = Downed.state(vampire);
            context.assertTrue(state != null && state.isDowned(), "the vampire is downed");
          }
          TestPlayers.finish(context, vampires.toArray(ServerPlayerEntity[]::new));
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 40)
  public void theSunKillsADownedVampire(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.damage(vampire.getDamageSources().generic(), 1000.0F);
        });
    context.runAtTick(
        TestPlayers.READY + 25,
        () -> {
          vampire.damage(NightbornDamage.sun(context.getWorld()), 2.0F);
          context.assertTrue(vampire.isDead(), "sun finishes a downed vampire");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void theVoidStillKillsAVampire(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.damage(vampire.getDamageSources().outOfWorld(), 1000.0F);
          context.assertTrue(vampire.isDead(), "falling into the void is not survived");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aDownedOrEscapingVampireTakesNoOtherDamage(final TestContext context) {
    final ServerPlayerEntity downed = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity escaping = TestPlayers.vampire(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          for (final Phase phase : Phase.values()) {
            final ServerPlayerEntity vampire = phase == Phase.DOWNED ? downed : escaping;
            vampire.setHealth(8.0F);
            vampire.setAttached(Downed.TYPE, new DownedState(phase, 200));
            context.assertFalse(
                vampire.damage(vampire.getDamageSources().lava(), 10.0F), phase + " ignores lava");
            context.assertFalse(
                vampire.damage(vampire.getDamageSources().generic(), 10.0F),
                phase + " ignores blows");
            context.assertEquals(vampire.getHealth(), 8.0F, phase + " health is untouched");
          }
          TestPlayers.finish(context, downed, escaping);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void aDownedVampireGetsUpWithAShareOfItsHealthAndEscapes(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        5,
        () -> {
          vampire.setHealth(1.0F);
          vampire.setAttached(Downed.TYPE, new DownedState(Phase.DOWNED, 3));
        });
    context.runAtTick(
        7,
        () -> {
          context.assertTrue(
              VisibleStateAttachment.get(vampire).downed(), "everyone sees it is down");
          context.assertEquals(
              vampire.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED),
              0.0,
              "a downed vampire cannot move");
        });
    context.runAtTick(
        12,
        () -> {
          final DownedState state = Downed.state(vampire);
          context.assertTrue(state != null && !state.isDowned(), "it got up and is escaping");
          context.assertEquals(vampire.getHealth(), 8.0F, "it gets up with 20% of 40 health");
          context.assertTrue(
              vampire.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) > 0.0,
              "an escaping vampire can move");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void downedIsKeptThroughSaveAndLoad(final TestContext context) {
    final ServerPlayerEntity saved = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    saved.setAttached(Downed.TYPE, new DownedState(Phase.DOWNED, 321));
    final NbtCompound nbt = saved.writeNbt(new NbtCompound());
    final ServerPlayerEntity loaded =
        new ServerPlayerEntity(
            context.getWorld().getServer(),
            context.getWorld(),
            new GameProfile(UUID.randomUUID(), "loaded"),
            SyncedClientOptions.createDefault());
    loaded.readNbt(nbt);
    context.assertEquals(
        Downed.state(loaded), new DownedState(Phase.DOWNED, 321), "leaving does not escape it");
    TestPlayers.finish(context, saved);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void deathClearsTheDownedState(final TestContext context) {
    final ServerPlayerEntity dead = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    dead.setAttached(Downed.TYPE, new DownedState(Phase.ESCAPING, 100));
    final ServerPlayerEntity respawned =
        context
            .getWorld()
            .getServer()
            .getPlayerManager()
            .respawnPlayer(dead, false, Entity.RemovalReason.KILLED);
    context.assertTrue(Downed.state(respawned) == null, "death clears downed and escaping");
    TestPlayers.finish(context, respawned);
  }
}
