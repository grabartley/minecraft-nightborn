package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.feeding.TurningBite;
import com.grahambartley.nightborn.feeding.TurningBiteToggle;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class TurningBiteGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireSwitchesTheirBiteBothWays(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.assertFalse(TurningBite.isOn(vampire), "a new vampire's bite feeds");
    context.assertTrue(TurningBiteToggle.toggle(vampire), "the switch is accepted");
    context.assertTrue(TurningBite.isOn(vampire), "the bite now turns");
    TurningBiteToggle.toggle(vampire);
    context.assertFalse(TurningBite.isOn(vampire), "and feeds again");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aHumanCannotSwitchABite(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    context.assertFalse(TurningBiteToggle.toggle(human), "a human has no bite to switch");
    context.assertFalse(TurningBite.isOn(human), "nothing is stored");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void beingMadeHumanClearsTheChoice(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    TurningBite.set(vampire, true);
    VampireTransitions.makeHuman(vampire);
    context.assertFalse(TurningBite.isOn(vampire), "a human keeps no turning bite");
    VampireTransitions.turn(vampire);
    context.assertFalse(TurningBite.isOn(vampire), "turning again starts with a feeding bite");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theChoiceIsKeptThroughDeath(final TestContext context) {
    final ServerPlayerEntity dead = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    TurningBite.set(dead, true);
    final ServerPlayerEntity respawned =
        context
            .getWorld()
            .getServer()
            .getPlayerManager()
            .respawnPlayer(dead, false, Entity.RemovalReason.KILLED);
    context.assertFalse(respawned == dead, "respawn makes a new player");
    context.assertTrue(TurningBite.isOn(respawned), "the turning bite is still on");
    TestPlayers.finish(context, respawned);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void theChoiceIsKeptThroughSaveAndLoad(final TestContext context) {
    final ServerPlayerEntity saved = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    TurningBite.set(saved, true);
    final NbtCompound nbt = saved.writeNbt(new NbtCompound());
    final ServerPlayerEntity loaded =
        new ServerPlayerEntity(
            context.getWorld().getServer(),
            context.getWorld(),
            new GameProfile(UUID.randomUUID(), saved.getGameProfile().getName()),
            SyncedClientOptions.createDefault());
    loaded.readNbt(nbt);
    context.assertTrue(TurningBite.isOn(loaded), "the turning bite is still on after a reload");
    TestPlayers.finish(context, saved);
  }
}
