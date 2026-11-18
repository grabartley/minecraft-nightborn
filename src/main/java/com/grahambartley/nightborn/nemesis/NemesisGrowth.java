package com.grahambartley.nightborn.nemesis;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.stage.VampireStages;
import com.grahambartley.nightborn.name.Epithets;
import com.grahambartley.nightborn.name.MobNaming;
import com.grahambartley.nightborn.name.NameBearer;
import com.grahambartley.nightborn.name.NameDisplay;
import com.grahambartley.nightborn.name.NamePools;
import com.grahambartley.nightborn.nemesis.NemesisLines.Moment;
import com.grahambartley.nightborn.vampire.Modifiers;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class NemesisGrowth {

  static final Identifier BOOST = Identifier.of(Nightborn.MOD_ID, "nemesis_boost");

  private NemesisGrowth() {}

  public static void grow(final ServerWorld world, final MobEntity mob, final NemesisMark mark) {
    final NemesisLedger ledger = NemesisLedger.of(world.getServer());
    final Optional<NemesisEntry> entry =
        ledger.entry(mark.quarry(), mob.getUuid()).filter(held -> held.current(mark));
    final Optional<NemesisKinds.Found> found = NemesisKinds.of(mob);
    if (entry.isEmpty() || found.isEmpty()) {
      return;
    }
    final NemesisKind kind = found.get().kind();
    final Optional<Identifier> older = olderStage(world, mob);
    final NemesisRules.Growth growth =
        NemesisRules.growth(kind, mark.growths(), older.isPresent(), mob.getRandom().nextDouble());
    if (growth.ages() && mob instanceof VampireEntity vampire) {
      older.ifPresent(vampire::becomeStage);
    }
    if (growth.boosts()) {
      final double boost = NemesisRules.boost(kind, mark.growths() + 1);
      Modifiers.set(
          mob, EntityAttributes.GENERIC_MAX_HEALTH, BOOST, boost, Operation.ADD_MULTIPLIED_BASE);
      Modifiers.set(
          mob, EntityAttributes.GENERIC_ATTACK_DAMAGE, BOOST, boost, Operation.ADD_MULTIPLIED_BASE);
    }
    if (growth.retitles()) {
      final String current = mob instanceof NameBearer bearer ? bearer.epithet() : "";
      NemesisRules.newEpithet(
              NamePools.pool(world.getRegistryManager(), kind.epithets()).names(),
              current,
              mob.getRandom().nextInt(Integer.MAX_VALUE))
          .ifPresent(epithet -> retitle(mob, mark.given(), epithet));
    }
    mob.setHealth(mob.getMaxHealth());
    final NemesisMark grown = mark.grown();
    NemesisAttachment.set(mob, grown);
    ledger.put(
        mark.quarry(),
        entry.get().withSnapshot(mob.getName().getString(), NemesisMaking.snapshot(mob)));
    final ServerPlayerEntity quarry = world.getServer().getPlayerManager().getPlayer(mark.quarry());
    if (quarry != null) {
      NemesisTaunts.say(mob, quarry, Moment.FLED);
    }
  }

  static void retitle(final MobEntity mob, final String given, final String epithet) {
    if (mob instanceof NameBearer bearer) {
      bearer.bearEpithet(epithet);
      if (bearer.epithet().equals(epithet)) {
        MobNaming.give(mob, Epithets.join(given, epithet));
        return;
      }
    }
    final boolean female = mob instanceof NameBearer bearer && bearer.bearsWomansName();
    mob.setCustomName(
        Text.translatableWithFallback(
            NameDisplay.WITH_EPITHET,
            Epithets.join(given, epithet),
            Text.literal(given),
            Text.translatableWithFallback(Epithets.key(epithet, female), epithet)));
    mob.setCustomNameVisible(true);
  }

  private static Optional<Identifier> olderStage(final ServerWorld world, final MobEntity mob) {
    if (!(mob instanceof VampireEntity vampire)) {
      return Optional.empty();
    }
    final List<NemesisRules.Rung> rungs =
        VampireStages.stages(world.getRegistryManager()).entrySet().stream()
            .map(
                stage ->
                    new NemesisRules.Rung(
                        stage.getKey().toString(),
                        stage.getValue().rank(),
                        stage.getValue().weight()))
            .toList();
    return NemesisRules.nextRung(rungs, vampire.stageId().toString()).map(Identifier::of);
  }
}
