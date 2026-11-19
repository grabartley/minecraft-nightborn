package com.grahambartley.nightborn.inquisitor;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.boss.BloodShare;
import com.grahambartley.nightborn.boss.SetWear;
import com.grahambartley.nightborn.clothing.WornAttributes;
import com.grahambartley.nightborn.config.InquisitorSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Modifiers;
import com.grahambartley.nightborn.vampire.VampireAttributes;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class InquisitorSet {

  static final Identifier EXTRA_BLOOD = Identifier.of(Nightborn.MOD_ID, "inquisitor/set_blood");
  static final Identifier SLOWER_DRAIN =
      Identifier.of(Nightborn.MOD_ID, "inquisitor/set_slower_drain");
  static final AttachmentType<Float> CARRY =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "inquisitor_blood_carry"));
  static final int CHECK_TICKS = 20;

  private InquisitorSet() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(InquisitorSet::afterDamage);
    ServerTickEvents.END_SERVER_TICK.register(InquisitorSet::tick);
  }

  public static int share(
      final boolean vampire,
      final boolean blade,
      final boolean complete,
      final InquisitorSettings s) {
    if (!vampire || !blade) {
      return 0;
    }
    return complete ? s.setBlood() : s.greatswordBlood();
  }

  public static int extraBlood(
      final boolean vampire, final boolean complete, final InquisitorSettings s) {
    return vampire && complete ? s.setExtraBlood() : 0;
  }

  public static double drainChange(
      final boolean vampire, final boolean complete, final InquisitorSettings s) {
    return vampire && complete ? -s.setDrainSlower() / 100.0 : 0.0;
  }

  static boolean wearsAll(final LivingEntity wearer) {
    return SetWear.wearsAll(wearer, InquisitorSpoils.SET_ARMOR, InquisitorSpoils.SET_BLADES);
  }

  private static void afterDamage(
      final LivingEntity victim,
      final DamageSource source,
      final float base,
      final float taken,
      final boolean blocked) {
    if (!(source.getAttacker() instanceof LivingEntity attacker)
        || !source.isDirect()
        || attacker == victim) {
      return;
    }
    final int percent =
        share(
            Vampires.isVampire(attacker),
            SetWear.holds(attacker, InquisitorSpoils.SET_BLADES),
            wearsAll(attacker),
            ServerConfigService.current().inquisitor());
    if (percent <= 0) {
      return;
    }
    final BloodShare share =
        BloodShare.after(attacker.getAttachedOrElse(CARRY, 0.0F), taken, percent);
    attacker.setAttached(CARRY, share.carry());
    if (share.blood() > 0) {
      BloodAttachment.set(attacker, BloodAttachment.get(attacker) + share.blood());
    }
  }

  private static void tick(final MinecraftServer server) {
    if (server.getTicks() % CHECK_TICKS != 0) {
      return;
    }
    final InquisitorSettings settings = ServerConfigService.current().inquisitor();
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      final boolean vampire = Vampires.isVampire(player);
      final boolean complete = wearsAll(player);
      Modifiers.set(
          player,
          VampireAttributes.EXTRA_BLOOD,
          EXTRA_BLOOD,
          extraBlood(vampire, complete, settings),
          EntityAttributeModifier.Operation.ADD_VALUE);
      Modifiers.set(
          player,
          WornAttributes.BLOOD_DRAIN,
          SLOWER_DRAIN,
          drainChange(vampire, complete, settings),
          EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }
  }
}
