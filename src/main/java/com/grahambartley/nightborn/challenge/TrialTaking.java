package com.grahambartley.nightborn.challenge;

import java.util.Optional;
import net.minecraft.util.Identifier;

public final class TrialTaking {

  public record Taken(ChallengeRecord record, Identifier trial) {}

  private TrialTaking() {}

  public static Optional<Taken> take(
      final ChallengeRecord record, final Optional<Side> side, final int index) {
    final TrialOffer offer = record.offer();
    if (side.filter(Side.HUNTER::equals).isEmpty()
        || !offer.side().equals(side)
        || index < 0
        || index >= offer.trials().size()) {
      return Optional.empty();
    }
    final OfferedTrial chosen = offer.trials().get(index);
    if (chosen.taken()) {
      return Optional.empty();
    }
    return Optional.of(
        new Taken(
            record.withOffer(offer.updating(chosen.trial(), OfferedTrial::taking)),
            chosen.trial()));
  }
}
