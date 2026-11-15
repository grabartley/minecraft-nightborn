package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public class FactionGameTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void factionIsKeptThroughDeathAndRespawn(final TestContext context) {
    for (final Faction faction : Faction.values()) {
      final ServerPlayerEntity dead = context.createMockCreativeServerPlayerInWorld();
      FactionAttachment.set(dead, faction);
      final ServerPlayerEntity respawned =
          context
              .getWorld()
              .getServer()
              .getPlayerManager()
              .respawnPlayer(dead, false, Entity.RemovalReason.KILLED);
      context.assertFalse(respawned == dead, "respawn should create a new player");
      context.assertEquals(
          FactionAttachment.get(respawned), faction, "faction after death and respawn");
    }
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void factionIsKeptThroughSaveAndLoad(final TestContext context) {
    for (final Faction faction : Faction.values()) {
      final ServerPlayerEntity saved = context.createMockCreativeServerPlayerInWorld();
      FactionAttachment.set(saved, faction);
      final NbtCompound nbt = saved.writeNbt(new NbtCompound());
      final ServerPlayerEntity loaded = freshPlayer(context, saved);
      loaded.readNbt(nbt);
      context.assertEquals(FactionAttachment.get(loaded), faction, "faction after save and load");
    }
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void humanSavesNoFactionData(final TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    FactionAttachment.set(player, Faction.VAMPIRE);
    FactionAttachment.set(player, Faction.HUMAN);
    final NbtCompound attachments =
        player.writeNbt(new NbtCompound()).getCompound(AttachmentTarget.NBT_ATTACHMENT_KEY);
    context.assertFalse(
        attachments.contains(FactionAttachment.ID.toString()),
        "a human should save no faction data");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void turningLeavesTheInventoryAlone(final TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    player.getInventory().setStack(0, new ItemStack(Items.IRON_SWORD));
    player.getInventory().setStack(9, new ItemStack(Items.BREAD, 12));
    player.getInventory().armor.set(3, new ItemStack(Items.LEATHER_HELMET));
    final NbtCompound before = player.writeNbt(new NbtCompound()).copy();

    FactionAttachment.set(player, Faction.VAMPIRE);

    final NbtCompound after = player.writeNbt(new NbtCompound());
    context.assertEquals(
        after.getList("Inventory", NbtCompound.COMPOUND_TYPE),
        before.getList("Inventory", NbtCompound.COMPOUND_TYPE),
        "inventory after turning");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void factionIsSentOnlyToItsOwnPlayer(final TestContext context) {
    final ServerPlayerEntity vampire = context.createMockCreativeServerPlayerInWorld();
    final ServerPlayerEntity onlooker = context.createMockCreativeServerPlayerInWorld();
    FactionAttachment.set(vampire, Faction.VAMPIRE);
    context.assertTrue(
        FactionAttachment.SYNC_PREDICATE.test(vampire, vampire),
        "a vampire should be sent their own faction");
    context.assertFalse(
        FactionAttachment.SYNC_PREDICATE.test(vampire, onlooker),
        "another player should never be sent a vampire's faction");
    context.complete();
  }

  private static ServerPlayerEntity freshPlayer(
      final TestContext context, final ServerPlayerEntity original) {
    return new ServerPlayerEntity(
        context.getWorld().getServer(),
        context.getWorld(),
        new GameProfile(UUID.randomUUID(), original.getGameProfile().getName()),
        SyncedClientOptions.createDefault());
  }
}
