package com.grahambartley.nightborn.hold;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.vampire.Modifiers;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

public final class Holds {

  public static final TagKey<EntityType<?>> UNHOLDABLE =
      TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Nightborn.MOD_ID, "unholdable"));

  public static final AttachmentType<HoldState> TYPE =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "hold"),
          builder ->
              builder
                  .persistent(HoldState.CODEC)
                  .syncWith(HoldState.PACKET_CODEC, AttachmentSyncPredicate.all()));

  static final Identifier STILL = Identifier.of(Nightborn.MOD_ID, "hold/still");

  private Holds() {}

  public static void register() {
    AttackEntityCallback.EVENT.register(
        (player, world, hand, entity, hit) ->
            stopsAttacks(player) ? ActionResult.FAIL : ActionResult.PASS);
  }

  public static HoldState state(final Entity entity) {
    return entity.getAttachedOrElse(TYPE, HoldState.NONE);
  }

  public static Optional<HoldKind> held(final Entity entity) {
    return state(entity).active(entity.getWorld().getTime());
  }

  public static boolean pinned(final Entity entity) {
    return held(entity).filter(kind -> kind == HoldKind.PIN).isPresent();
  }

  public static boolean still(final Entity entity) {
    return held(entity).isPresent();
  }

  public static boolean stopsAttacks(final Entity entity) {
    return held(entity).map(HoldKind::stopsAttacks).orElse(false);
  }

  public static boolean stopsPower(final Entity entity, final boolean moves) {
    return held(entity).map(kind -> kind.stopsPower(moves)).orElse(false);
  }

  public static boolean hold(
      final LivingEntity caster, final LivingEntity target, final HoldKind kind, final int ticks) {
    final long now = target.getWorld().getTime();
    if (!PowerReach.touches(caster, target)
        || !HoldRules.takes(
            state(target), now, target.getType().isIn(UNHOLDABLE), target.isAlive(), false)) {
      return false;
    }
    target.setAttached(TYPE, HoldRules.start(kind, now, ticks, graceTicks()));
    stillen(target);
    if (target instanceof ServerPlayerEntity player) {
      player.sendMessage(Text.translatable("nightborn.hold." + kind.asString()), true);
    }
    target
        .getWorld()
        .playSound(
            null,
            target.getBlockPos(),
            kind == HoldKind.STAGGER
                ? SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK
                : SoundEvents.ENTITY_GOAT_RAM_IMPACT,
            SoundCategory.HOSTILE,
            1.0F,
            0.8F);
    return true;
  }

  public static void tick(final LivingEntity entity) {
    final HoldState state = entity.getAttached(TYPE);
    if (state == null) {
      return;
    }
    final long now = entity.getWorld().getTime();
    if (HoldRules.ended(state, now)) {
      entity.setAttached(TYPE, new HoldState(Optional.empty(), state.until(), state.immuneUntil()));
      free(entity);
      return;
    }
    if (state.held(now)) {
      stillen(entity);
    } else if (now >= state.immuneUntil()) {
      entity.removeAttached(TYPE);
    }
  }

  private static void stillen(final LivingEntity entity) {
    Modifiers.set(
        entity,
        EntityAttributes.GENERIC_MOVEMENT_SPEED,
        STILL,
        -1.0,
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    Modifiers.set(
        entity,
        EntityAttributes.GENERIC_JUMP_STRENGTH,
        STILL,
        -1.0,
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    entity.setVelocity(0.0, Math.min(0.0, entity.getVelocity().y), 0.0);
    entity.velocityModified = true;
    if (entity instanceof MobEntity mob) {
      mob.getNavigation().stop();
      if (stopsAttacks(mob)) {
        mob.goalSelector.getGoals().stream()
            .filter(PrioritizedGoal::isRunning)
            .forEach(PrioritizedGoal::stop);
        mob.setAttacking(false);
      }
    }
  }

  private static void free(final LivingEntity entity) {
    Modifiers.remove(entity, EntityAttributes.GENERIC_MOVEMENT_SPEED, STILL);
    Modifiers.remove(entity, EntityAttributes.GENERIC_JUMP_STRENGTH, STILL);
  }

  static int graceTicks() {
    return ServerConfigService.current().skills().holdGraceSeconds() * 20;
  }
}
