package com.grahambartley.nightborn.trap;

import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import com.grahambartley.nightborn.boss.ZoneShape;
import com.grahambartley.nightborn.config.WardSettings;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.ward.WardStrength;
import com.grahambartley.nightborn.ward.Wards;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class MarkedGrounds {

  public static final String WEAKENED_SOURCE = "consecrated";

  static final int PULSE_TICKS = 10;
  static final int LINGER_TICKS = PULSE_TICKS + 5;

  private record Field(
      RegistryKey<World> world, Vec3d centre, UUID owner, FieldPower power, long until) {

    boolean covers(final World at, final Vec3d point) {
      return world.equals(at.getRegistryKey())
          && FieldRules.lasts(at.getTime(), until)
          && FieldRules.inside(point.squaredDistanceTo(centre), power.radius());
    }
  }

  private static final List<Field> FIELDS = new ArrayList<>();

  private MarkedGrounds() {}

  static void register() {
    ServerTickEvents.END_WORLD_TICK.register(MarkedGrounds::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> clearAll());
  }

  public static void clearAll() {
    FIELDS.clear();
  }

  public static void mark(final LivingEntity caster, final FieldPower power) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final Vec3d centre = caster.getPos();
    FIELDS.add(
        new Field(
            world.getRegistryKey(),
            centre,
            caster.getUuid(),
            power,
            world.getTime() + power.durationTicks()));
    ZoneMarkEntity.place(
            world,
            ZoneShape.circle(centre.x, centre.z, power.radius()),
            centre.y + 0.02,
            Optional.of(power.look()))
        .linger(power.durationTicks());
  }

  public static boolean rearms(final World world, final Vec3d point) {
    return any(field -> field.power().rearmsTraps() && field.covers(world, point));
  }

  public static boolean holdsDown(final LivingEntity vampire) {
    return Vampires.isVampire(vampire)
        && any(
            field ->
                field.power().unwings()
                    && field.covers(vampire.getWorld(), vampire.getPos())
                    && reaches(vampire, field));
  }

  public static boolean bars(final LivingEntity caster, final Identifier power) {
    if (!Vampires.isVampire(caster)) {
      return false;
    }
    return any(
        field ->
            FieldRules.bars(
                field.power().bars().contains(power),
                field.covers(caster.getWorld(), caster.getPos()),
                reaches(caster, field)));
  }

  private static boolean any(final Predicate<Field> test) {
    return FIELDS.stream().anyMatch(test);
  }

  private static void tick(final ServerWorld world) {
    final long now = world.getTime();
    FIELDS.removeIf(
        field ->
            field.world().equals(world.getRegistryKey()) && !FieldRules.lasts(now, field.until()));
    for (final Field field : List.copyOf(FIELDS)) {
      if (field.world().equals(world.getRegistryKey())
          && field.power().pulses()
          && (field.until() - now) % PULSE_TICKS == 0) {
        pulse(world, field);
      }
    }
  }

  private static void pulse(final ServerWorld world, final Field field) {
    final LivingEntity owner = owner(world, field);
    final float radius = field.power().radius();
    for (final LivingEntity vampire :
        world.getEntitiesByClass(
            LivingEntity.class,
            new Box(field.centre(), field.centre()).expand(radius, 3.0, radius),
            candidate ->
                Vampires.isVampire(candidate)
                    && field.covers(world, candidate.getPos())
                    && reaches(candidate, field))) {
      if (field.power().unwings()) {
        Grounding.unwing(vampire);
      }
      if (field.power().weakens()) {
        weaken(vampire, owner);
      }
    }
  }

  private static void weaken(final LivingEntity vampire, @Nullable final LivingEntity owner) {
    final WardSettings settings = ServerConfigService.current().wards();
    final int strength =
        Wards.dulled(
            vampire,
            WardStrength.crucifix(
                settings.crucifixStrength(),
                owner == null ? 0.0 : HunterAttributes.faith(owner),
                settings.faithBonus()));
    final boolean fresh = Weakened.sources(vampire).strength(WEAKENED_SOURCE) == 0;
    Weakened.add(vampire, WEAKENED_SOURCE, strength, LINGER_TICKS);
    if (fresh && vampire instanceof ServerPlayerEntity player) {
      player.sendMessage(Text.translatable("nightborn.faith.consecrated"), true);
    }
  }

  @Nullable
  private static LivingEntity owner(final ServerWorld world, final Field field) {
    final Entity owner = world.getEntity(field.owner());
    return owner instanceof LivingEntity living ? living : null;
  }

  private static boolean reaches(final LivingEntity caster, final Field field) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return false;
    }
    final LivingEntity owner = owner(world, field);
    if (owner != null) {
      return PowerReach.touches(owner, caster);
    }
    return !(caster instanceof PlayerEntity);
  }
}
