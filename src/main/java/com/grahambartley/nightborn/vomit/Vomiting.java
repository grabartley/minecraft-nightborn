package com.grahambartley.nightborn.vomit;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.config.BloodSettings;
import com.grahambartley.nightborn.config.WardSettings;
import com.grahambartley.nightborn.network.PlayerCue;
import com.grahambartley.nightborn.network.PlayerCues;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.village.SuspicionCause;
import com.grahambartley.nightborn.village.VampireSigns;
import com.grahambartley.nightborn.weakened.Weakened;
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
      final PlayerEntity vampire,
      final ItemStack stack,
      final BloodSettings blood,
      final WardSettings wards) {
    final ItemStack leftBehind = Consumables.leftBehind(stack);
    vampire.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
    if (vampire instanceof ServerPlayerEntity server) {
      final GarlicMeal.Toll plain =
          GarlicMeal.Toll.plain(blood.vomitDamage(), NAUSEA_TICKS, BloodAttachment.get(server));
      final GarlicMeal.Toll toll = GarlicMeal.is(stack) ? GarlicMeal.worsen(plain, wards) : plain;
      sicken(server, toll);
      throwUp(server, toll.damage(), toll.nauseaTicks());
      Deeds.record(server, Deed.of(DeedKinds.FOOD_BROUGHT_UP));
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

  private static void sicken(final ServerPlayerEntity vampire, final GarlicMeal.Toll toll) {
    if (toll.weakenTicks() > 0) {
      Weakened.add(vampire, GarlicMeal.WEAKENED_SOURCE, toll.weakenStrength(), toll.weakenTicks());
    }
    if (toll.blood() != BloodAttachment.get(vampire)) {
      BloodAttachment.set(vampire, toll.blood());
    }
  }

  static void throwUp(final ServerPlayerEntity vampire, final int damage, final int nauseaTicks) {
    vampire.addStatusEffect(
        new StatusEffectInstance(StatusEffects.NAUSEA, nauseaTicks, 0, false, false, true));
    PlayerCues.send(vampire, PlayerCue.VOMIT);
    VampireSigns.shown(vampire, SuspicionCause.SEEN_BRINGING_UP_FOOD);
    if (damage > 0) {
      vampire.damage(NightbornDamage.vomit(vampire.getWorld()), damage);
    }
  }
}
