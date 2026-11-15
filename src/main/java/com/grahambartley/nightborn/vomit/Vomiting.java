package com.grahambartley.nightborn.vomit;

import com.grahambartley.nightborn.config.BloodSettings;
import com.grahambartley.nightborn.network.VomitPayload;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;

public final class Vomiting {

  static final int NAUSEA_TICKS = 120;

  private Vomiting() {}

  public enum Leftover {
    KEEP_STACK,
    HAND_BACK_CONTAINER,
    POCKET_CONTAINER,
    KEEP_REST
  }

  public static ItemStack swallow(
      final PlayerEntity vampire, final ItemStack stack, final BloodSettings settings) {
    final ItemStack leftBehind = Consumables.leftBehind(stack);
    vampire.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
    if (vampire instanceof ServerPlayerEntity server) {
      throwUp(server, settings.vomitDamage());
    }
    final boolean creative = vampire.isInCreativeMode();
    if (!creative) {
      stack.decrement(1);
    }
    return switch (leftover(creative, stack.isEmpty(), leftBehind.isEmpty())) {
      case KEEP_STACK, KEEP_REST -> stack;
      case HAND_BACK_CONTAINER -> leftBehind;
      case POCKET_CONTAINER -> {
        if (!vampire.getInventory().insertStack(leftBehind)) {
          vampire.dropItem(leftBehind, false);
        }
        yield stack;
      }
    };
  }

  static Leftover leftover(
      final boolean creative, final boolean usedUp, final boolean noContainer) {
    if (creative) {
      return Leftover.KEEP_STACK;
    }
    if (usedUp) {
      return Leftover.HAND_BACK_CONTAINER;
    }
    return noContainer ? Leftover.KEEP_REST : Leftover.POCKET_CONTAINER;
  }

  static void throwUp(final ServerPlayerEntity vampire, final int damage) {
    vampire.addStatusEffect(
        new StatusEffectInstance(StatusEffects.NAUSEA, NAUSEA_TICKS, 0, false, false, true));
    final VomitPayload payload = new VomitPayload(vampire.getId());
    ServerPlayNetworking.send(vampire, payload);
    PlayerLookup.tracking(vampire).forEach(watcher -> ServerPlayNetworking.send(watcher, payload));
    if (damage > 0) {
      vampire.damage(NightbornDamage.vomit(vampire.getWorld()), damage);
    }
  }
}
