package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.rank.RankLadder;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradedItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HunterOfferTest {

  private static final RankLadder LADDER =
      new RankLadder(
          Stream.of("initiate", "hunter", "veteran", "master")
              .map(path -> new RankLadder.Rung(Identifier.of("nightborn", path), null))
              .toList());

  static Stream<String> shippedPaths() {
    return Stream.of("slayer", "faithful", "trapper", "seeker");
  }

  private static HunterOffer offer(
      final String path, final int order, final String rank, final int pathSkills) {
    return new HunterOffer(
        Identifier.of("nightborn", path),
        new GuildOffer(
            order,
            new TradedItem(Items.EMERALD),
            Optional.empty(),
            new ItemStack(Items.PAPER),
            1,
            Optional.empty()),
        Optional.ofNullable(rank.isEmpty() ? null : Identifier.of("nightborn", rank)),
        pathSkills);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("shippedPaths")
  @DisplayName("every path sells two or three wares to any hunter, and at most one more it earns")
  void eachShippedPathSellsAShortList(final String path) throws IOException {
    final List<HunterOffer> sold =
        GuildDataFiles.load("hunter_offer", HunterOffer.CODEC).values().stream()
            .filter(offer -> offer.path().equals(Identifier.of("nightborn", path)))
            .toList();
    final long plain =
        sold.stream()
            .filter(offer -> offer.hunterRank().isEmpty() && offer.pathSkills() == 0)
            .count();
    assertTrue(plain >= 2 && plain <= 3, path + " sells " + plain + " plain wares");
    assertTrue(sold.size() - plain <= 1, path + " has more than one earned ware");
  }

  @ParameterizedTest(name = "paths {1} -> {2}")
  @CsvSource({
    "'slayer:20, faithful:10, slayer:10, seeker:5', 'slayer, faithful', 'slayer:10, slayer:20, faithful:10'",
    "'slayer:20, faithful:10, slayer:10, seeker:5', 'faithful, slayer', 'faithful:10, slayer:10, slayer:20'",
    "'trapper:1, seeker:2', 'slayer, faithful', ''",
    "'seeker:3, seeker:1, trapper:2', 'seeker', 'seeker:1, seeker:3'"
  })
  @DisplayName("a hunter sells only from its own paths, path by path, in each offer's order")
  void forPathsKeepsToTheHuntersPaths(
      final String offers, final String paths, final String expected) {
    final Map<Identifier, HunterOffer> byId = new HashMap<>();
    for (final String entry : offers.split(", ")) {
      final String[] parts = entry.split(":");
      byId.put(
          Identifier.of("nightborn", entry.replace(':', '_')),
          offer(parts[0], Integer.parseInt(parts[1]), "", 0));
    }
    final List<Identifier> picked =
        Arrays.stream(paths.split(", ")).map(path -> Identifier.of("nightborn", path)).toList();
    assertEquals(
        expected,
        HunterOffer.forPaths(byId, picked, 0, path -> 0, LADDER).stream()
            .map(offer -> offer.path().getPath() + ":" + offer.offer().order())
            .collect(Collectors.joining(", ")));
  }

  @ParameterizedTest(name = "needs rank {0} and {1} skills; seller rank {2} with {3} -> {4}")
  @CsvSource({
    "'', 0, 0, 0, true",
    "hunter, 0, 0, 0, false",
    "hunter, 0, 1, 0, true",
    "veteran, 0, 3, 0, true",
    "lost, 0, 3, 64, false",
    "'', 3, 3, 2, false",
    "'', 3, 0, 3, true",
    "hunter, 3, 1, 2, false",
    "hunter, 3, 1, 4, true"
  })
  @DisplayName("an earned ware opens once the seller's rank and skills in its path reach it")
  void opens(
      final String rank,
      final int pathSkills,
      final int sellerRank,
      final int skillsInPath,
      final boolean opens) {
    assertEquals(
        opens, offer("slayer", 0, rank, pathSkills).opens(sellerRank, skillsInPath, LADDER));
  }

  @ParameterizedTest(name = "rank {0}, skills {1}")
  @CsvSource({"0, 0", "1, 0", "1, 5", "3, 10"})
  @DisplayName("an earned ware is counted against the skills in its own path, not another's")
  void forPathsCountsSkillsPerPath(final int sellerRank, final int slayerSkills) {
    final Map<Identifier, HunterOffer> byId =
        Map.of(
            Identifier.of("nightborn", "slayer_deep"), offer("slayer", 1, "", 3),
            Identifier.of("nightborn", "faithful_deep"), offer("faithful", 1, "", 3));
    final List<HunterOffer> sold =
        HunterOffer.forPaths(
            byId,
            List.of(Identifier.of("nightborn", "slayer"), Identifier.of("nightborn", "faithful")),
            sellerRank,
            path -> path.getPath().equals("slayer") ? slayerSkills : 0,
            LADDER);
    assertEquals(slayerSkills >= 3 ? 1 : 0, sold.size());
  }
}
