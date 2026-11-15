package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Identifier;

public record TreeProgress(
    int earned, float blood, List<BoughtSkill> bought, List<Identifier> bloodlines) {

  public static final int MAX_POINTS = 100_000;

  public static final TreeProgress EMPTY = new TreeProgress(0, 0.0F, List.of(), List.of());

  public static final Codec<TreeProgress> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(0, MAX_POINTS)
                          .optionalFieldOf("earned", 0)
                          .forGetter(TreeProgress::earned),
                      Codec.floatRange(0.0F, Float.MAX_VALUE)
                          .optionalFieldOf("blood", 0.0F)
                          .forGetter(TreeProgress::blood),
                      BoughtSkill.CODEC
                          .listOf()
                          .optionalFieldOf("bought", List.of())
                          .forGetter(TreeProgress::bought),
                      Identifier.CODEC
                          .listOf()
                          .optionalFieldOf("bloodlines", List.of())
                          .forGetter(TreeProgress::bloodlines))
                  .apply(instance, TreeProgress::new));

  public TreeProgress {
    earned = Math.max(0, Math.min(MAX_POINTS, earned));
    blood = Math.max(0.0F, blood);
    bought = List.copyOf(bought);
    bloodlines = List.copyOf(bloodlines);
  }

  public int spent() {
    return bought.stream().mapToInt(BoughtSkill::paid).sum();
  }

  public int free() {
    return Math.max(0, earned - spent());
  }

  public boolean owns(final Identifier skill) {
    return bought.stream().anyMatch(b -> b.skill().equals(skill));
  }

  public List<Identifier> ownedInOrder() {
    return bought.stream().map(BoughtSkill::skill).toList();
  }

  public boolean isEmpty() {
    return equals(EMPTY);
  }

  public TreeProgress withEarned(final int points) {
    return new TreeProgress(points, blood, bought, bloodlines);
  }

  public TreeProgress addPoints(final int points) {
    return withEarned(earned + Math.max(0, points));
  }

  public TreeProgress addBlood(final float worth, final int bloodPerPoint) {
    if (worth <= 0.0F || bloodPerPoint <= 0) {
      return this;
    }
    final float total = blood + worth;
    final int points = (int) Math.floor(total / bloodPerPoint);
    return new TreeProgress(earned + points, total - points * bloodPerPoint, bought, bloodlines);
  }

  public TreeProgress buy(final Identifier skill, final int cost, final Identifier bloodline) {
    final List<BoughtSkill> nextBought = new ArrayList<>(bought);
    nextBought.add(new BoughtSkill(skill, cost));
    final List<Identifier> nextLines = new ArrayList<>(bloodlines);
    if (bloodline != null && !nextLines.contains(bloodline)) {
      nextLines.add(bloodline);
    }
    return new TreeProgress(earned, blood, nextBought, nextLines);
  }

  public TreeProgress refundAll() {
    return new TreeProgress(earned, blood, List.of(), List.of());
  }
}
