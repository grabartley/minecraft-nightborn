package com.grahambartley.nightborn.challenge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.util.Identifier;

public final class ChallengeProgress {

  public record Context(
      Map<Identifier, Trial> trials,
      Map<Identifier, Feat> feats,
      Optional<Side> side,
      OptionalLong night) {

    public Context {
      trials = Map.copyOf(trials);
      feats = Map.copyOf(feats);
    }
  }

  public record TrialDone(Identifier trial, boolean firstTime) {}

  public record Outcome(ChallengeRecord record, List<TrialDone> trials, List<Identifier> feats) {

    public Outcome {
      trials = List.copyOf(trials);
      feats = List.copyOf(feats);
    }
  }

  private ChallengeProgress() {}

  public static Outcome apply(
      final ChallengeRecord before, final Deed deed, final Context context) {
    final List<TrialDone> trialsDone = new ArrayList<>();
    ChallengeRecord record = before;
    if (record.offer().side().isPresent() && record.offer().side().equals(context.side())) {
      for (final OfferedTrial offered : record.offer().trials()) {
        final Trial trial = context.trials().get(offered.trial());
        if (trial == null || !offered.counting()) {
          continue;
        }
        final Tally next = trial.goal().after(offered.tally(), deed, context.night());
        final boolean finished = trial.goal().done(next);
        record =
            record.withOffer(
                record.offer().updating(offered.trial(), o -> o.counted(next, finished)));
        if (finished) {
          trialsDone.add(new TrialDone(offered.trial(), !record.everFinished(offered.trial())));
          record = record.finishing(offered.trial());
        }
      }
    }
    final List<Identifier> featsEarned = new ArrayList<>();
    FeatLog feats = record.feats();
    final List<Map.Entry<Identifier, Feat>> ordered =
        context.feats().entrySet().stream().sorted(Map.Entry.comparingByKey(ORDER)).toList();
    for (final Map.Entry<Identifier, Feat> entry : ordered) {
      final Identifier id = entry.getKey();
      final Feat feat = entry.getValue();
      if (!feats.open(id) || !feat.side().holds(context.side())) {
        continue;
      }
      if (feat.spoiledBy().filter(spoiler -> spoiler.matches(deed)).isPresent()) {
        feats = feats.spoiling(id);
        continue;
      }
      final Tally counted = feats.tally(id);
      final Tally next = feat.goal().after(counted, deed, context.night());
      if (feat.goal().done(next)) {
        feats = feats.earning(id, context.side().filter(Side.VAMPIRE::equals).isPresent());
        featsEarned.add(id);
      } else if (!next.equals(counted)) {
        feats = feats.counted(id, next);
      }
    }
    return new Outcome(record.withFeats(feats), trialsDone, featsEarned);
  }

  private static final Comparator<Identifier> ORDER = Comparator.comparing(Identifier::toString);
}
