# ADR 0023: Built-in mob actions are scored beside powers from data

## Context

The mob brain (ADR 0018) chose a mob's powers, but five things a mob does in a fight were still decided by rules of their own beside it. A hunter threw holy water or raised its crucifix by a fixed check every tick. A vampire rolled for a leap, a dash or a dodge in a fixed order at each think, and on a new threat rolled again and cast a dodge power through its own path. Each could happen in the same moment as a power, and none could be tuned or seen with `/nightborn brain`.

## Decision

**An action is an option like a power.** Each built-in action has a file in `data/nightborn/nightborn/mob_action/` with a list of uses, written exactly as a power's `mob` uses are, and an optional `dodge` flag. The brain puts the mob's actions into the same list of options as its powers and scores them with the same scorer. An option says whether it is a power or an action. An action costs no blood, so it is always affordable. One think chooses one option, so a mob never uses an action and a power in the same moment.

**The mob says what is to hand, the data says when it is wanted.** A mob's move set offers its actions with their readiness, the way `Powers.check` gives a power's: a vampire's move while it stands, has rested since its last one and passes its stage's chance for that move; holy water while the hunter faces a foe with a flask ready; the crucifix while a foe is within the crucifix's reach. The stage chances, the throw wait and the move rest are kept as they were. The leap and dash ranges, the holy water range and the need for sight moved into the action files. A new fact, `target_threatening`, carries the old threat test so the dodge can weigh it. The four leap and dash range settings were removed, since a data pack now tunes those ranges.

**Weights keep the old order.** A dodge and holy water weigh 1.5, above a plain power. A leap and a dash weigh 0.5 and the crucifix 0.4, so a power that suits the moment comes first and they fill the thinks the mob has no power for. A raised crucifix counts as running, so it is not weighed again and stays up until it can no longer be held or the hunter casts a power.

**A reaction is a think.** On a new threat a vampire still rolls its stage's dodge chance and, if it passes, thinks at once with only the options kept for dodging, then casts the one chosen without a tell, as before. It is the same think with the same scorer, not a separate path.

**The owner runs the moves, then the think.** `MobFighting` ticks the mob's move set, lets it react, and then thinks. The move set carries out a chosen action. It is told when a power is told so a hunter lowers its crucifix.

## Consequences

- A data pack can retune or switch off any built-in action by its file, and a bad use is reported at start with the action's name.
- `/nightborn brain` shows actions beside powers, under ids such as `nightborn:leap`.
- A hunter throws holy water and checks its crucifix at its own think pace rather than every tick.
- A vampire that owns a ready power in range leaps less often than before, since it now casts the power instead of doing both.
- The Eldest and the Grand Inquisitor do not use this brain and are unchanged.
