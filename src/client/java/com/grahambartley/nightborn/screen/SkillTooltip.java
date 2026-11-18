package com.grahambartley.nightborn.screen;

import com.grahambartley.nightborn.client.PowerSlotView;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillNames;
import com.grahambartley.nightborn.skill.SkillPurchase;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public final class SkillTooltip {

  private SkillTooltip() {}

  public record Power(int blood, int cooldownTicks) {}

  public record Facts(
      int nextCost,
      int rank,
      int maxRank,
      List<Identifier> requires,
      Optional<Identifier> stage,
      Optional<Identifier> guildRank,
      Optional<Power> power) {

    public static Facts of(final Skill skill, final int rank) {
      return new Facts(
          skill.nextCost(rank),
          rank,
          skill.maxRank(),
          skill.requires(),
          skill.stage(),
          skill.rank(),
          skill.power().map(p -> new Power(p.blood(), p.cooldownTicks())));
    }
  }

  public static List<Text> lines(
      final Identifier id,
      final Facts facts,
      final SkillPurchase.Verdict verdict,
      final SkillTree tree,
      final int titleColour) {
    final List<Text> lines = new ArrayList<>();
    lines.add(
        Text.translatable(SkillNames.skill(id))
            .styled(style -> style.withColor(TextColor.fromRgb(titleColour))));
    lines.add(Text.translatable(SkillNames.description(id)).formatted(Formatting.GRAY));
    if (facts.maxRank() > 1) {
      lines.add(
          Text.translatable("nightborn.skills.rank", facts.rank(), facts.maxRank())
              .formatted(Formatting.WHITE));
    }
    if (facts.rank() < facts.maxRank()) {
      lines.add(
          Text.translatable("nightborn.skills.cost", facts.nextCost()).formatted(Formatting.WHITE));
    }
    if (!facts.requires().isEmpty()) {
      MutableText needs = Text.empty();
      for (int i = 0; i < facts.requires().size(); i++) {
        if (i > 0) {
          needs = needs.append(", ");
        }
        needs = needs.append(Text.translatable(SkillNames.skill(facts.requires().get(i))));
      }
      lines.add(Text.translatable("nightborn.skills.needs", needs).formatted(Formatting.WHITE));
    }
    facts
        .stage()
        .ifPresent(
            stage ->
                lines.add(
                    Text.translatable(
                            "nightborn.skills.stage", Text.translatable(SkillNames.stage(stage)))
                        .formatted(Formatting.WHITE)));
    facts
        .guildRank()
        .ifPresent(
            rank ->
                lines.add(
                    Text.translatable(
                            "nightborn.skills.guild_rank",
                            Text.translatable(SkillNames.guildRank(rank)))
                        .formatted(Formatting.WHITE)));
    facts.power().ifPresent(shown -> lines.add(powerLine(shown)));
    lines.add(status(verdict, tree, facts.rank()));
    if (facts.power().isPresent() && facts.rank() > 0) {
      lines.add(Text.translatable("nightborn.skills.bar.drag").formatted(Formatting.GRAY));
    }
    return lines;
  }

  static Text powerLine(final Power power) {
    final long seconds = PowerSlotView.seconds(power.cooldownTicks());
    final MutableText line =
        power.blood() > 0
            ? Text.translatable("nightborn.skills.power", power.blood(), seconds)
            : Text.translatable("nightborn.skills.power.free", seconds);
    return line.formatted(Formatting.DARK_RED);
  }

  static Text status(final SkillPurchase.Verdict verdict, final SkillTree tree, final int rank) {
    return switch (verdict) {
      case ALLOWED ->
          Text.translatable(
                  rank > 0 ? "nightborn.skills.click_to_rank" : "nightborn.skills.click_to_buy")
              .formatted(Formatting.GREEN);
      case OWNED ->
          Text.translatable(verdict.translationKey(tree)).formatted(Formatting.DARK_GREEN);
      default -> Text.translatable(verdict.translationKey(tree)).formatted(Formatting.RED);
    };
  }
}
