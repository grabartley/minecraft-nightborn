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
      final boolean vampire, final boolean hunterPoints, final Oath oath) {
    final FakeAttachmentTarget player = new FakeAttachmentTarget();
    FactionAttachment.set(player, vampire ? Faction.VAMPIRE : Faction.HUMAN);
    if (hunterPoints) {
      SkillAttachment.set(player, SkillTree.HUNTER, TreeProgress.EMPTY.withEarned(1));
    }
    OathAttachment.set(player, oath);
    return player;
  }

  @ParameterizedTest(name = "vampire {0}, proven {1}, sworn {2} -> told {3}, proven after {4}")
  @CsvSource({
    "false, false, false, true, true",
    "false, true, false, false, true",
    "false, true, true, false, true",
    "true, false, false, false, false"
  })
  @DisplayName("staking a vampire proves a human once, and tells them only if they can now swear")
  void prove(
      final boolean vampire,
      final boolean proven,
      final boolean sworn,
      final boolean told,
      final boolean provenAfter) {
    final FakeAttachmentTarget player = player(vampire, false, new Oath(proven, sworn, 0, 0));
    assertEquals(told, GuildMembership.prove(player));
    assertEquals(provenAfter, OathAttachment.get(player).proven());
  }

  @ParameterizedTest(name = "vampire {0}, proven {1}, sworn {2} -> sworn now {3}")
  @CsvSource({
    "false, false, false, true",
    "false, true, false, true",
    "false, true, true, false",
    "true, true, false, false"
  })
  @DisplayName("an operator or a Guild Master swears in any human who is not yet a hunter")
  void swear(
      final boolean vampire, final boolean proven, final boolean sworn, final boolean swornNow) {
    final FakeAttachmentTarget player = player(vampire, false, new Oath(proven, sworn, 0, 0));
    assertEquals(swornNow, GuildMembership.swear(player));
    assertEquals(!vampire, GuildMembership.isHunter(player));
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
    final FakeAttachmentTarget player = player(vampire, false, new Oath(proven, sworn, 0, 0));
    assertEquals(may, GuildMembership.maySwear(player));
    assertEquals(hunter, GuildMembership.isHunter(player));
  }

  @ParameterizedTest(name = "proven {0} -> hunter {1}")
  @CsvSource({"false, false", "true, false"})
  @DisplayName("hunter points alone never make an unsworn human a hunter")
  void pointsAlone(final boolean proven, final boolean hunter) {
    final FakeAttachmentTarget player = player(false, true, new Oath(proven, false, 0, 0));
    assertEquals(hunter, GuildMembership.isHunter(player));
    assertEquals(proven, GuildMembership.maySwear(player));
  }
}
