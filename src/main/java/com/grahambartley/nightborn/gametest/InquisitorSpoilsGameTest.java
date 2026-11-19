package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodCapacity;
import com.grahambartley.nightborn.clothing.ClothingItems;
import com.grahambartley.nightborn.clothing.ClothingPiece;
import com.grahambartley.nightborn.clothing.Lining;
import com.grahambartley.nightborn.clothing.WornAttributes;
import com.grahambartley.nightborn.inquisitor.InquisitorSpoils;
import com.grahambartley.nightborn.inquisitor.InquisitorTrims;
import com.grahambartley.nightborn.inquisitor.TameHawkEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class InquisitorSpoilsGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final BlockPos BESIDE = new BlockPos(4, 2, 2);
  static final int ROLLS = 20;
  static final int BASE_SLOT = 1;
  static final int ADDITION_SLOT = 2;
  static final int RESULT_SLOT = 3;

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void everyKillGivesTreasureAndTheInquisitorsMail(final TestContext context) {
    final PigEntity body = context.spawnMob(EntityType.PIG, new BlockPos(4, 2, 4));
    body.setAiDisabled(true);
    final LootTable table =
        context
            .getWorld()
            .getServer()
            .getReloadableRegistries()
            .getLootTable(
                RegistryKey.of(
                    RegistryKeys.LOOT_TABLE,
                    Identifier.of(Nightborn.MOD_ID, "entities/grand_inquisitor")));
    final LootContextParameterSet parameters =
        new LootContextParameterSet.Builder(context.getWorld())
            .add(LootContextParameters.THIS_ENTITY, body)
            .add(LootContextParameters.ORIGIN, body.getPos())
            .add(
                LootContextParameters.DAMAGE_SOURCE,
                context.getWorld().getDamageSources().generic())
            .build(LootContextTypes.ENTITY);
    for (int roll = 0; roll < ROLLS; roll++) {
      final List<ItemStack> drops = table.generateLoot(parameters);
      final int mail =
          drops.stream()
              .filter(stack -> stack.isOf(InquisitorSpoils.MAIL))
              .mapToInt(ItemStack::getCount)
              .sum();
      context.assertTrue(mail >= 2 && mail <= 4, "a kill leaves two to four mail, not " + mail);
      context.assertTrue(drops.size() > 5, "a kill leaves treasure, not " + drops);
    }
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void theTrimsTheSongAndTheirRecipesLoad(final TestContext context) {
    final var registries = context.getWorld().getRegistryManager();
    context.assertTrue(
        registries.get(RegistryKeys.TRIM_PATTERN).contains(InquisitorTrims.CROSS), "cross trim");
    context.assertTrue(
        registries.get(RegistryKeys.TRIM_PATTERN).contains(InquisitorTrims.CHAIN), "chain trim");
    context.assertTrue(
        registries.get(RegistryKeys.JUKEBOX_SONG).contains(InquisitorSpoils.SONG), "the song");
    for (final String recipe :
        List.of(
            "cross_armor_trim_smithing_template",
            "cross_armor_trim_smithing_template_smithing_trim",
            "chain_armor_trim_smithing_template",
            "chain_armor_trim_smithing_template_smithing_trim",
            "inquisitor_mail_lining")) {
      context.assertTrue(
          context
              .getWorld()
              .getRecipeManager()
              .get(Identifier.of(Nightborn.MOD_ID, recipe))
              .isPresent(),
          recipe + " loaded");
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 120)
  public void mailLinesClothingAtASmithingTableAndKeepsItsDyeAndEnchantments(
      final TestContext context) {
    final ServerPlayerEntity smith = TestPlayers.human(context, BESIDE);
    final BlockPos table = new BlockPos(4, 2, 4);
    context.setBlockState(table, Blocks.SMITHING_TABLE);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final RegistryEntry<Enchantment> protection =
              context
                  .getWorld()
                  .getRegistryManager()
                  .get(RegistryKeys.ENCHANTMENT)
                  .getEntry(Enchantments.PROTECTION)
                  .orElseThrow();
          final ItemStack coat = new ItemStack(ClothingItems.of(ClothingPiece.values()[0]));
          coat.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x5A1020, true));
          coat.addEnchantment(protection, 3);
          final SmithingScreenHandler screen =
              new SmithingScreenHandler(
                  1,
                  smith.getInventory(),
                  ScreenHandlerContext.create(context.getWorld(), context.getAbsolutePos(table)));
          screen.getSlot(BASE_SLOT).setStack(coat.copy());
          screen.getSlot(ADDITION_SLOT).setStack(new ItemStack(InquisitorSpoils.MAIL, 2));
          final ItemStack lined = screen.getSlot(RESULT_SLOT).getStack().copy();
          context.assertTrue(screen.canUse(smith), "the smithing table is in reach");
          context.assertTrue(
              screen.getSlot(RESULT_SLOT).canTakeItems(smith),
              "the lined piece can be taken with the template slot empty");
          context.assertTrue(Lining.lined(lined), "the mail lines the piece, made " + lined);
          context.assertTrue(lined.isOf(coat.getItem()), "it is still the same piece");
          context.assertTrue(
              coat.get(DataComponentTypes.DYED_COLOR)
                  .equals(lined.get(DataComponentTypes.DYED_COLOR)),
              "it keeps its dye");
          context.assertTrue(
              lined.getEnchantments().getLevel(protection) == 3, "it keeps its enchantments");
          screen.getSlot(RESULT_SLOT).onTakeItem(smith, lined);
          context.assertTrue(
              screen.getSlot(BASE_SLOT).getStack().isEmpty(), "the plain piece is used up");
          context.assertTrue(
              screen.getSlot(ADDITION_SLOT).getStack().getCount() == 1, "one mail is used up");
          TestPlayers.finish(context, smith);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 200)
  public void theGreatswordFeedsAVampireAndTheFullSetHoldsMoreAndDrainsSlower(
      final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, BESIDE);
    final IronGolemEntity golem = context.spawnMob(EntityType.IRON_GOLEM, new BlockPos(4, 2, 4));
    golem.setAiDisabled(true);
    final var settings = ServerConfigService.current().inquisitor();
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final int plainCapacity = BloodCapacity.of(vampire);
          vampire.setStackInHand(Hand.MAIN_HAND, new ItemStack(InquisitorSpoils.GREATSWORD));
          BloodAttachment.set(vampire, 5);
          golem.damage(context.getWorld().getDamageSources().playerAttack(vampire), 10.0F);
          final float dealt = golem.getMaxHealth() - golem.getHealth();
          final int gained = (int) Math.floor(dealt * settings.greatswordBlood() / 100.0F);
          context.assertTrue(gained > 0, "the blow is big enough to give blood, dealt " + dealt);
          context.assertTrue(
              BloodAttachment.get(vampire) == 5 + gained,
              "the greatsword gives back " + gained + ", blood is " + BloodAttachment.get(vampire));
          vampire.equipStack(EquipmentSlot.HEAD, new ItemStack(InquisitorSpoils.HAT));
          vampire.equipStack(EquipmentSlot.CHEST, new ItemStack(InquisitorSpoils.COAT));
          vampire.equipStack(EquipmentSlot.LEGS, new ItemStack(InquisitorSpoils.GREAVES));
          vampire.equipStack(EquipmentSlot.FEET, new ItemStack(InquisitorSpoils.BOOTS));
          context.runAtTick(
              TestPlayers.READY + 25,
              () -> {
                context.assertTrue(
                    BloodCapacity.of(vampire) == plainCapacity + settings.setExtraBlood(),
                    "the full set raises " + plainCapacity + " to " + BloodCapacity.of(vampire));
                context.assertTrue(
                    Math.abs(
                            WornAttributes.value(vampire, WornAttributes.BLOOD_DRAIN)
                                - (1.0 - settings.setDrainSlower() / 100.0))
                        < 1.0E-6,
                    "the full set slows the drain");
                BloodAttachment.set(vampire, 5);
                final float before = golem.getHealth();
                golem.damage(context.getWorld().getDamageSources().playerAttack(vampire), 10.0F);
                final float setDealt = before - golem.getHealth();
                final int setGained = (int) Math.floor(setDealt * settings.setBlood() / 100.0F);
                context.assertTrue(
                    setGained > (int) Math.floor(setDealt * settings.greatswordBlood() / 100.0F),
                    "the full set gives more than the greatsword alone would for "
                        + setDealt
                        + " damage, "
                        + setGained);
                context.assertTrue(
                    BloodAttachment.get(vampire) == 5 + setGained,
                    "the full set gives back "
                        + setGained
                        + " for "
                        + setDealt
                        + " damage, blood is "
                        + BloodAttachment.get(vampire));
                BloodAttachment.set(vampire, BloodCapacity.of(vampire));
                golem.timeUntilRegen = 0;
                golem.damage(context.getWorld().getDamageSources().playerAttack(vampire), 10.0F);
                context.assertTrue(
                    BloodAttachment.get(vampire) == BloodCapacity.of(vampire),
                    "a full vampire is never pushed past its cap");
                vampire.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
              });
          context.runAtTick(
              TestPlayers.READY + 50,
              () -> {
                context.assertTrue(
                    BloodCapacity.of(vampire) == plainCapacity,
                    "putting the greatsword away takes the set bonus back");
                TestPlayers.finish(context, vampire);
              });
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 200)
  public void theGreatswordGivesAHumanNoBlood(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, BESIDE);
    final IronGolemEntity golem = context.spawnMob(EntityType.IRON_GOLEM, new BlockPos(4, 2, 4));
    golem.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          human.setStackInHand(Hand.MAIN_HAND, new ItemStack(InquisitorSpoils.GREATSWORD));
          final Integer before = human.getAttached(BloodAttachment.TYPE);
          golem.damage(context.getWorld().getDamageSources().playerAttack(human), 10.0F);
          context.assertTrue(golem.getHealth() < golem.getMaxHealth(), "the blow lands");
          context.assertTrue(
              Objects.equals(before, human.getAttached(BloodAttachment.TYPE)),
              "a human's blood is untouched");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 200)
  public void aTameHawkBelongsToWhoeverSetsItFreeAndComesBackToThem(final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.human(context, BESIDE);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final ItemStack stack = new ItemStack(InquisitorSpoils.TAME_HAWK);
          owner.setStackInHand(Hand.MAIN_HAND, stack);
          context.useStackOnBlock(owner, stack, new BlockPos(4, 1, 4), Direction.UP);
          final List<TameHawkEntity> hawks =
              context
                  .getWorld()
                  .getEntitiesByClass(
                      TameHawkEntity.class, context.getTestBox().expand(1.0), hawk -> true);
          hawks.forEach(hawk -> hawk.setAiDisabled(true));
          context.assertTrue(hawks.size() == 1, "one hawk is set free, not " + hawks.size());
          final TameHawkEntity hawk = hawks.getFirst();
          context.assertTrue(owner.getUuid().equals(hawk.owner()), "it belongs to who freed it");
          context.assertTrue(
              hawk.isInvulnerableTo(context.getWorld().getDamageSources().generic()),
              "nothing ordinary can hurt it");
          owner.setSneaking(true);
          hawk.interact(owner, Hand.MAIN_HAND);
          context.assertTrue(hawk.isRemoved(), "its sneaking owner picks it up");
          context.assertTrue(
              owner.getInventory().count(InquisitorSpoils.TAME_HAWK) == 1,
              "and gets the hawk back, not a bat");
          TestPlayers.finish(context, owner);
        });
  }
}
