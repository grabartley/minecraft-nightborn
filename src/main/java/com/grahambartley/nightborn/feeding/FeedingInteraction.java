package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.enchant.EnchantedValues;
import com.grahambartley.nightborn.house.duel.DuelGuard;
import com.grahambartley.nightborn.moment.Moments;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.ward.Wards;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

final class FeedingInteraction {

  private FeedingInteraction() {}

  static ActionResult onUse(
      final PlayerEntity player,
      final World world,
      final Hand hand,
      final Entity entity,
      @Nullable final EntityHitResult hit) {
    if (hand != Hand.MAIN_HAND || !(entity instanceof LivingEntity victim)) {
      return ActionResult.PASS;
    }
    final Optional<BloodQuality> quality = BloodQuality.of(victim.getType());
    final boolean pvp =
        world.isClient
            || world.getServer().isPvpEnabled()
            || DuelGuard.forcesPvp(player.getUuid(), victim.getUuid());
    final BiteRules.Decision decision =
        BiteRules.decide(
            new BiteRules.Biter(
                Vampires.isVampire(player),
                player.isSneaking(),
                player.getMainHandStack().isEmpty(),
                Downed.helpless(player) || Moments.locked(player)),
            new BiteRules.Target(
                quality.isPresent(),
                Vampires.isVampire(victim),
                victim instanceof PlayerEntity,
                victim.isAlive(),
                Wards.wards(victim),
                BiteQuarry.finishable(victim)
                    && BiteRules.finishable(
                        victim.getHealth(),
                        victim.getMaxHealth(),
                        Math.round(
                            EnchantedValues.apply(
                                player.getMainHandStack(),
                                EnchantedValues.FINISHER_HEALTH,
                                ServerConfigService.current().feeding().finisherHealthPercent()))),
                BiteQuarry.guards(victim)),
            pvp);
    if (!decision.claimsTheInteraction()) {
      return ActionResult.PASS;
    }
    if (player instanceof ServerPlayerEntity vampire) {
      if (decision.bites() || decision.finishes()) {
        FeedingSessions.hold(vampire, victim, quality.orElseThrow(), decision.finishes());
      } else if (decision == BiteRules.Decision.PVP_OFF) {
        vampire.sendMessage(Text.translatable("nightborn.feeding.pvp_off"), true);
      } else if (decision == BiteRules.Decision.WARDED) {
        vampire.sendMessage(Text.translatable("nightborn.feeding.warded"), true);
      } else if (decision == BiteRules.Decision.GUARDED) {
        vampire.sendMessage(Text.translatable("nightborn.feeding.guarded"), true);
      }
    }
    return ActionResult.SUCCESS;
  }
}
