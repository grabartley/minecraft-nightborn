package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.config.FamiliarSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.wheel.CommandWheel;
import java.util.OptionalLong;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public final class FamiliarCare {

  private FamiliarCare() {}

  static ActionResult interact(
      final HumanFamiliarEntity familiar, final PlayerEntity player, final Hand hand) {
    final ItemStack stack = player.getStackInHand(hand);
    final boolean server = familiar.getWorld() instanceof ServerWorld;
    return switch (CareRules.choose(
        !server || familiar.isOwnedBy(player),
        hand == Hand.MAIN_HAND,
        stack.isOf(Items.EMERALD),
        player.getOffHandStack().isOf(Items.EMERALD))) {
      case PAY -> {
        if (server) {
          pay(familiar, player, stack);
        }
        yield ActionResult.success(!server);
      }
      case OTHER_HAND -> ActionResult.PASS;
      case NONE -> CommandWheel.use(player, hand, familiar);
    };
  }

  static void pay(
      final HumanFamiliarEntity familiar, final PlayerEntity player, final ItemStack emeralds) {
    final FamiliarSettings settings = ServerConfigService.current().familiars();
    final long today = FamiliarClock.today(familiar.getWorld());
    final OptionalLong paidThrough =
        Wages.pay(
            familiar.paidThroughDay(),
            today,
            settings.wageDaysPerEmerald(),
            settings.wageDaysAhead());
    if (paidThrough.isEmpty()) {
      player.sendMessage(Text.translatable("nightborn.familiar.wage.ahead"), true);
      return;
    }
    familiar.paidThroughDay(paidThrough.getAsLong());
    emeralds.decrementUnlessCreative(1, player);
    player.sendMessage(
        Text.translatable("nightborn.familiar.wage.paid", paidThrough.getAsLong() - today + 1),
        true);
    final ServerWorld world = (ServerWorld) familiar.getWorld();
    world.playSound(
        null,
        familiar.getBlockPos(),
        SoundEvents.ENTITY_VILLAGER_YES,
        SoundCategory.NEUTRAL,
        1.0F,
        1.0F);
    world.spawnParticles(
        ParticleTypes.HAPPY_VILLAGER,
        familiar.getX(),
        familiar.getBodyY(0.8),
        familiar.getZ(),
        5,
        0.3,
        0.3,
        0.3,
        0.0);
  }

  static void slighted(final HumanFamiliarEntity familiar, final DamageSource source) {
    final Entity attacker = source.getAttacker();
    final int loss =
        Slights.loyaltyLost(
            attacker instanceof PlayerEntity player && familiar.isOwnedBy(player),
            source.isOf(NightbornDamage.BITE),
            familiar.ticksSinceSlighted(),
            ServerConfigService.current().familiars());
    if (loss > 0) {
      familiar.changeLoyalty(-loss);
      familiar.markSlighted();
    }
  }
}
