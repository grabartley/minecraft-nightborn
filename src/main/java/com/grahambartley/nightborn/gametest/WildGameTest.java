package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.house.House;
import com.grahambartley.nightborn.house.Houses;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.structure.HouseholdRefill;
import com.grahambartley.nightborn.structure.HouseholdRoll;
import com.grahambartley.nightborn.wild.Wanderer;
import com.grahambartley.nightborn.wild.WandererAttachment;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public final class WildGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final long FEWEST = 15L * NightTime.DAY_LENGTH;
  static final long MOST = 40L * NightTime.DAY_LENGTH;

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aWatcherNeverTakesAVictimButAWildVampireMay(final TestContext context) {
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(2, 2, 2));
    villager.setAiDisabled(true);
    final VampireEntity wild = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
    wild.setAiDisabled(true);
    final VampireEntity watcher = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(5, 2, 5));
    watcher.setAiDisabled(true);
    final long now = context.getWorld().getTime();
    WandererAttachment.set(wild, Wanderer.wild(now));
    WandererAttachment.set(watcher, Wanderer.watching(new UUID(0, 1), now));
    context.assertTrue(wild.canTarget(villager), "a wild vampire may take a villager");
    context.assertFalse(watcher.canTarget(villager), "a watcher never takes anyone");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void anEmptiedHouseholdRefillsFifteenToFortyDaysOn(final TestContext context) {
    final HouseholdRoll roll = HouseholdRoll.get(context.getWorld().getServer());
    final GlobalPos home =
        GlobalPos.create(
            context.getWorld().getRegistryKey(), context.getAbsolutePos(new BlockPos(3, 2, 3)));
    final UUID last = UUID.randomUUID();
    roll.enlist(home, last);
    roll.forget(home, last);
    final long now = context.getWorld().getTimeOfDay();
    HouseholdRefill.sweep(context.getWorld());
    final long at = roll.record(home).refillAt().orElse(Long.MIN_VALUE);
    context.assertTrue(
        at >= now + FEWEST && at <= now + MOST,
        "an emptied household refills 15 to 40 days on, not at " + at + " from " + now);
    context.assertFalse(
        roll.record(home).refillDue(now), "a newly emptied household is not refilled at once");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aClaimedStructureNeverRefills(final TestContext context) {
    final MinecraftServer server = context.getWorld().getServer();
    final BlockPos home = context.getAbsolutePos(new BlockPos(2, 2, 3));
    final GlobalPos at = GlobalPos.create(context.getWorld().getRegistryKey(), home);
    final UUID last = UUID.randomUUID();
    HouseholdRoll.get(server).enlist(at, last);
    HouseholdRoll.get(server).forget(at, last);
    context.assertTrue(
        HouseholdRefill.mayArrive(context.getWorld(), home),
        "an emptied, unclaimed structure may take a new household");
    Houses.get(server)
        .put(
            House.founded(UUID.randomUUID(), GlobalPos.create(at.dimension(), home.up()))
                .seatedAt(Optional.of(at)));
    context.assertFalse(
        HouseholdRefill.mayArrive(context.getWorld(), home),
        "a structure a player has claimed never takes a new household");
    context.assertFalse(
        HouseholdRefill.arrive(context.getWorld(), home), "no household moves into it");
    context.complete();
  }
}
