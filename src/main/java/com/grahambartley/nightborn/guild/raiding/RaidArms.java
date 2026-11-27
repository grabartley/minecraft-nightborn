package com.grahambartley.nightborn.guild.raiding;

import com.grahambartley.nightborn.mob.look.Weighted;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;

public record RaidArms(
    Identifier stage,
    Optional<Identifier> veteran,
    List<RaidArms.Kits> kits,
    Map<Identifier, Integer> rankWeights) {

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
      RecordCodecBuilder.<RaidArms>create(
              instance ->
                  instance
                      .group(
                          Identifier.CODEC.fieldOf("stage").forGetter(RaidArms::stage),
                          Identifier.CODEC.optionalFieldOf("veteran").forGetter(RaidArms::veteran),
                          Kits.CODEC.listOf().fieldOf("kits").forGetter(RaidArms::kits),
                          Codec.unboundedMap(
                                  Identifier.CODEC, Codec.intRange(0, Weighted.MAX_WEIGHT))
                              .optionalFieldOf("rank_weights", Map.of())
                              .forGetter(RaidArms::rankWeights))
                      .apply(instance, RaidArms::new))
          .validate(RaidArms::fits);

  static DataResult<RaidArms> fits(final RaidArms arms) {
    final int total =
        arms.kits().stream().mapToInt(Kits::count).sum() + (arms.veteran().isPresent() ? 1 : 0);
    return total <= MOST
        ? DataResult.success(arms)
        : DataResult.error(() -> "A raiding party holds at most " + MOST + ", got " + total);
  }

  public RaidArms {
    kits = List.copyOf(kits);
    rankWeights = Map.copyOf(rankWeights);
  }

  public boolean led(final int percent) {
    return veteran.isPresent() && percent > 0;
  }

  public List<Identifier> hunterKits(final int percent) {
    final int room = MOST - (led(percent) ? 1 : 0);
    final List<Identifier> roster = new ArrayList<>();
    for (final Kits entry : kits) {
      final int scaled = (int) Math.round(entry.count() * Math.max(0, percent) / 100.0);
      for (int i = 0; i < scaled && roster.size() < room; i++) {
        roster.add(entry.kit());
      }
    }
    return roster;
  }

  public int size(final int percent) {
    return hunterKits(percent).size() + (led(percent) ? 1 : 0);
  }
}
