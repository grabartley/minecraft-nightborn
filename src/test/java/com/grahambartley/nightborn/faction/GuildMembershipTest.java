package com.grahambartley.nightborn.faction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.testing.FakeAttachmentTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuildMembershipTest {

  private static FakeAttachmentTarget player(
      final boolean vampire, final boolean oldPoints, final Oath oath) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    FactionAttachment.set(player, vampire ? Faction.VAMPIRE : Faction.HUMAN);
    if (oldPoints) {
      SkillAttachment.set(player, SkillTree.HUNTER, TreeProgress.EMPTY.withEarned(1));
    }
    OathAttachment.set(player, oath);
    return player;
  }

  @ParameterizedTest(name = "vampire {0}, old points {1}, proven {2} -> told {3}, proven after {4}")
  @CsvSource({
    "false, false, false, true, true",
    "false, false, true, false, true",
    "false, true, false, false, true",
    "true, false, false, false, false"
  })
  @DisplayName("staking a vampire proves a human once, and tells them only if they can now swear")
  void prove(
      final boolean vampire,
      final boolean oldPoints,
      final boolean proven,
      final boolean told,
      final boolean provenAfter) {
    final FakeAttachmentTarget player = player(vampire, oldPoints, new Oath(proven, false));
    assertEquals(told, GuildMembership.prove(player));
    assertEquals(provenAfter, OathAttachment.get(player).proven());
  }

  @ParameterizedTest(name = "vampire {0}, old points {1}, proven {2} -> sworn {3}")
  @CsvSource({
    "false, false, false, true",
    "false, false, true, true",
    "false, true, false, false",
    "true, false, true, false"
  })
  @DisplayName("an operator or a Guild Master swears in any human who is not yet a hunter")
  void swear(
      final boolean vampire, final boolean oldPoints, final boolean proven, final boolean sworn) {
    final FakeAttachmentTarget player = player(vampire, oldPoints, new Oath(proven, false));
    assertEquals(sworn, GuildMembership.swear(player));
    assertEquals(!vampire && (sworn || oldPoints), GuildMembership.isHunter(player));
  }

  @ParameterizedTest(name = "vampire {0}, proven {1}, sworn {2} -> may swear {3}, hunter {4}")
  @CsvSource({
    "false, false, false, false, false",
    "false, true, false, true, false",
    "false, true, true, false, true",
    "true, true, true, false, false"
  })
  @DisplayName("a turned hunter loses the oath while a vampire and gets it back if made human")
  void standing(
      final boolean vampire,
      final boolean proven,
      final boolean sworn,
      final boolean may,
      final boolean hunter) {
    final FakeAttachmentTarget player = player(vampire, false, new Oath(proven, sworn));
    assertEquals(may, GuildMembership.maySwear(player));
    assertEquals(hunter, GuildMembership.isHunter(player));
  }
}
