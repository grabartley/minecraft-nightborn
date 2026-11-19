package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.coffin.CoffinOwnership;
import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.PowerSight;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public final class Scenes {

  public static final int LONGEST_TICKS = 72000;
  static final int MOST_KILLS = 256;
  static final double COFFIN_REACH = 2.0;

  static final int MOST_BITES = 1024;

  private static final Map<UUID, Scene> BITES =
      new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<UUID, Scene> eldest) {
          return size() > MOST_BITES;
        }
      };
  private static final Map<RegistryKey<World>, SceneKills> KILLS = new HashMap<>();

  private Scenes() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, base, taken, blocked) -> {
          if (taken > 0.0F && entity.isAlive() && bite(source)) {
            BITES.put(
                entity.getUuid(),
                VampireReading.of((LivingEntity) source.getAttacker(), entity.getPos()));
          }
        });
    ServerLivingEntityEvents.AFTER_DEATH.register(Scenes::died);
    ServerLifecycleEvents.SERVER_STOPPED.register(
        server -> {
          BITES.clear();
          KILLS.clear();
        });
  }

  public static Optional<Scene> read(final LivingEntity hunter, final ScenePower power) {
    if (!(hunter.getWorld() instanceof ServerWorld world)) {
      return Optional.empty();
    }
    final long now = world.getTime();
    final LivingEntity victim = PowerSight.looked(hunter, power.range());
    final BlockHitResult block =
        world.raycast(
            new RaycastContext(
                hunter.getEyePos(),
                hunter.getEyePos().add(hunter.getRotationVec(1.0F).multiply(power.range())),
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                hunter));
    final boolean hit = block.getType() == HitResult.Type.BLOCK;
    return pick(
        Optional.ofNullable(victim)
            .filter(hunter::canSee)
            .map(seen -> BITES.get(seen.getUuid()))
            .filter(bitten -> bitten.fresh(now, power.freshTicks())),
        () -> hit ? coffin(world, block) : Optional.empty(),
        () ->
            Optional.ofNullable(KILLS.get(world.getRegistryKey()))
                .flatMap(
                    kills ->
                        kills.nearest(
                            spot(hit, block.getPos(), hunter.getPos()),
                            power.killRadius(),
                            now,
                            power.freshTicks())));
  }

  public static Optional<Scene> pick(
      final Optional<Scene> victim,
      final Supplier<Optional<Scene>> coffin,
      final Supplier<Optional<Scene>> kill) {
    return victim.or(coffin).or(kill);
  }

  public static Vec3d spot(final boolean hit, final Vec3d block, final Vec3d hunter) {
    return hit ? block : hunter;
  }

  private static Optional<Scene> coffin(final ServerWorld world, final BlockHitResult block) {
    return CoffinOwnership.owner(world, block.getBlockPos())
        .map(owner -> keeper(world, owner, block.getPos()))
        .or(() -> sleeper(world, block.getBlockPos()));
  }

  private static Scene keeper(final ServerWorld world, final UUID owner, final Vec3d coffin) {
    final LivingEntity keeper = vampire(world, owner);
    return keeper == null
        ? Scene.far(owner, coffin, world.getTime())
        : VampireReading.of(keeper, coffin);
  }

  private static Optional<Scene> sleeper(final ServerWorld world, final BlockPos coffin) {
    if (!CoffinSleepers.isCoffin(world, coffin)) {
      return Optional.empty();
    }
    final int reach = ServerConfigService.current().mobs().vampireHomeRadius() * 2;
    return world
        .getEntitiesByClass(
            VampireEntity.class,
            new Box(coffin).expand(reach),
            vampire ->
                vampire.isAlive()
                    && vampire
                        .homeLife()
                        .coffin()
                        .map(home -> home.isWithinDistance(coffin, COFFIN_REACH))
                        .orElse(false))
        .stream()
        .findFirst()
        .map(vampire -> VampireReading.of(vampire, Vec3d.ofCenter(coffin)));
  }

  public static LivingEntity vampire(final ServerWorld world, final UUID id) {
    final Entity found =
        Optional.<Entity>ofNullable(world.getEntity(id))
            .orElseGet(() -> world.getServer().getPlayerManager().getPlayer(id));
    return found instanceof LivingEntity living
            && living.getWorld() == world
            && living.isAlive()
            && Vampires.isVampire(living)
        ? living
        : null;
  }

  private static boolean bite(final DamageSource source) {
    return source.isOf(NightbornDamage.BITE)
        && source.getAttacker() instanceof LivingEntity biter
        && Vampires.isVampire(biter);
  }

  private static void died(final LivingEntity victim, final DamageSource source) {
    BITES.remove(victim.getUuid());
    if (victim.getWorld() instanceof ServerWorld world
        && source.getAttacker() instanceof LivingEntity killer
        && killer != victim
        && Vampires.isVampire(killer)) {
      KILLS
          .computeIfAbsent(world.getRegistryKey(), key -> new SceneKills(MOST_KILLS))
          .add(VampireReading.of(killer, victim.getPos()));
    }
  }
}
