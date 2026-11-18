package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.batform.BatFormLimits;
import com.grahambartley.nightborn.smoke.SmokePoof;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import org.jetbrains.annotations.Nullable;

public final class MistForm {

  public static final TagKey<Block> PASSES =
      TagKey.of(RegistryKeys.BLOCK, Identifier.of(Nightborn.MOD_ID, "mist_passes"));
  public static final TagKey<DamageType> PASSES_THROUGH =
      TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Nightborn.MOD_ID, "passes_through_mist"));
  public static final TagKey<DamageType> HARMS =
      TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Nightborn.MOD_ID, "harms_mist"));

  public static final EntityDimensions DIMENSIONS = EntityDimensions.changing(0.5F, 0.5F);

  static final int LINGER_TICKS = 5;

  public static final AttachmentType<MistState> TYPE =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "mist_form"),
          builder ->
              builder
                  .persistent(MistState.CODEC)
                  .syncWith(MistState.PACKET_CODEC, AttachmentSyncPredicate.all()));

  private MistForm() {}

  public static void register() {
    ServerLivingEntityEvents.ALLOW_DAMAGE.register(
        (entity, source, amount) ->
            !passesThrough(entity, source) && !strikesFromMist(source.getAttacker(), source));
    UseItemCallback.EVENT.register(
        BatFormLimits.BEFORE_OTHERS,
        (player, world, hand) ->
            on(player)
                ? TypedActionResult.fail(player.getStackInHand(hand))
                : TypedActionResult.pass(player.getStackInHand(hand)));
    UseEntityCallback.EVENT.register(
        BatFormLimits.BEFORE_OTHERS,
        (player, world, hand, entity, hit) -> on(player) ? ActionResult.FAIL : ActionResult.PASS);
    AttackEntityCallback.EVENT.register(
        BatFormLimits.BEFORE_OTHERS,
        (player, world, hand, entity, hit) -> on(player) ? ActionResult.FAIL : ActionResult.PASS);
  }

  static boolean roomToStand(final LivingEntity vampire) {
    final EntityDimensions standing =
        (vampire instanceof PlayerEntity
                ? PlayerEntity.STANDING_DIMENSIONS
                : vampire.getType().getDimensions())
            .scaled(vampire.getScale());
    return vampire.getWorld().isSpaceEmpty(standing.getBoxAt(vampire.getPos()));
  }

  public static boolean on(final Entity entity) {
    return entity.getAttached(TYPE) != null;
  }

  public static boolean passes(final Entity entity, final BlockState state) {
    return on(entity) && state.isIn(PASSES);
  }

  static boolean strikesFromMist(@Nullable final Entity attacker, final DamageSource source) {
    return attacker != null && attacker == source.getSource() && on(attacker);
  }

  public static boolean passesThrough(final Entity entity, final DamageSource source) {
    return on(entity) && source.isIn(PASSES_THROUGH);
  }

  public static float harm(final Entity entity, final DamageSource source, final float amount) {
    final MistState state = entity.getAttached(TYPE);
    return state != null && source.isIn(HARMS) ? amount * state.harm() : amount;
  }

  public static void enter(final LivingEntity vampire, final MistPower power) {
    vampire.setAttached(
        TYPE, new MistState(vampire.getWorld().getTime() + power.durationTicks(), power.harm()));
    vampire.calculateDimensions();
    SmokePoof.burst(vampire, SoundEvents.ENTITY_ILLUSIONER_PREPARE_BLINDNESS);
  }

  public static void tick(final LivingEntity vampire) {
    final MistState state = vampire.getAttached(TYPE);
    if (state == null) {
      return;
    }
    if (state.over(vampire.getWorld().getTime())) {
      if (roomToStand(vampire)) {
        leave(vampire);
      } else {
        vampire.setAttached(
            TYPE, new MistState(vampire.getWorld().getTime() + LINGER_TICKS, state.harm()));
      }
      return;
    }
    if (vampire.getWorld() instanceof ServerWorld world && vampire.age % 2 == 0) {
      world.spawnParticles(
          ParticleTypes.WHITE_SMOKE,
          vampire.getX(),
          vampire.getY() + 0.3,
          vampire.getZ(),
          6,
          0.3,
          0.3,
          0.3,
          0.01);
    }
  }

  public static void leave(final LivingEntity vampire) {
    vampire.removeAttached(TYPE);
    vampire.calculateDimensions();
    SmokePoof.burst(vampire, SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE);
  }
}
