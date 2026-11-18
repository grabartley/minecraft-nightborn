package com.grahambartley.nightborn.nemesis;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.age.AgeNight;
import com.grahambartley.nightborn.boss.ArenaBoss;
import com.grahambartley.nightborn.config.VillageSettings;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.name.Epithets;
import com.grahambartley.nightborn.name.NameBearer;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.structure.VampireStructureStarts;
import com.grahambartley.nightborn.wild.WandererAttachment;
import java.util.UUID;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.Structure;

public final class NemesisMaking {

  public static final TagKey<Structure> SEALED_HOMES =
      TagKey.of(RegistryKeys.STRUCTURE, Identifier.of(Nightborn.MOD_ID, "sealed_homes"));

  private NemesisMaking() {}

  public static boolean make(final MobEntity mob, final UUID quarry) {
    if (!(mob.getWorld() instanceof ServerWorld world)) {
      return false;
    }
    return NemesisKinds.of(mob)
        .filter(
            found -> {
              final VillageSettings settings = ServerConfigService.current().villages();
              final NemesisLedger ledger = NemesisLedger.of(world.getServer());
              if (!NemesisRules.mayBecome(
                  mob.hasCustomName(),
                  mob instanceof ArenaBoss,
                  sealedHome(world, mob),
                  NemesisAttachment.get(mob).isPresent(),
                  ledger.of(quarry).size(),
                  settings.nemesisLimit())) {
                return false;
              }
              final long night = AgeNight.index(world.getServer().getOverworld().getTimeOfDay());
              final NemesisEntry entry =
                  new NemesisEntry(
                      mob.getUuid(),
                      found.id(),
                      mob.getName().getString(),
                      NemesisRules.nextVisit(
                          night,
                          settings.nemesisVisitFewest(),
                          settings.nemesisVisitMost(),
                          mob.getRandom().nextInt(Integer.MAX_VALUE)),
                      0,
                      new NbtCompound());
              if (!ledger.add(quarry, entry, settings.nemesisLimit())) {
                return false;
              }
              NemesisAttachment.set(mob, new NemesisMark(quarry, 0, 0, given(mob)));
              mob.setPersistent();
              if (mob instanceof HunterEntity hunter) {
                hunter.leaveParty();
              }
              WandererAttachment.clear(mob);
              NemesisWatch.track(mob);
              NemesisFights.settled(mob.getUuid(), quarry);
              ledger.put(quarry, entry.withSnapshot(entry.name(), snapshot(mob)));
              return true;
            })
        .isPresent();
  }

  public static NbtCompound snapshot(final MobEntity mob) {
    final NbtCompound nbt = new NbtCompound();
    mob.saveSelfNbt(nbt);
    return nbt;
  }

  static String given(final MobEntity mob) {
    final String name = mob.getName().getString();
    return mob instanceof NameBearer bearer
        ? Epithets.given(name, bearer.epithet()).orElse(name)
        : name;
  }

  private static boolean sealedHome(final ServerWorld world, final MobEntity mob) {
    return mob instanceof VampireEntity vampire
        && vampire
            .home()
            .flatMap(home -> VampireStructureStarts.startAt(world, home))
            .map(
                start ->
                    world
                        .getRegistryManager()
                        .get(RegistryKeys.STRUCTURE)
                        .getEntry(start.getStructure())
                        .isIn(SEALED_HOMES))
            .orElse(false);
  }
}
