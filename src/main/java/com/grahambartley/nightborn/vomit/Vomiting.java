package com.grahambartley.nightborn.vomit;

import com.grahambartley.nightborn.config.BloodSettings;
import com.grahambartley.nightborn.network.PlayerCue;
import com.grahambartley.nightborn.network.PlayerCues;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;

public final class Vomiting {

  static final int NAUSEA_TICKS = 120;

  private Vomiting() {}

  enum Leftover {
    KEEP,
    HAND_BACK_CONTAINER,
    POCKET_CONTAINER
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
      case KEEP -> stack;
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
      return Leftover.KEEP;
    }
    if (usedUp) {
      return Leftover.HAND_BACK_CONTAINER;
    }
    return noContainer ? Leftover.KEEP : Leftover.POCKET_CONTAINER;
  }

  static void throwUp(final ServerPlayerEntity vampire, final int damage) {
    vampire.addStatusEffect(
        new StatusEffectInstance(StatusEffects.NAUSEA, NAUSEA_TICKS, 0, false, false, true));
    PlayerCues.send(vampire, PlayerCue.VOMIT);
    if (damage > 0) {
      vampire.damage(NightbornDamage.vomit(vampire.getWorld()), damage);
    }
  }
}
