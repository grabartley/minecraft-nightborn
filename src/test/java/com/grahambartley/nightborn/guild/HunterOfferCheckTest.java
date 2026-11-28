package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.rank.GuildRank;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillTree;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradedItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HunterOfferCheckTest {

  private static final Identifier SLAYER = Identifier.of("nightborn", "slayer");
  private static final Identifier HUNTER = Identifier.of("nightborn", "hunter");

  private static GuildOffer sale(
      final int emeralds,
      final Item gives,
      final int count,
      final int bottles,
      final Optional<Identifier> rank) {
    return new GuildOffer(
        0,
        new TradedItem(Items.EMERALD, emeralds),
        bottles > 0 ? Optional.of(new TradedItem(Items.GLASS_BOTTLE, bottles)) : Optional.empty(),
        new ItemStack(gives, count),
        1,
        rank);
  }

  private static List<String> check(final HunterOffer offer, final List<GuildOffer> master) {
    return HunterOfferCheck.problems(
        Map.of(Identifier.of("nightborn", "ware"), offer),
        Set.of(SLAYER),
        Set.of(HUNTER),
        master,
        stack -> stack.isOf(Items.PAPER));
  }

  @ParameterizedTest(name = "hunter {0} for {1}, master {2} for {3} -> undercuts {4}")
  @CsvSource({
    "3, 1, 2, 1, false",
    "2, 1, 2, 1, false",
    "1, 1, 2, 1, true",
    "4, 8, 3, 8, false",
    "3, 8, 3, 6, true",
    "1, 2, 2, 4, false"
  })
  @DisplayName("a hunter may match or pass the Guild Master's price per piece, never go under it")
  void undercuts(
      final int hunterEmeralds,
      final int hunterCount,
      final int masterEmeralds,
      final int masterCount,
      final boolean undercuts) {
    assertEquals(
        undercuts,
        HunterOfferCheck.undercuts(
            sale(hunterEmeralds, Items.ARROW, hunterCount, 0, Optional.empty()),
            sale(masterEmeralds, Items.ARROW, masterCount, 0, Optional.empty())));
  }

  @ParameterizedTest(name = "hunter bottles {0}, master bottles {1} -> undercuts {2}")
  @CsvSource({"1, 1, false", "0, 1, true", "2, 1, false", "1, 0, false"})
  @DisplayName("leaving out something the Guild Master also asks for is selling cheaper")
  void undercutsOnEveryWant(final int hunterBottles, final int masterBottles, final boolean under) {
    assertEquals(
        under,
        HunterOfferCheck.undercuts(
            sale(5, Items.POTION, 1, hunterBottles, Optional.empty()),
            sale(4, Items.POTION, 1, masterBottles, Optional.empty())));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "fine, slayer, '', false, ARROW, 0, ''",
    "unknown path, lost, '', false, ARROW, 0, 'nightborn:ware names unknown path nightborn:lost'",
    "unknown rank, slayer, nightborn:lost, false, ARROW, 0, 'nightborn:ware names unknown hunter rank nightborn:lost'",
    "guild rank, slayer, '', true, ARROW, 0, 'nightborn:ware asks a guild rank, which only the Guild Master may ask'",
    "contract, slayer, '', false, PAPER, 0, 'nightborn:ware sells a contract, which only the Guild Master gives'",
    "undercut, slayer, '', false, IRON_SWORD, 1, 'nightborn:ware undercuts the Guild Master on the same goods'",
    "rank held, slayer, '', false, IRON_AXE, 1, 'nightborn:ware sells goods the Guild Master keeps for rank'"
  })
  @DisplayName("each broken rule for a hunter offer is reported by name")
  void problems(
      final String name,
      final String path,
      final String hunterRank,
      final boolean guildRank,
      final String gives,
      final int emeralds,
      final String expected) {
    final Item item = Registries.ITEM.get(Identifier.ofVanilla(gives.toLowerCase(Locale.ROOT)));
    final HunterOffer offer =
        new HunterOffer(
            Identifier.of("nightborn", path),
            sale(
                emeralds == 0 ? 3 : emeralds,
                item,
                1,
                0,
                guildRank ? Optional.of(HUNTER) : Optional.empty()),
            hunterRank.isEmpty() ? Optional.empty() : Optional.of(Identifier.of(hunterRank)),
            0);
    final List<GuildOffer> master =
        List.of(
            sale(4, Items.IRON_SWORD, 1, 0, Optional.empty()),
            sale(1, Items.IRON_AXE, 1, 0, Optional.of(HUNTER)));
    assertEquals(expected, String.join("; ", check(offer, master)));
  }

  @Test
  @DisplayName("the shipped hunter offers break none of the rules")
  void shippedOffersPass() throws IOException {
    final Set<Identifier> paths =
        GuildDataFiles.load("skill_branch", SkillBranch.CODEC).entrySet().stream()
            .filter(e -> e.getValue().tree() == SkillTree.HUNTER && e.getValue().line())
            .map(e -> Identifier.of("nightborn", e.getKey()))
            .collect(Collectors.toSet());
    final Set<Identifier> ranks =
        GuildDataFiles.load("guild_rank", GuildRank.CODEC).keySet().stream()
            .map(rank -> Identifier.of("nightborn", rank))
            .collect(Collectors.toSet());
    assertEquals(
        List.of(),
        HunterOfferCheck.problems(
            GuildDataFiles.load("hunter_offer", HunterOffer.CODEC).entrySet().stream()
                .collect(
                    Collectors.toMap(
                        e -> Identifier.of("nightborn", e.getKey()), Map.Entry::getValue)),
            paths,
            ranks,
            List.copyOf(GuildDataFiles.load("guild_offer", GuildOffer.CODEC).values()),
            stack -> stack.contains(ContractItems.PROGRESS)));
  }
}
