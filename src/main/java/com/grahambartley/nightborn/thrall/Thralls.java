package com.grahambartley.nightborn.thrall;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.MindSettings;
import com.grahambartley.nightborn.mind.MindAttributes;
import com.grahambartley.nightborn.mind.MindTies;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.UUID;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerData;

public final class Thralls {

  public static final EntityType<ThrallEntity> THRALL =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(Nightborn.MOD_ID, "thrall"),
          EntityType.Builder.create(ThrallEntity::new, SpawnGroup.MISC)
              .dimensions(0.6F, 1.95F)
              .eyeHeight(1.62F)
              .maxTrackingRange(10)
              .build());

  private Thralls() {}

  public static void register() {
    FabricDefaultAttributeRegistry.register(THRALL, ThrallEntity.createThrallAttributes());
  }

  public static BindRules.Outcome mayBind(
      final ServerPlayerEntity vampire, final LivingEntity victim, final float tookBlood) {
    final ServerWorld world = vampire.getServerWorld();
    final ThrallRegistry registry = ThrallRegistry.of(world.getServer());
    final MindSettings settings = ServerConfigService.current().minds();
    final BindRules.Outcome outcome =
        BindRules.decide(
            victim instanceof VillagerEntity villager && victim.isAlive() && !villager.isBaby(),
            MindTies.mesmerisedBy(victim, vampire),
            tookBlood > 0.0F,
            registry.count(vampire.getUuid()),
            MindAttributes.thrallLimit(vampire),
            world.getRandom().nextFloat(),
            settings.thrallChance());
    if (outcome == BindRules.Outcome.BOUND && victim instanceof VillagerEntity villager) {
      bind(vampire, villager, registry);
    }
    outcome
        .translationKey()
        .ifPresent(
            key -> vampire.sendMessage(Text.translatable(key, victim.getDisplayName()), true));
    return outcome;
  }

  private static void bind(
      final ServerPlayerEntity vampire,
      final VillagerEntity villager,
      final ThrallRegistry registry) {
    final VillagerData data = villager.getVillagerData();
    final VillagerMemory memory =
        VillagerMemory.of(villager, villager.getRegistryManager().getOps(NbtOps.INSTANCE));
    final ThrallEntity thrall = villager.convertTo(THRALL, true);
    if (thrall == null) {
      return;
    }
    villager.releaseTicketFor(MemoryModuleType.HOME);
    villager.releaseTicketFor(MemoryModuleType.JOB_SITE);
    villager.releaseTicketFor(MemoryModuleType.POTENTIAL_JOB_SITE);
    villager.releaseTicketFor(MemoryModuleType.MEETING_POINT);
    thrall.bind(vampire.getUuid(), data, memory);
    thrall.setHealth(villager.getHealth());
    registry.bind(vampire.getUuid(), thrall.getUuid());
    final ServerWorld world = vampire.getServerWorld();
    world.spawnParticles(
        ParticleTypes.SOUL, thrall.getX(), thrall.getEyeY(), thrall.getZ(), 8, 0.3, 0.4, 0.3, 0.01);
    world.playSound(
        null,
        thrall.getBlockPos(),
        SoundEvents.PARTICLE_SOUL_ESCAPE.value(),
        SoundCategory.NEUTRAL,
        1.0F,
        0.7F);
  }

  static void checkBond(final ServerWorld world, final ThrallEntity thrall) {
    final boolean kept =
        thrall
            .owner()
            .filter(owner -> ThrallRegistry.of(world.getServer()).keeps(owner, thrall.getUuid()))
            .isPresent();
    if (!kept) {
      release(thrall);
    }
  }

  public static boolean free(final ThrallEntity thrall) {
    if (!(thrall.getWorld() instanceof ServerWorld world) || !thrall.isAlive()) {
      return false;
    }
    thrall
        .owner()
        .ifPresent(owner -> ThrallRegistry.of(world.getServer()).free(owner, thrall.getUuid()));
    release(thrall);
    return true;
  }

  public static int freeOwner(final MinecraftServer server, final UUID owner) {
    return ThrallRegistry.of(server).freeOwner(owner);
  }

  static void release(final ThrallEntity thrall) {
    final VillagerData data = thrall.getVillagerData();
    final VillagerEntity villager = thrall.convertTo(EntityType.VILLAGER, true);
    if (villager != null) {
      villager.setVillagerData(data);
      thrall.memory().restore(villager);
      villager.setHealth(thrall.getHealth());
    }
  }
}
