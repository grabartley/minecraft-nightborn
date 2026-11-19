package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.accessory.GiftScale;
import com.grahambartley.nightborn.servant.Servants;
import com.grahambartley.nightborn.vampire.Modifiers;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class GhostlyFamiliars {

  static final Identifier PACT_HEALTH = Identifier.of(Nightborn.MOD_ID, "familiar/pact_health");

  public static final EntityType<GhostlyFamiliarEntity> GHOSTLY_FAMILIAR =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(Nightborn.MOD_ID, "ghostly_familiar"),
          EntityType.Builder.create(GhostlyFamiliarEntity::new, SpawnGroup.MISC)
              .dimensions(0.6F, 1.4F)
              .eyeHeight(1.15F)
              .makeFireImmune()
              .maxTrackingRange(8)
              .build());

  private GhostlyFamiliars() {}

  static void register() {
    FabricDefaultAttributeRegistry.register(
        GHOSTLY_FAMILIAR, GhostlyFamiliarEntity.createFamiliarAttributes());
  }

  public static List<GhostlyFamiliarEntity> owned(final ServerWorld world, final UUID owner) {
    final List<GhostlyFamiliarEntity> owned = new ArrayList<>();
    world.collectEntitiesByType(
        GHOSTLY_FAMILIAR,
        familiar -> familiar.isAlive() && Servants.serves(familiar.owner(), owner),
        owned);
    return owned;
  }

  @Nullable
  public static GhostlyFamiliarEntity summon(final LivingEntity caster, final FamiliarPower power) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return null;
    }
    final List<GhostlyFamiliarEntity> owned =
        owned(world, caster.getUuid()).stream()
            .sorted(Comparator.comparingLong(GhostlyFamiliarEntity::summonedAt))
            .toList();
    final int dismiss =
        FamiliarRules.toDismiss(owned.size(), OccultAttributes.familiarLimit(caster));
    owned.stream().limit(dismiss).forEach(GhostlyFamiliarEntity::vanish);
    final GhostlyFamiliarEntity familiar = GHOSTLY_FAMILIAR.create(world);
    if (familiar == null) {
      return null;
    }
    final Vec3d ahead = caster.getPos().add(caster.getRotationVector().multiply(1.0, 0.0, 1.0));
    final Vec3d at =
        world.isSpaceEmpty(familiar, familiar.getType().getDimensions().getBoxAt(ahead))
            ? ahead
            : caster.getPos();
    familiar.refreshPositionAndAngles(at.x, at.y, at.z, caster.getHeadYaw(), 0.0F);
    familiar.summon(
        caster.getUuid(),
        power,
        GiftScale.ticks(
            power.lifetimeTicks(), GiftAttributes.value(caster, GiftAttributes.FAMILIAR_TIME)));
    Modifiers.set(
        familiar,
        EntityAttributes.GENERIC_MAX_HEALTH,
        PACT_HEALTH,
        OccultAttributes.familiarHealth(caster) - 1.0,
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    familiar.setHealth(familiar.getMaxHealth());
    world.spawnEntity(familiar);
    world.spawnParticles(
        ParticleTypes.SOUL,
        familiar.getX(),
        familiar.getBodyY(0.5),
        familiar.getZ(),
        12,
        0.3,
        0.3,
        0.3,
        0.02);
    return familiar;
  }
}
