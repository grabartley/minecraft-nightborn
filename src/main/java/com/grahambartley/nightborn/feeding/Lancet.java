package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodGifts;
import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.blood.BloodValue;
import com.grahambartley.nightborn.config.FeedingSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public final class Lancet {

  private Lancet() {}

  static ActionResult onUse(
      final PlayerEntity player,
      final World world,
      final Hand hand,
      final Entity entity,
      @Nullable final EntityHitResult hit) {
    final ItemStack lancet = player.getStackInHand(hand);
    if (!lancet.isOf(BloodItems.LANCET) || !(entity instanceof LivingEntity creature)) {
      return ActionResult.PASS;
    }
    final Optional<BloodQuality> quality = BloodQuality.of(creature.getType());
    final LancetRules rule = ruleFor(creature, hasEmptyBottle(player.getInventory()));
    if (player instanceof ServerPlayerEntity bleeder) {
      if (rule == LancetRules.BLEEDS) {
        bleed(
            bleeder,
            hand,
            creature,
            quality.orElseThrow(),
            BloodGifts.forPrey(bleeder.getRegistryManager(), creature.getType()));
      } else {
        refuse(bleeder, rule);
      }
    }
    return ActionResult.SUCCESS;
  }

  public static ActionResult onSelf(final PlayerEntity player, final Hand hand) {
    final LancetRules rule =
        LancetRules.onSelf(
            player.isSneaking(),
            Vampires.isVampire(player),
            hasEmptyBottle(player.getInventory()),
            player.getHealth(),
            draw());
    if (rule == LancetRules.PASS) {
      return ActionResult.PASS;
    }
    if (player instanceof ServerPlayerEntity bleeder) {
      if (rule == LancetRules.BLEEDS) {
        bleed(bleeder, hand, bleeder, BloodQuality.PLAYER, Optional.empty());
      } else {
        refuse(bleeder, rule);
      }
    }
    return ActionResult.SUCCESS;
  }

  public static LancetRules ruleFor(final LivingEntity creature, final boolean emptyBottle) {
    return LancetRules.onCreature(
        new LancetRules.Creature(
            BloodQuality.of(creature.getType()).isPresent(),
            Vampires.isVampire(creature),
            creature instanceof PlayerEntity,
            creature.isAlive(),
            creature.getHealth(),
            BiteQuarry.guards(creature)),
        emptyBottle,
        draw());
  }

  public static float draw() {
    final FeedingSettings feeding = ServerConfigService.current().feeding();
    return LancetRules.draw(feeding.lancetDamage(), feeding.bottleSize());
  }

  private static void bleed(
      final ServerPlayerEntity bleeder,
      final Hand hand,
      final LivingEntity creature,
      final BloodQuality quality,
      final Optional<Identifier> gift) {
    final Optional<ItemStack> bottle =
        drawBottle(bleeder.getServerWorld(), bleeder, creature, quality, gift);
    bleeder.swingHand(hand, true);
    if (bottle.isEmpty()) {
      return;
    }
    takeEmptyBottle(bleeder.getInventory());
    if (!BottleStowing.stow(bleeder.getInventory(), bottle.get())) {
      bleeder.dropItem(bottle.get(), false);
    }
    bleeder
        .getStackInHand(hand)
        .damage(
            1, bleeder, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
  }

  public static Optional<ItemStack> drawBottle(
      final ServerWorld world,
      final Entity bleeder,
      final LivingEntity creature,
      final BloodQuality quality,
      final Optional<Identifier> gift) {
    final float before = creature.getHealth();
    creature.damage(NightbornDamage.lancet(world, creature == bleeder ? null : bleeder), draw());
    final float taken = BloodValue.taken(before, creature.getHealth());
    if (taken < ServerConfigService.current().feeding().bottleSize()) {
      return Optional.empty();
    }
    world.playSound(
        null,
        creature.getBlockPos(),
        SoundEvents.ITEM_BOTTLE_FILL,
        SoundCategory.PLAYERS,
        1.0F,
        0.8F);
    world.emitGameEvent(bleeder, GameEvent.FLUID_PICKUP, creature.getBlockPos());
    return Optional.of(BloodBottleItem.of(quality, gift));
  }

  private static void refuse(final ServerPlayerEntity bleeder, final LancetRules rule) {
    rule.refusal().ifPresent(key -> bleeder.sendMessage(Text.translatable(key), true));
  }

  static boolean hasEmptyBottle(final PlayerInventory inventory) {
    return inventory.contains(new ItemStack(Items.GLASS_BOTTLE));
  }

  private static void takeEmptyBottle(final PlayerInventory inventory) {
    final int slot = inventory.getSlotWithStack(new ItemStack(Items.GLASS_BOTTLE));
    if (slot >= 0) {
      inventory.getStack(slot).decrement(1);
      inventory.markDirty();
    } else if (inventory.offHand.getFirst().isOf(Items.GLASS_BOTTLE)) {
      inventory.offHand.getFirst().decrement(1);
      inventory.markDirty();
    }
  }
}
