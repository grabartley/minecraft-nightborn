package com.grahambartley.nightborn.batform;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.hold.HoldKind;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.skill.Boost;
import com.grahambartley.nightborn.skill.Boosts;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

public final class WingedForm {

  public static final AttachmentType<WingedState> TYPE =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "winged_form"),
          builder ->
              builder
                  .persistent(WingedState.CODEC)
                  .syncWith(WingedState.PACKET_CODEC, AttachmentSyncPredicate.all()));

  static final AttachmentType<Identifier> SOURCE =
      AttachmentRegistry.createPersistent(
          Identifier.of(Nightborn.MOD_ID, "winged_form_source"), Identifier.CODEC);

  private WingedForm() {}

  public static void register() {}

  public static boolean on(final Entity entity) {
    return entity.getAttached(TYPE) != null;
  }

  public static float scale(final Entity entity) {
    final WingedState state = entity.getAttached(TYPE);
    return state == null ? 1.0F : state.scale();
  }

  public static float damageTaken(final Entity entity) {
    final WingedState state = entity.getAttached(TYPE);
    return state == null || state.over(entity.getWorld().getTime()) ? 1.0F : state.damageTaken();
  }

  public static Optional<List<String>> forcedBy(final Entity entity) {
    return Optional.ofNullable(entity.getAttached(TYPE)).map(WingedState::forcedBy);
  }

  public static void enter(
      final LivingEntity vampire, final Identifier source, final WingedFormPower power) {
    final long now = vampire.getWorld().getTime();
    vampire.setAttached(TYPE, WingedState.of(now, power));
    vampire.setAttached(SOURCE, source);
    BatForm.enter(vampire);
    vampire.calculateDimensions();
    Boosts.start(vampire, source, Boost.of(now, power.durationTicks(), power.attributes()));
    vampire.playSound(SoundEvents.ENTITY_RAVAGER_ROAR, 1.5F, 0.7F);
    roar(vampire, power);
  }

  public static void tick(final LivingEntity vampire) {
    final WingedState state = vampire.getAttached(TYPE);
    if (state != null && state.over(vampire.getWorld().getTime())) {
      BatForm.leave(vampire);
    }
  }

  static void left(final LivingEntity vampire) {
    if (!on(vampire)) {
      return;
    }
    final Identifier source = vampire.getAttached(SOURCE);
    vampire.removeAttached(TYPE);
    vampire.removeAttached(SOURCE);
    if (source != null) {
      Boosts.end(vampire, source);
    }
  }

  public static boolean hears(final boolean listed, final boolean player, final boolean vampire) {
    return listed || player && !vampire;
  }

  private static void roar(final LivingEntity vampire, final WingedFormPower power) {
    final double radius = power.roarRadius();
    if (radius <= 0.0 || power.roarStaggerTicks() <= 0) {
      return;
    }
    for (final LivingEntity heard :
        vampire
            .getWorld()
            .getEntitiesByClass(
                LivingEntity.class,
                new Box(vampire.getBlockPos()).expand(radius),
                candidate ->
                    candidate.squaredDistanceTo(vampire) <= radius * radius
                        && hears(
                            candidate.getType().isIn(power.roarStaggers()),
                            candidate instanceof PlayerEntity,
                            Vampires.isVampire(candidate)))) {
      Holds.hold(vampire, heard, HoldKind.STAGGER, power.roarStaggerTicks());
    }
  }
}
