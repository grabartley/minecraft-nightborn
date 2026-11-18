package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.accessory.AccessoryItems;
import com.grahambartley.nightborn.accessory.AccessoryMove;
import com.grahambartley.nightborn.accessory.AccessoryPiece;
import com.grahambartley.nightborn.accessory.AccessorySlot;
import com.grahambartley.nightborn.clothing.ClothingItems;
import com.grahambartley.nightborn.clothing.ClothingPiece;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class AccessoryGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "accessories";
  static final String NIGHT_BATCH = "accessories-night";
  static final BlockPos SPOT = new BlockPos(4, 2, 4);
  static final int WORN = 5;
  static final int CHECK = 10;
  static final double EPSILON = 1.0E-6;

  @BeforeBatch(batchId = NIGHT_BATCH)
  public void pinNight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = NIGHT_BATCH)
  public void unpinNight(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void eachSlotHoldsOnlyWhatBelongsInIt(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    final ItemStack veil = new ItemStack(AccessoryItems.of(AccessoryPiece.MOURNING_VEIL));
    final ItemStack cape = new ItemStack(AccessoryItems.of(AccessoryPiece.HIGH_COLLARED_CAPE));
    final ItemStack hat = new ItemStack(ClothingItems.of(ClothingPiece.TOP_HAT));
    context.assertTrue(fits(human, AccessorySlot.FACE, veil), "a veil is worn on the face");
    context.assertFalse(fits(human, AccessorySlot.BACK, veil), "a veil is not worn on the back");
    context.assertTrue(fits(human, AccessorySlot.BACK, cape), "a cape is worn on the back");
    context.assertFalse(fits(human, AccessorySlot.FACE, hat), "a hat is not a face accessory");
    context.assertTrue(
        fits(
            human,
            AccessorySlot.SECOND_RING,
            new ItemStack(AccessoryItems.of(AccessoryPiece.ONYX_RING))),
        "a ring fits the second ring slot");
    context.assertFalse(
        human.getPreferredEquipmentSlot(cape) == EquipmentSlot.CHEST,
        "a cape no longer goes in the chest armour slot");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = WORN + 10)
  public void batwingOnCoatAndCapeCountsOnceAtTheHigherLevel(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    final double before = human.getAttributeValue(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE);
    final ItemStack coat = new ItemStack(ClothingItems.of(ClothingPiece.FROCK_COAT));
    coat.addEnchantment(enchantment(context, "batwing"), 1);
    final ItemStack cape = new ItemStack(AccessoryItems.of(AccessoryPiece.HIGH_COLLARED_CAPE));
    cape.addEnchantment(enchantment(context, "batwing"), 3);
    human.equipStack(EquipmentSlot.CHEST, coat);
    slot(human, AccessorySlot.BACK).setStack(0, cape);
    context.runAtTick(
        WORN,
        () -> {
          context.assertTrue(
              Math.abs(
                      before
                          + 3.0
                          - human.getAttributeValue(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE))
                  < EPSILON,
              "Batwing III on the cape and I on the coat counts once, at III");
          slot(human, AccessorySlot.BACK).setStack(0, ItemStack.EMPTY);
        });
    context.runAtTick(
        WORN + 2,
        () -> {
          context.assertTrue(
              Math.abs(
                      before
                          + 1.0
                          - human.getAttributeValue(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE))
                  < EPSILON,
              "taking the cape off leaves the coat's Batwing I");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aVeilLeftOnTheHeadMovesToTheFace(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    human.equipStack(
        EquipmentSlot.HEAD, new ItemStack(AccessoryItems.of(AccessoryPiece.MOURNING_VEIL)));
    AccessoryMove.fromArmour(human);
    context.assertTrue(human.getEquippedStack(EquipmentSlot.HEAD).isEmpty(), "the head is free");
    context.assertTrue(
        slot(human, AccessorySlot.FACE)
            .getStack(0)
            .isOf(AccessoryItems.of(AccessoryPiece.MOURNING_VEIL)),
        "the veil is now on the face");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aCapeWithNoRoomOnTheBackGoesToTheInventory(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    slot(human, AccessorySlot.BACK)
        .setStack(0, new ItemStack(AccessoryItems.of(AccessoryPiece.HIGH_COLLARED_CAPE)));
    human.equipStack(
        EquipmentSlot.CHEST, new ItemStack(AccessoryItems.of(AccessoryPiece.HIGH_COLLARED_CAPE)));
    AccessoryMove.fromArmour(human);
    context.assertTrue(human.getEquippedStack(EquipmentSlot.CHEST).isEmpty(), "the chest is free");
    context.assertTrue(
        human
            .getInventory()
            .contains(new ItemStack(AccessoryItems.of(AccessoryPiece.HIGH_COLLARED_CAPE))),
        "the second cape is kept in the inventory");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = CHECK + 10)
  public void nocturneOnAVeilSeesInTheDark(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    final ItemStack veil = new ItemStack(AccessoryItems.of(AccessoryPiece.MOURNING_VEIL));
    veil.addEnchantment(enchantment(context, "nocturne"), 1);
    slot(human, AccessorySlot.FACE).setStack(0, veil);
    context.runAtTick(
        CHECK,
        () -> {
          context.assertTrue(
              human.hasStatusEffect(StatusEffects.NIGHT_VISION),
              "Nocturne works from the face slot at night");
          TestPlayers.finish(context, human);
        });
  }

  private static boolean fits(
      final ServerPlayerEntity wearer, final AccessorySlot slot, final ItemStack stack) {
    final TrinketInventory inventory = slot(wearer, slot);
    return TrinketsApi.evaluatePredicateSet(
        inventory.getSlotType().getValidatorPredicates(),
        stack,
        new SlotReference(inventory, 0),
        wearer);
  }

  private static TrinketInventory slot(final ServerPlayerEntity wearer, final AccessorySlot slot) {
    return TrinketsApi.getTrinketComponent(wearer)
        .orElseThrow()
        .getInventory()
        .get(slot.group())
        .get(slot.slotName());
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
