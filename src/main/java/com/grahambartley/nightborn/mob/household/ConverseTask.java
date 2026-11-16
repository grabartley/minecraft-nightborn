package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.look.VampireVoice;
import java.util.List;

public final class ConverseTask implements HouseholdTask {

  static final double TALK_REACH = 2.5;
  public static final int LINE_TICKS = 50;
  static final int GIVE_UP_TICKS = 300;

  private final VampireEntity vampire;
  private final VampireEntity partner;
  private final List<Conversation.Line> lines;
  private final Walk walk;
  private final Countdown approach = new Countdown(GIVE_UP_TICKS);
  private int line;
  private int nextLine;
  private boolean together;

  public ConverseTask(
      final VampireEntity vampire,
      final VampireEntity partner,
      final List<Conversation.Line> lines) {
    this.vampire = vampire;
    this.partner = partner;
    this.lines = lines;
    this.walk = new Walk(vampire);
  }

  public static long length(final int lines) {
    return (long) GIVE_UP_TICKS + (long) lines * LINE_TICKS;
  }

  @Override
  public boolean tick() {
    if (!partner.isAlive() || partner.getTarget() != null || partner.isSleeping()) {
      return false;
    }
    vampire.getLookControl().lookAt(partner);
    partner.getLookControl().lookAt(vampire);
    if (!together) {
      if (!approach.running()) {
        return false;
      }
      together = walk.toward(partner.getPos(), TALK_REACH);
      return true;
    }
    if (--nextLine > 0) {
      return true;
    }
    if (line >= lines.size()) {
      return false;
    }
    final Conversation.Line spoken = lines.get(line++);
    final VampireEntity speaker = spoken.opener() ? vampire : partner;
    speaker.showAction(spoken.tone().clip());
    speaker.speak(
        spoken.tone() == Conversation.Tone.ARGUE ? VampireVoice.ARGUE : VampireVoice.TALK);
    nextLine = LINE_TICKS;
    return true;
  }

  @Override
  public void stop() {
    partner.householdLife().release();
    vampire.householdLife().stopTalking();
  }
}
