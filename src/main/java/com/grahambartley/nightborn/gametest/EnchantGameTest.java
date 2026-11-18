package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodCapacity;
import com.grahambartley.nightborn.clothing.ClothingItems;
import com.grahambartley.nightborn.clothing.ClothingPiece;
import com.grahambartley.nightborn.clothing.WornAttributes;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class EnchantGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "enchantments";
  static final String NIGHT_BATCH = "enchantments-night";
  static final String NOON_BATCH = "enchantments-noon";
  static final BlockPos SPOT = new BlockPos(4, 2, 4);
  static final BlockPos PREY = new BlockPos(2, 2, 2);
  static final float BASE = 4.0F;
  static final int WORN = 5;

  @BeforeBatch(batchId = NIGHT_BATCH)
  public void pinNight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = NIGHT_BATCH)
  public void unpinNight(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = NOON_BATCH)
  public void pinNoon(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = NOON_BATCH)
  public void unpinNoon(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void bloodlettingGivesAVampireBloodEveryFewBlows(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, SPOT);
    final PigEntity pig = context.spawnEntity(EntityType.PIG, PREY);
    pig.setAiDisabled(true);
    vampire.setStackInHand(Hand.MAIN_HAND, enchanted(context, "bloodletting", 3));
    BloodAttachment.set(vampire, 10);
    blow(context, vampire, pig);
    context.assertEquals(10, BloodAttachment.get(vampire), "one blow at III is not yet enough");
    blow(context, vampire, pig);
    context.assertEquals(11, BloodAttachment.get(vampire), "the second blow at III gives blood");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void bloodlettingNeverPassesTheCap(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, SPOT);
    final PigEntity pig = context.spawnEntity(EntityType.PIG, PREY);
    pig.setAiDisabled(true);
    vampire.setStackInHand(Hand.MAIN_HAND, enchanted(context, "bloodletting", 3));
    final int full = BloodCapacity.of(vampire);
    BloodAttachment.set(vampire, full);
    blow(context, vampire, pig);
    blow(context, vampire, pig);
    context.assertEquals(full, BloodAttachment.get(vampire), "blood stays at its cap");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void bloodlettingDoesNothingInAHumansHand(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    final PigEntity pig = context.spawnEntity(EntityType.PIG, PREY);
    pig.setAiDisabled(true);
    human.setStackInHand(Hand.MAIN_HAND, enchanted(context, "bloodletting", 3));
    blow(context, human, pig);
    blow(context, human, pig);
    context.assertFalse(
        human.hasAttached(com.grahambartley.nightborn.enchant.BloodlettingEffect.BLOWS),
        "a human's blows are never counted");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 20)
  public void nightfallHitsHarderOnlyInAVampiresHand(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, SPOT);
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    final PigEntity pig = context.spawnEntity(EntityType.PIG, PREY);
    pig.setAiDisabled(true);
    final ItemStack sword = enchanted(context, "nightfall", 3);
    context.assertEquals(BASE + 3.0F, damage(context, vampire, sword, pig), "+3 at night");
    context.assertEquals(BASE, damage(context, human, sword, pig), "nothing in a human's hand");
    TestPlayers.finish(context, vampire, human);
  }

  @GameTest(templateName = ARENA, batchId = NOON_BATCH, tickLimit = 20)
  public void daybreakHitsVampiresHarderByDay(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, PREY);
    vampire.setAiDisabled(true);
    final PigEntity pig = context.spawnEntity(EntityType.PIG, new BlockPos(6, 2, 6));
    pig.setAiDisabled(true);
    final ItemStack sword = enchanted(context, "daybreak", 2);
    context.assertEquals(BASE + 2.0F, damage(context, human, sword, vampire), "+2 on a vampire");
    context.assertEquals(BASE, damage(context, human, sword, pig), "nothing on a pig");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void heartseekerSharpensAStakeOnAVampire(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, PREY);
    vampire.setAiDisabled(true);
    final PigEntity pig = context.spawnEntity(EntityType.PIG, new BlockPos(6, 2, 6));
    pig.setAiDisabled(true);
    final ItemStack stake = new ItemStack(GearItems.WOODEN_STAKE);
    stake.addEnchantment(enchantment(context, "heartseeker"), 3);
    final float staked = BASE + ServerConfigService.current().gear().stakeDamage();
    context.assertTrue(
        Math.abs(staked * 1.3F - damage(context, human, stake, vampire)) < 1.0E-4F,
        "Heartseeker III adds 30% to a stake's blow on a vampire");
    context.assertEquals(BASE, damage(context, human, stake, pig), "nothing on a pig");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = WORN + 10)
  public void thirstMakesBloodDrainFaster(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, SPOT);
    final ItemStack coat = new ItemStack(ClothingItems.of(ClothingPiece.FROCK_COAT));
    coat.addEnchantment(enchantment(context, "thirst"), 1);
    vampire.equipStack(EquipmentSlot.CHEST, coat);
    context.runAtTick(
        WORN,
        () -> {
          context.assertEquals(
              1.25,
              vampire.getAttributeValue(WornAttributes.BLOOD_DRAIN),
              "Thirst drains blood a quarter faster");
          TestPlayers.finish(context, vampire);
        });
  }

  private static void blow(
      final TestContext context, final ServerPlayerEntity attacker, final PigEntity target) {
    EnchantmentHelper.onTargetDamaged(
        context.getWorld(), target, context.getWorld().getDamageSources().playerAttack(attacker));
  }

  private static float damage(
      final TestContext context,
      final ServerPlayerEntity attacker,
      final ItemStack weapon,
      final net.minecraft.entity.Entity target) {
    return EnchantmentHelper.getDamage(
        context.getWorld(),
        weapon,
        target,
        context.getWorld().getDamageSources().playerAttack(attacker),
        BASE);
  }

  private static ItemStack enchanted(
      final TestContext context, final String name, final int level) {
    final ItemStack sword = new ItemStack(Items.IRON_SWORD);
    sword.addEnchantment(enchantment(context, name), level);
    return sword;
  }

  private static RegistryEntry<Enchantment> enchantment(
      final TestContext context, final String name) {
    return context
        .getWorld()
        .getRegistryManager()
        .get(RegistryKeys.ENCHANTMENT)
        .getEntry(RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(Nightborn.MOD_ID, name)))
        .orElseThrow();
  }
}
