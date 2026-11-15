package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.ammo.AmmoItems;
import com.grahambartley.nightborn.ammo.BoltItem;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.gore.Gore;
import com.grahambartley.nightborn.gore.GoreEvents;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class GoreGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";

  private static <M extends MobEntity> M still(
      final TestContext context, final EntityType<M> type, final int x) {
    final M mob = context.spawnEntity(type, new BlockPos(x, 2, 3));
    mob.setAiDisabled(true);
    return mob;
  }

  private static ServerPlayerEntity hunterWith(final TestContext context, final ItemStack weapon) {
    final ServerPlayerEntity hunter = context.createMockCreativeServerPlayerInWorld();
    hunter.setStackInHand(Hand.MAIN_HAND, weapon);
    return hunter;
  }

  @GameTest(templateName = ARENA)
  public void aBiteSpraysFromABleedingVictimButNotFromASkeleton(final TestContext context) {
    final VampireEntity vampire = still(context, NightbornMobs.VAMPIRE, 1);
    final PigEntity pig = still(context, EntityType.PIG, 3);
    final MobEntity skeleton = still(context, EntityType.SKELETON, 5);
    final var bite = NightbornDamage.bite(context.getWorld(), vampire);
    context.assertEquals(Optional.of(Gore.BITE), GoreEvents.onHit(pig, bite), "bitten pig");
    context.assertEquals(Optional.empty(), GoreEvents.onHit(skeleton, bite), "bitten skeleton");
    context.complete();
  }

  @GameTest(templateName = ARENA)
  public void aSilverSwordDrawsBloodFromAVampireOnly(final TestContext context) {
    final VampireEntity vampire = still(context, NightbornMobs.VAMPIRE, 1);
    final PigEntity pig = still(context, EntityType.PIG, 3);
    final ServerPlayerEntity hunter = hunterWith(context, new ItemStack(GearItems.SILVER_SWORD));
    final var blow = context.getWorld().getDamageSources().playerAttack(hunter);
    context.assertEquals(Optional.of(Gore.SILVER), GoreEvents.onHit(vampire, blow), "vampire");
    context.assertEquals(Optional.empty(), GoreEvents.onHit(pig, blow), "pig");
    context.complete();
  }

  @GameTest(templateName = ARENA)
  public void aStakeSpraysOnAHitAndBurstsAVampireOnDeath(final TestContext context) {
    final VampireEntity vampire = still(context, NightbornMobs.VAMPIRE, 1);
    final ServerPlayerEntity hunter = context.createMockCreativeServerPlayerInWorld();
    final var stake = NightbornDamage.stake(context.getWorld(), hunter);
    context.assertEquals(Optional.of(Gore.STAKE), GoreEvents.onHit(vampire, stake), "hit");
    context.assertEquals(Optional.of(Gore.STAKED), GoreEvents.onDeath(vampire, stake), "death");
    context.complete();
  }

  @GameTest(templateName = ARENA)
  public void aVictimBittenToDeathIsDrained(final TestContext context) {
    final VampireEntity vampire = still(context, NightbornMobs.VAMPIRE, 1);
    final PigEntity pig = still(context, EntityType.PIG, 3);
    final var bite = NightbornDamage.bite(context.getWorld(), vampire);
    context.assertEquals(Optional.of(Gore.DRAINED), GoreEvents.onDeath(pig, bite), "drained");
    context.complete();
  }

  @GameTest(templateName = ARENA)
  public void aMeleeStakeBlowSpraysAsAStake(final TestContext context) {
    final VampireEntity vampire = still(context, NightbornMobs.VAMPIRE, 1);
    final ServerPlayerEntity hunter = hunterWith(context, new ItemStack(GearItems.WOODEN_STAKE));
    final var blow = context.getWorld().getDamageSources().playerAttack(hunter);
    context.assertEquals(Optional.of(Gore.STAKE), GoreEvents.onHit(vampire, blow), "stake");
    context.complete();
  }

  @GameTest(templateName = ARENA)
  public void aSilverBoltDrawsBloodFromAVampire(final TestContext context) {
    final VampireEntity vampire = still(context, NightbornMobs.VAMPIRE, 1);
    final ServerPlayerEntity hunter = context.createMockCreativeServerPlayerInWorld();
    final PersistentProjectileEntity bolt =
        (PersistentProjectileEntity)
            ((BoltItem) AmmoItems.SILVER_BOLT)
                .createProjectile(
                    context.getWorld(),
                    hunter,
                    new ItemStack(AmmoItems.SILVER_BOLT),
                    new ItemStack(Items.CROSSBOW),
                    false);
    final var shot = context.getWorld().getDamageSources().arrow(bolt, hunter);
    context.assertEquals(Optional.of(Gore.SILVER), GoreEvents.onHit(vampire, shot), "bolt");
    context.complete();
  }
}
