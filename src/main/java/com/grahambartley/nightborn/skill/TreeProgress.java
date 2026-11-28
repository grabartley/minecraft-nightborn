package com.grahambartley.nightborn.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record TreeProgress(
    int earned,
    float experience,
    List<BoughtSkill> bought,
    List<Identifier> lines,
    Optional<PowerLayout> bar) {

  public static final int MAX_POINTS = 100_000;

  public static final TreeProgress EMPTY = new TreeProgress(0, 0.0F, List.of(), List.of());

  public static final MapCodec<TreeProgress> MAP_CODEC =
      RecordCodecBuilder.mapCodec(
          instance ->
              instance
                  .group(
                      Codec.intRange(0, MAX_POINTS)
                          .optionalFieldOf("earned", 0)
                          .forGetter(TreeProgress::earned),
                      Codec.floatRange(0.0F, Float.MAX_VALUE)
                          .optionalFieldOf("experience", 0.0F)
                          .forGetter(TreeProgress::experience),
                      BoughtSkill.CODEC
                          .listOf()
                          .optionalFieldOf("bought", List.of())
                          .forGetter(TreeProgress::bought),
                      Identifier.CODEC
                          .listOf()
                          .optionalFieldOf("bloodlines", List.of())
                          .forGetter(TreeProgress::lines),
                      PowerLayout.CODEC.optionalFieldOf("bar").forGetter(TreeProgress::bar))
                  .apply(instance, TreeProgress::new));

  public static final Codec<TreeProgress> CODEC = MAP_CODEC.codec();

  public TreeProgress {
    earned = Math.max(0, Math.min(MAX_POINTS, earned));
    experience = Math.max(0.0F, experience);
    bought = List.copyOf(bought);
    lines = List.copyOf(lines);
  }

  public TreeProgress(
      final int earned,
      final float experience,
      final List<BoughtSkill> bought,
      final List<Identifier> lines) {
    this(earned, experience, bought, lines, Optional.empty());
  }

  public int spent() {
    return bought.stream().mapToInt(BoughtSkill::paid).sum();
  }

  public int free() {
    return Math.max(0, earned - spent());
  }

  public int rank(final Identifier skill) {
    return (int) bought.stream().filter(b -> b.skill().equals(skill)).count();
  }

  public boolean owns(final Identifier skill) {
    return rank(skill) > 0;
  }

  public int ownedIn(
      final Identifier line, final Function<Identifier, Optional<Identifier>> lineOf) {
    return (int)
        ownedInOrder().stream()
            .filter(skill -> lineOf.apply(skill).filter(line::equals).isPresent())
            .count();
  }

  public List<Identifier> ownedInOrder() {
    return bought.stream().map(BoughtSkill::skill).distinct().toList();
  }

  public boolean isEmpty() {
    return equals(EMPTY);
  }

  public boolean canSetEarned(final int points) {
    return points >= spent();
  }

  public TreeProgress withEarned(final int points) {
    return new TreeProgress(points, experience, bought, lines, bar);
  }

  public TreeProgress withBar(final PowerLayout layout) {
    return new TreeProgress(earned, experience, bought, lines, Optional.of(layout));
  }

  public TreeProgress addExperience(
      final float gained, final int firstCost, final int risePercent) {
    final SkillCurve.Gain gain =
        SkillCurve.add(earned, experience, gained, firstCost, risePercent, MAX_POINTS);
    return new TreeProgress(gain.earned(), gain.experience(), bought, lines, bar);
  }

  public TreeProgress buy(final Identifier skill, final int cost, @Nullable final Identifier line) {
    final List<BoughtSkill> nextBought = new ArrayList<>(bought);
    nextBought.add(new BoughtSkill(skill, cost));
    final List<Identifier> nextLines = new ArrayList<>(lines);
    if (line != null && !nextLines.contains(line)) {
      nextLines.add(line);
    }
    return new TreeProgress(earned, experience, nextBought, nextLines, bar);
  }

  public TreeProgress keeping(
      final int points, final List<BoughtSkill> kept, final List<Identifier> keptLines) {
    return new TreeProgress(
        points,
        experience,
        kept,
        keptLines,
        bar.map(layout -> layout.keeping(id -> kept.stream().anyMatch(b -> b.skill().equals(id)))));
  }

  public TreeProgress refundAll() {
    return keeping(earned, List.of(), List.of());
  }
}
