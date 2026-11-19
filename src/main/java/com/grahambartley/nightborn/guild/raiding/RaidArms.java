package com.grahambartley.nightborn.guild.raiding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record RaidArms(Identifier stage, Optional<Identifier> veteran, List<RaidArms.Kits> kits) {

  public static final int MOST = 12;

  public record Kits(Identifier kit, int count) {
    public static final Codec<Kits> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Identifier.CODEC.fieldOf("kit").forGetter(Kits::kit),
                        Codec.intRange(1, MOST).fieldOf("count").forGetter(Kits::count))
                    .apply(instance, Kits::new));
  }

  public static final Codec<RaidArms> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("stage").forGetter(RaidArms::stage),
                      Identifier.CODEC.optionalFieldOf("veteran").forGetter(RaidArms::veteran),
                      Kits.CODEC.listOf().fieldOf("kits").forGetter(RaidArms::kits))
                  .apply(instance, RaidArms::new));

  public RaidArms {
    kits = List.copyOf(kits);
  }

  public List<Identifier> hunterKits() {
    final int room = MOST - (veteran.isPresent() ? 1 : 0);
    final List<Identifier> roster = new ArrayList<>();
    for (final Kits entry : kits) {
      for (int i = 0; i < entry.count() && roster.size() < room; i++) {
        roster.add(entry.kit());
      }
    }
    return roster;
  }

  public int size() {
    return hunterKits().size() + (veteran.isPresent() ? 1 : 0);
  }
}
