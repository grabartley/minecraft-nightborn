package com.grahambartley.nightborn.slayer;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.hold.HoldKind;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.landing.Landing;
import com.grahambartley.nightborn.skill.Pounces;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.vanish.VanishSpots;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class Guards {

  public static final AttachmentType<GuardState> TYPE =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "guard"));

  private Guards() {}

  public static void raise(final LivingEntity hunter, final GuardPower power) {
    hunter.setAttached(
        TYPE, new GuardState(hunter.getWorld().getTime() + power.guardTicks(), power));
    hunter.swingHand(Hand.MAIN_HAND, true);
  }

  public static boolean up(final LivingEntity hunter) {
    final GuardState state = hunter.getAttached(TYPE);
    return state != null && state.up(hunter.getWorld().getTime());
  }

  public static void tick(final LivingEntity hunter) {
    final GuardState state = hunter.getAttached(TYPE);
    if (state == null || hunter.getWorld().isClient()) {
      return;
    }
    if (!state.up(hunter.getWorld().getTime()) || !hunter.isAlive()) {
      hunter.removeAttached(TYPE);
      return;
    }
    hunter
        .getWorld()
        .getEntitiesByClass(
            LivingEntity.class,
            hunter.getBoundingBox().expand(state.power().reach()),
            vampire ->
                Vampires.isVampire(vampire)
                    && state.meets(
                        hunter.distanceTo(vampire),
                        Charges.charging(vampire, state.power().chargeTicks())))
        .forEach(vampire -> turnBack(hunter, vampire, state.power()));
  }

  public static boolean turns(final LivingEntity hunter, final LivingEntity vampire) {
    final GuardState state = hunter.getAttached(TYPE);
    if (state == null || !state.up(hunter.getWorld().getTime()) || !Vampires.isVampire(vampire)) {
      return false;
    }
    return turnBack(hunter, vampire, state.power());
  }

  static boolean turnBack(
      final LivingEntity hunter, final LivingEntity vampire, final GuardPower power) {
    if (vampire == hunter || !PowerReach.touches(hunter, vampire)) {
      return false;
    }
    Pounces.stop(vampire);
    Charges.clear(vampire);
    if (PowerReach.marks(vampire)) {
      final Vec3d away =
          VanishSpots.toward(hunter.getPos(), vampire.getPos(), hunter.getRotationVector());
      Landing.reach(vampire, vampire.getEyePos().add(away.multiply(power.push())))
          .ifPresent(spot -> Landing.move(vampire, spot));
    }
    Holds.hold(hunter, vampire, HoldKind.STAGGER, power.staggerTicks(), true);
    hunter.swingHand(Hand.MAIN_HAND, true);
    hunter
        .getWorld()
        .playSound(
            null,
            hunter.getBlockPos(),
            SoundEvents.ITEM_SHIELD_BLOCK,
            SoundCategory.PLAYERS,
            1.0F,
            0.8F);
    if (hunter instanceof ServerPlayerEntity player) {
      player.sendMessage(Text.translatable("nightborn.slayer.riposte"), true);
    }
    return true;
  }
}
