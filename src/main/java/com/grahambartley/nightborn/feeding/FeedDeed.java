package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;

public final class FeedDeed {

  private FeedDeed() {}

  static Deed of(final LivingEntity victim, final boolean seen, final boolean giftTaken) {
    final Optional<Identifier> profession =
        victim instanceof VillagerEntity villager
            ? Optional.of(villager.getVillagerData().getProfession())
                .filter(job -> job != VillagerProfession.NONE && job != VillagerProfession.NITWIT)
                .map(Registries.VILLAGER_PROFESSION::getId)
            : Optional.empty();
    return of(
        Registries.ENTITY_TYPE.getId(victim.getType()),
        victim instanceof AnimalEntity,
        victim instanceof VillagerEntity,
        profession,
        seen,
        giftTaken);
  }

  public static Deed of(
      final Identifier prey,
      final boolean animal,
      final boolean villager,
      final Optional<Identifier> profession,
      final boolean seen,
      final boolean giftTaken) {
    Deed deed =
        Deed.of(DeedKinds.FED)
            .with(DeedKinds.PREY, prey)
            .with(DeedKinds.ANIMAL, animal)
            .with(DeedKinds.VILLAGER, villager)
            .with(DeedKinds.SEEN, seen)
            .with(DeedKinds.GIFTED, giftTaken);
    if (profession.isPresent()) {
      deed = deed.with(DeedKinds.PROFESSION, profession.get());
    }
    return deed;
  }
}
