package com.grahambartley.nightborn.slayer;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.hold.HoldKind;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.landing.Landing;
import com.grahambartley.nightborn.skill.PowerHeading;
import com.grahambartley.nightborn.skill.PowerSight;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.vanish.Vanish;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class Dashes {

  static final double THREAT_SEARCH = 8.0;

  public static final AttachmentType<DashState> TYPE =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "dash"));

  private Dashes() {}

  static void register() {
    ServerLivingEntityEvents.ALLOW_DAMAGE.register(
        (entity, source, amount) -> !slips(entity, source));
  }

  public static Optional<Vec3d> destination(
      final LivingEntity caster, final DashPower power, final PowerHeading held) {
    return Vanish.destination(caster, power.distance(), held);
  }

  public static void go(final LivingEntity caster, final Vec3d to, final DashPower power) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final long now = world.getTime();
    final Vec3d from = caster.getPos();
    caster.setAttached(TYPE, new DashState(now, power));
    final List<LivingEntity> threats =
        world.getEntitiesByClass(
            LivingEntity.class,
            caster.getBoundingBox().expand(THREAT_SEARCH),
            vampire -> threatens(vampire, caster, power));
    Landing.move(caster, to);
    world.spawnParticles(ParticleTypes.POOF, from.x, from.y + 0.2, from.z, 10, 0.3, 0.1, 0.3, 0.02);
    threats.forEach(vampire -> dodge(caster, vampire, power));
  }

  public static boolean dashing(final LivingEntity entity) {
    final DashState state = entity.getAttached(TYPE);
    return state != null && state.untouchable(entity.getWorld().getTime());
  }

  static boolean slips(final LivingEntity entity, final DamageSource source) {
    final DashState state = entity.getAttached(TYPE);
    if (state == null) {
      return false;
    }
    final long now = entity.getWorld().getTime();
    if (state.over(now)) {
      entity.removeAttached(TYPE);
      return false;
    }
    if (state.dodges(now)
        && source.getAttacker() instanceof LivingEntity attacker
        && Vampires.isVampire(attacker)) {
      dodge(entity, attacker, state.power());
    }
    return state.untouchable(now) && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY);
  }

  static boolean threatens(
      final LivingEntity vampire, final LivingEntity hunter, final DashPower power) {
    if (vampire == hunter || !Vampires.isVampire(vampire) || !vampire.isAlive()) {
      return false;
    }
    if (Charges.charging(vampire, power.dodgeTicks())) {
      return aims(vampire, hunter, THREAT_SEARCH);
    }
    return vampire.handSwinging
        && aims(
            vampire,
            hunter,
            vampire instanceof PlayerEntity player ? player.getEntityInteractionRange() : 0.0)
        && (!(vampire instanceof MobEntity mob) || mob.isInAttackRange(hunter));
  }

  private static boolean aims(
      final LivingEntity vampire, final LivingEntity hunter, final double range) {
    return vampire instanceof MobEntity mob
        ? mob.getTarget() == hunter
        : PowerSight.looked(vampire, range) == hunter;
  }

  private static void dodge(
      final LivingEntity hunter, final LivingEntity vampire, final DashPower power) {
    if (!Holds.hold(hunter, vampire, HoldKind.STAGGER, power.staggerTicks(), true)) {
      return;
    }
    if (hunter instanceof ServerPlayerEntity player) {
      player.sendMessage(Text.translatable("nightborn.slayer.dodged"), true);
    }
    DodgeRefunds.refund(hunter);
  }
}
