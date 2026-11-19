package com.grahambartley.nightborn.trap;

import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import com.grahambartley.nightborn.boss.ZoneShape;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class KillingGrounds {

  private record Field(
      RegistryKey<World> world, Vec3d centre, UUID owner, FieldPower power, long until) {

    boolean covers(final World at, final Vec3d point) {
      return world.equals(at.getRegistryKey())
          && FieldRules.lasts(at.getTime(), until)
          && FieldRules.inside(point.squaredDistanceTo(centre), power.radius());
    }
  }

  private static final List<Field> FIELDS = new ArrayList<>();

  private KillingGrounds() {}

  static void register() {
    ServerTickEvents.END_WORLD_TICK.register(
        world ->
            FIELDS.removeIf(
                field ->
                    field.world().equals(world.getRegistryKey())
                        && !FieldRules.lasts(world.getTime(), field.until())));
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

  public static boolean covers(final World world, final Vec3d point) {
    return FIELDS.stream().anyMatch(field -> field.covers(world, point));
  }

  public static boolean bars(final LivingEntity caster, final Identifier power) {
    if (!Vampires.isVampire(caster)) {
      return false;
    }
    return FIELDS.stream()
        .anyMatch(
            field ->
                FieldRules.bars(
                    field.power().bars().contains(power),
                    true,
                    field.covers(caster.getWorld(), caster.getPos()),
                    reaches(caster, field)));
  }

  private static boolean reaches(final LivingEntity caster, final Field field) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return false;
    }
    final Entity owner = world.getEntity(field.owner());
    if (owner instanceof LivingEntity living) {
      return PowerReach.touches(living, caster);
    }
    return !(caster instanceof PlayerEntity);
  }
}
