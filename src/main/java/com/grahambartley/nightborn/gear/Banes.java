package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.coating.Coating;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class Banes {

  public enum Bane {
    GARLIC,
    HOLY_WATER,
    SILVER,
    STAKE
  }

  public record Marks(Map<UUID, Set<Bane>> byAttacker) {

    public static final Marks NONE = new Marks(Map.of());

    public Marks {
      byAttacker = Map.copyOf(byAttacker);
    }

    public Set<Bane> of(final UUID attacker) {
      return byAttacker.getOrDefault(attacker, Set.of());
    }

    public Marks with(final UUID attacker, final Set<Bane> banes) {
      final Set<Bane> next = EnumSet.noneOf(Bane.class);
      next.addAll(of(attacker));
      next.addAll(banes);
      final Map<UUID, Set<Bane>> all = new HashMap<>(byAttacker);
      all.put(attacker, Set.copyOf(next));
      return new Marks(all);
    }
  }

  static final AttachmentType<Marks> MARKS =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "bane_marks"));

  private Banes() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (victim, source, base, taken, blocked) -> {
          if (!blocked) {
            struck(source.getAttacker(), victim, of(source));
          }
        });
    ServerLivingEntityEvents.AFTER_DEATH.register(
        (victim, source) -> struck(source.getAttacker(), victim, of(source)));
  }

  public static Set<Bane> of(final Coating coating) {
    return EnumSet.of(coating == Coating.GARLIC ? Bane.GARLIC : Bane.HOLY_WATER);
  }

  static Set<Bane> of(final DamageSource source) {
    return of(Staking.staked(source), VampireBane.isSilverBlow(source));
  }

  static Set<Bane> of(final boolean stake, final boolean silver) {
    final Set<Bane> banes = EnumSet.noneOf(Bane.class);
    if (stake) {
      banes.add(Bane.STAKE);
    }
    if (silver) {
      banes.add(Bane.SILVER);
    }
    return banes;
  }

  public static void struck(
      final Entity attacker, final LivingEntity victim, final Set<Bane> banes) {
    if (banes.isEmpty()
        || !(attacker instanceof ServerPlayerEntity player)
        || attacker == victim
        || !Vampires.isVampire(victim)) {
      return;
    }
    final Marks before = victim.getAttachedOrElse(MARKS, Marks.NONE);
    final Marks after = before.with(player.getUuid(), banes);
    if (after.of(player.getUuid()).size() > before.of(player.getUuid()).size()) {
      victim.setAttached(MARKS, after);
      Deeds.record(
          player,
          Deed.of(DeedKinds.BANES_DEALT).with(DeedKinds.BANES, after.of(player.getUuid()).size()));
    }
  }
}
