package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.challenge.ChallengeAttachment;
import com.grahambartley.nightborn.challenge.ChallengeRecord;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.challenge.OfferedTrial;
import com.grahambartley.nightborn.challenge.ServerFirst;
import com.grahambartley.nightborn.challenge.ServerFirsts;
import com.grahambartley.nightborn.challenge.Side;
import com.grahambartley.nightborn.challenge.TrialOffer;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;

public class ChallengeGameTest implements FabricGameTest {

  static final Identifier LAST_DROP = Identifier.of("nightborn", "the_last_drop");

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void aVampireFinishesATrialOnOfferAndIsPaidOnce(final TestContext context) {
    final ServerPlayerEntity vampire = context.createMockCreativeServerPlayerInWorld();
    VampireTransitions.turn(vampire);
    ChallengeAttachment.set(
        vampire,
        ChallengeRecord.EMPTY.withOffer(
            new TrialOffer(
                Optional.of(Side.VAMPIRE),
                Long.MAX_VALUE,
                List.of(OfferedTrial.fresh(LAST_DROP, true)))));
    final float before = SkillAttachment.get(vampire, SkillTree.VAMPIRE).experience();
    for (int kill = 0; kill < 7; kill++) {
      Deeds.record(vampire, Deed.of(DeedKinds.FINISHER_KILL));
    }
    final ChallengeRecord after = ChallengeAttachment.get(vampire);
    context.assertTrue(
        after.offer().find(LAST_DROP).map(OfferedTrial::done).orElse(false), "trial done");
    context.assertTrue(after.everFinished(LAST_DROP), "trial remembered as finished");
    context.assertTrue(
        SkillAttachment.get(vampire, SkillTree.VAMPIRE).experience() > before
            || SkillAttachment.get(vampire, SkillTree.VAMPIRE).earned() > 0,
        "the trial paid vampire experience");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void aServerFirstIsKeptByWhoeverClaimedItFirst(final TestContext context) {
    final ServerPlayerEntity first = context.createMockCreativeServerPlayerInWorld();
    final ServerPlayerEntity second = context.createMockCreativeServerPlayerInWorld();
    final Deed master = Deed.of(DeedKinds.RANK_REACHED).with(DeedKinds.TOP, true);
    Deeds.record(first, master);
    final ServerFirsts firsts = ServerFirsts.of(context.getWorld().getServer());
    final Optional<UUID> holder =
        firsts.book().holder(ServerFirst.MASTER).map(held -> held.player());
    context.assertTrue(holder.isPresent(), "the first to Master is recorded");
    Deeds.record(second, master);
    context.assertEquals(
        firsts.book().holder(ServerFirst.MASTER).map(held -> held.player()),
        holder,
        "a later Master never takes the record");
    context.complete();
  }
}
