package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillShopTest {

  @ParameterizedTest(name = "rank {0} of {1}: {2}")
  @CsvSource({
    "1, 1, nightborn.skills.bought, 1",
    "1, 3, nightborn.skills.bought.rank, 2",
    "3, 3, nightborn.skills.bought.rank, 2"
  })
  @DisplayName("buying a ranked skill names the rank, and a single-rank skill just its name")
  void bought(final int rank, final int maxRank, final String key, final int args) {
    final Text text =
        SkillShop.bought(Identifier.of("nightborn", "core/thick_blood"), rank, maxRank);
    final TranslatableTextContent content = (TranslatableTextContent) text.getContent();
    assertEquals(key, content.getKey());
    assertEquals(args, content.getArgs().length);
    if (args == 2) {
      assertEquals(rank, content.getArgs()[1]);
    }
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"VAMPIRE, true", "HUNTER, false"})
  @DisplayName("only a vampire's bloodlines count toward Old Blood, never a hunter's paths")
  void countsBloodlines(final SkillTree tree, final boolean counts) {
    assertEquals(counts, SkillShop.countsBloodlines(tree));
  }
}
