package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.accessory.Accessories;
import com.grahambartley.nightborn.accessory.Accessory;
import com.grahambartley.nightborn.accessory.AccessoryHold;
import com.grahambartley.nightborn.accessory.AccessoryItems;
import com.grahambartley.nightborn.accessory.AccessoryMove;
import com.grahambartley.nightborn.accessory.AccessorySlot;
import com.grahambartley.nightborn.accessory.AccessoryStore;
import com.grahambartley.nightborn.accessory.WornStores;
import com.grahambartley.nightborn.ammo.AmmoItems;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.clothing.WornAttributes;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.holy.HolyWater;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class AccessoryGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final BlockPos SPOT = new BlockPos(4, 2, 4);
  static final int WORN = 5;

  @GameTest(templateName = ARENA, tickLimit = WORN + 10)
  public void aCapeOnTheBackGivesItsBatwing(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    final double before = human.getAttributeValue(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE);
    final ItemStack cape = new ItemStack(AccessoryItems.of("high_collared_cape"));
    cape.addEnchantment(
        context
            .getWorld()
            .getRegistryManager()
            .get(RegistryKeys.ENCHANTMENT)
            .getEntry(
                RegistryKey.of(
                    RegistryKeys.ENCHANTMENT, Identifier.of(Nightborn.MOD_ID, "batwing")))
            .orElseThrow(),
        3);
    slot(human, AccessorySlot.BACK).setStack(0, cape);
    context.runAtTick(
        WORN,
        () -> {
          context.assertEquals(
              before + 3.0,
              human.getAttributeValue(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE),
              "Batwing III on a cape worn on the back");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVeilLeftOnTheHeadMovesToTheFace(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    human.equipStack(EquipmentSlot.HEAD, new ItemStack(AccessoryItems.of("mourning_veil")));
    AccessoryMove.fromArmour(human);
    context.assertTrue(human.getEquippedStack(EquipmentSlot.HEAD).isEmpty(), "the head is free");
    context.assertTrue(
        slot(human, AccessorySlot.FACE).getStack(0).isOf(AccessoryItems.of("mourning_veil")),
        "the veil is now on the face");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, tickLimit = WORN + 10)
  public void aGarnetSignetSlowsOnlyAVampiresBlood(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, SPOT);
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT.east(2));
    slot(vampire, AccessorySlot.RING)
        .setStack(0, new ItemStack(AccessoryItems.of("garnet_signet")));
    slot(human, AccessorySlot.RING).setStack(0, new ItemStack(AccessoryItems.of("garnet_signet")));
    context.runAtTick(
        WORN,
        () -> {
          context.assertEquals(
              0.9,
              vampire.getAttributeValue(WornAttributes.BLOOD_DRAIN),
              "a vampire's blood drains a tenth slower");
          context.assertEquals(
              1.0,
              human.getAttributeValue(WornAttributes.BLOOD_DRAIN),
              "the ring does nothing on a human");
          TestPlayers.finish(context, vampire, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aBloodVialIsDrunkWhenTheBarRunsDry(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, SPOT);
    final ItemStack pendant = new ItemStack(AccessoryItems.of("blood_vial_pendant"));
    AccessoryStore.put(
        Accessories.of(context.getWorld().getRegistryManager(), pendant)
            .map(Accessory::holds)
            .orElseThrow(),
        pendant,
        new ItemStack(BloodItems.BLOOD_BOTTLE));
    slot(vampire, AccessorySlot.NECKLACE).setStack(0, pendant);
    BloodAttachment.set(vampire, 0);
    context.assertTrue(WornStores.drinkWhenDry(vampire), "the vial is drunk");
    context.assertTrue(BloodAttachment.get(vampire) > 0, "the blood bar fills");
    context.assertEquals(
        0,
        AccessoryStore.held(slot(vampire, AccessorySlot.NECKLACE).getStack(0)),
        "the vial is empty");
    context.assertTrue(
        vampire.getInventory().contains(new ItemStack(Items.GLASS_BOTTLE)), "the glass is kept");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, tickLimit = WORN + 10)
  public void aRingOfThePyreHalvesAFlamesBurn(final TestContext context) {
    final ServerPlayerEntity ringed = TestPlayers.vampire(context, SPOT);
    final ServerPlayerEntity bare = TestPlayers.vampire(context, SPOT.east(2));
    slot(ringed, AccessorySlot.RING)
        .setStack(0, new ItemStack(AccessoryItems.of("ring_of_the_pyre")));
    context.runAtTick(
        WORN,
        () -> {
          final float ringedBefore = ringed.getHealth();
          final float bareBefore = bare.getHealth();
          ringed.damage(ringed.getDamageSources().inFire(), 4.0F);
          bare.damage(bare.getDamageSources().inFire(), 4.0F);
          context.assertEquals(
              (bareBefore - bare.getHealth()) / 2.0F,
              ringedBefore - ringed.getHealth(),
              "the ring halves the burn against a bare vampire");
          TestPlayers.finish(context, ringed, bare);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aBandolierLoadsACrossbowBeforeTheInventory(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    final ItemStack belt = new ItemStack(AccessoryItems.of("bandolier"));
    AccessoryStore.put(holds(context, belt), belt, new ItemStack(AmmoItems.STAKE_BOLT, 5));
    slot(human, AccessorySlot.BELT).setStack(0, belt);
    final ItemStack crossbow = new ItemStack(Items.CROSSBOW);
    human.setStackInHand(Hand.MAIN_HAND, crossbow);
    crossbow.onStoppedUsing(context.getWorld(), human, 0);
    context.assertTrue(CrossbowItem.isCharged(crossbow), "the crossbow is loaded from the belt");
    context.assertEquals(
        4,
        AccessoryStore.held(slot(human, AccessorySlot.BELT).getStack(0)),
        "one bolt left the belt");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aBrokenStakeIsReplacedFromTheLoops(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    final ItemStack loops = new ItemStack(AccessoryItems.of("stake_loops"));
    AccessoryStore.put(holds(context, loops), loops, new ItemStack(GearItems.SILVER_STAKE));
    slot(human, AccessorySlot.BELT).setStack(0, loops);
    final ItemStack stake = new ItemStack(GearItems.WOODEN_STAKE);
    stake.setDamage(stake.getMaxDamage() - 1);
    human.setStackInHand(Hand.MAIN_HAND, stake);
    stake.damage(1, human, EquipmentSlot.MAINHAND);
    context.assertTrue(
        human.getMainHandStack().isOf(GearItems.SILVER_STAKE), "a stake comes to hand");
    context.assertEquals(
        0, AccessoryStore.held(slot(human, AccessorySlot.BELT).getStack(0)), "the loops are empty");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aThrownHolyWaterIsReplacedFromTheFlaskBelt(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    final ItemStack belt = new ItemStack(AccessoryItems.of("flask_belt"));
    AccessoryStore.put(
        holds(context, belt),
        belt,
        PotionContentsComponent.createStack(Items.SPLASH_POTION, HolyWater.POTION));
    slot(human, AccessorySlot.BELT).setStack(0, belt);
    human.setStackInHand(
        Hand.MAIN_HAND, PotionContentsComponent.createStack(Items.SPLASH_POTION, HolyWater.POTION));
    human.getMainHandStack().use(context.getWorld(), human, Hand.MAIN_HAND);
    context.assertTrue(
        human.getMainHandStack().isOf(Items.SPLASH_POTION), "a holy water comes to hand");
    context.assertEquals(
        0, AccessoryStore.held(slot(human, AccessorySlot.BELT).getStack(0)), "the belt is empty");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, tickLimit = WORN + 10)
  public void aTravellersCloakNeverFreezes(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, SPOT);
    slot(human, AccessorySlot.BACK)
        .setStack(0, new ItemStack(AccessoryItems.of("travellers_cloak")));
    context.runAtTick(
        WORN,
        () -> {
          context.assertFalse(human.canFreeze(), "the cloak keeps out the cold");
          context.assertTrue(
              PowderSnowBlock.canWalkOnPowderSnow(human), "the cloak walks on powder snow");
          TestPlayers.finish(context, human);
        });
  }

  private static List<AccessoryHold> holds(final TestContext context, final ItemStack piece) {
    return Accessories.of(context.getWorld().getRegistryManager(), piece)
        .map(Accessory::holds)
        .orElseThrow();
  }

  private static TrinketInventory slot(final ServerPlayerEntity wearer, final AccessorySlot slot) {
    return TrinketsApi.getTrinketComponent(wearer)
        .orElseThrow()
        .getInventory()
        .get(slot.group())
        .get(slot.slotName());
  }
}
