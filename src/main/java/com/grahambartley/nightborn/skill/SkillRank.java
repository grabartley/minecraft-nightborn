package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.faith.PrayerGift;
import com.grahambartley.nightborn.seeker.SeekGift;
import com.grahambartley.nightborn.slayer.StandGift;
import com.grahambartley.nightborn.trap.TrapGift;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record SkillRank(
    int cost,
    List<SkillAttribute> attributes,
    double bareHandDamage,
    List<PowerGift> powerGifts,
    List<TrapGift> trapGifts,
    List<PrayerGift> prayerGifts,
    List<StandGift> standGifts,
    List<SeekGift> seekGifts) {

  public static final Codec<SkillRank> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(0, 100).fieldOf("cost").forGetter(SkillRank::cost),
                      SkillAttribute.CODEC
                          .listOf()
                          .optionalFieldOf("attributes", List.of())
                          .forGetter(SkillRank::attributes),
                      Codec.doubleRange(0.0, 20.0)
                          .optionalFieldOf("bare_hand_damage", 0.0)
                          .forGetter(SkillRank::bareHandDamage),
                      PowerGift.CODEC
                          .listOf()
                          .optionalFieldOf("power_gifts", List.of())
                          .forGetter(SkillRank::powerGifts),
                      TrapGift.CODEC
                          .listOf()
                          .optionalFieldOf("trap_gifts", List.of())
                          .forGetter(SkillRank::trapGifts),
                      PrayerGift.CODEC
                          .listOf()
                          .optionalFieldOf("prayer_gifts", List.of())
                          .forGetter(SkillRank::prayerGifts),
                      StandGift.CODEC
                          .listOf()
                          .optionalFieldOf("stand_gifts", List.of())
                          .forGetter(SkillRank::standGifts),
                      SeekGift.CODEC
                          .listOf()
                          .optionalFieldOf("seek_gifts", List.of())
                          .forGetter(SkillRank::seekGifts))
                  .apply(instance, SkillRank::new));

  public SkillRank {
    attributes = List.copyOf(attributes);
    powerGifts = List.copyOf(powerGifts);
    trapGifts = List.copyOf(trapGifts);
    prayerGifts = List.copyOf(prayerGifts);
    standGifts = List.copyOf(standGifts);
    seekGifts = List.copyOf(seekGifts);
  }

  public static SkillRank costing(final int cost) {
    return new SkillRank(
        cost, List.of(), 0.0, List.of(), List.of(), List.of(), List.of(), List.of());
  }
}
