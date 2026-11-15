package com.grahambartley.nightborn.downed;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.DownedSettings;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Modifiers;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import org.jetbrains.annotations.Nullable;

public final class Downed {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "downed");

  public static final AttachmentType<DownedState> TYPE =
      AttachmentRegistry.create(ID, builder -> builder.persistent(DownedState.CODEC));

  static final Identifier PINNED = Identifier.of(Nightborn.MOD_ID, "downed_pinned");
  static final Identifier GROUNDED = Identifier.of(Nightborn.MOD_ID, "downed_grounded");
  static final int HEARTBEAT_TICKS = 30;
  static final float LAST_HEALTH = 1.0F;

  private Downed() {}

  public static void register() {
    ServerLivingEntityEvents.ALLOW_DAMAGE.register(Downed::allowDamage);
    ServerLivingEntityEvents.ALLOW_DEATH.register(Downed::allowDeath);
    AttackEntityCallback.EVENT.register(
        (player, world, hand, entity, hit) ->
            helpless(player) ? ActionResult.FAIL : ActionResult.PASS);
    AttackBlockCallback.EVENT.register(
        (player, world, hand, pos, direction) ->
            helpless(player) ? ActionResult.FAIL : ActionResult.PASS);
    UseBlockCallback.EVENT.register(
        (player, world, hand, hit) -> helpless(player) ? ActionResult.FAIL : ActionResult.PASS);
    UseEntityCallback.EVENT.register(
        (player, world, hand, entity, hit) ->
            helpless(player) ? ActionResult.FAIL : ActionResult.PASS);
    UseItemCallback.EVENT.register(
        (player, world, hand) ->
            helpless(player)
                ? TypedActionResult.fail(player.getStackInHand(hand))
                : TypedActionResult.pass(player.getStackInHand(hand)));
  }

  @Nullable
  public static DownedState state(final LivingEntity entity) {
    return entity.getAttached(TYPE);
  }

  public static boolean helpless(final PlayerEntity player) {
    if (player.getWorld().isClient) {
      return VisibleStateAttachment.get(player).downed();
    }
    final DownedState state = state(player);
    return state != null && state.isDowned();
  }

  public static void down(
      final LivingEntity vampire, final DownedSettings settings, final Optional<UUID> hunter) {
    vampire.setHealth(LAST_HEALTH);
    vampire.setAttached(TYPE, DownedRules.downed(settings, hunter));
    FeedingSessions.end(vampire);
    vampire
        .getWorld()
        .playSound(
            null,
            vampire.getBlockPos(),
            SoundEvents.ENTITY_PLAYER_BIG_FALL,
            SoundCategory.PLAYERS,
            1.0F,
            0.7F);
  }

  public static void tick(final LivingEntity vampire, final DownedSettings settings) {
    final DownedState state = state(vampire);
    if (state == null) {
      pin(vampire, false);
      return;
    }
    final DownedRules.Step step = DownedRules.tick(state, settings);
    vampire.setAttached(TYPE, step.state());
    switch (step.change()) {
      case STOOD_UP -> {
        vampire.setHealth(DownedRules.recoveredHealth(vampire.getMaxHealth(), settings));
        vampire
            .getWorld()
            .playSound(
                null,
                vampire.getBlockPos(),
                SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE,
                SoundCategory.PLAYERS,
                0.6F,
                0.6F);
      }
      case ESCAPED, NONE -> {}
    }
    final DownedState now = step.state();
    pin(vampire, now != null && now.isDowned());
    if (now != null && now.isDowned() && vampire.age % HEARTBEAT_TICKS == 0) {
      vampire
          .getWorld()
          .playSound(
              null,
              vampire.getBlockPos(),
              SoundEvents.ENTITY_WARDEN_HEARTBEAT,
              SoundCategory.PLAYERS,
              1.0F,
              0.8F);
    }
    if (vampire instanceof ServerPlayerEntity player && vampire.age % 20 == 0) {
      showCountdown(player, now);
    }
  }

  public static void clear(final LivingEntity entity) {
    entity.removeAttached(TYPE);
    pin(entity, false);
  }

  private static boolean allowDamage(
      final LivingEntity entity, final DamageSource source, final float amount) {
    if (!Vampires.isVampire(entity)) {
      return true;
    }
    return DownedRules.allowsDamage(state(entity), NightbornDamage.killsVampires(source));
  }

  private static boolean allowDeath(
      final LivingEntity entity, final DamageSource source, final float amount) {
    if (!Vampires.isVampire(entity)
        || !DownedRules.downsInsteadOfDying(NightbornDamage.killsVampires(source))) {
      return true;
    }
    down(entity, ServerConfigService.current().downed(), hunter(source));
    return false;
  }

  static Optional<UUID> hunter(final DamageSource source) {
    return source.getAttacker() instanceof PlayerEntity player
        ? DownedRules.hunter(!Vampires.isVampire(player), player.getUuid())
        : Optional.empty();
  }

  private static void pin(final LivingEntity entity, final boolean pinned) {
    final double value = pinned ? -1.0 : 0.0;
    Modifiers.set(
        entity,
        EntityAttributes.GENERIC_MOVEMENT_SPEED,
        PINNED,
        value,
        Operation.ADD_MULTIPLIED_TOTAL);
    Modifiers.set(
        entity,
        EntityAttributes.GENERIC_JUMP_STRENGTH,
        GROUNDED,
        value,
        Operation.ADD_MULTIPLIED_TOTAL);
  }

  private static void showCountdown(
      final ServerPlayerEntity player, @Nullable final DownedState state) {
    if (state == null) {
      return;
    }
    final String key =
        state.isDowned() ? "nightborn.downed.countdown" : "nightborn.escaping.countdown";
    player.sendMessage(Text.translatable(key, state.secondsLeft()), true);
  }
}
