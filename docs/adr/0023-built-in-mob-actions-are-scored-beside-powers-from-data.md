# ADR 0023: Built-in mob actions are scored beside powers from data

## Context

The mob brain (ADR 0018) chose a mob's powers, but five things a mob does in a fight were still decided by rules of their own beside it. A hunter threw holy water or raised its crucifix by a fixed check every tick. A vampire rolled for a leap, a dash or a dodge in a fixed order at each think, and on a new threat rolled again and cast a dodge power through its own path. Each could happen in the same moment as a power, and none could be tuned or seen with `/nightborn brain`.

## Decision

**An action is an option like a power.** Each built-in action has a file in `data/nightborn/nightborn/mob_action/` with a list of uses, written exactly as a power's `mob` uses are. The brain puts the mob's actions into the same list of options as its powers and scores them with the same scorer. An option says whether it is a power or an action. An action costs no blood, so it is always affordable. One think chooses one option, so a mob never uses an action and a power in the same moment.

**The mob says what is to hand, the data says when it is wanted.** A mob's move set offers its actions with their readiness, the way `Powers.check` gives a power's: a vampire's move while it stands, has rested since its last one and its one roll for the think falls under its stage's chance for that move, as the old single roll did; holy water while the hunter faces a foe with a flask ready; the crucifix while a foe is within the crucifix's reach. The stage chances, the throw wait and the move rest are kept as they were. The leap and dash ranges, the holy water range and the need for sight moved into the action files. A new fact, `target_threatening`, carries the old threat test so the dodge can weigh it. The dodge has a `retreat` use as well as a `fight` one, since a vampire fell back and dodged in the same moments before. The four leap and dash range settings were removed, since a data pack now tunes those ranges.

**Weights keep the old order.** A dodge and holy water weigh 1.5 and the crucifix 1.2, above a plain power, as they once went up before any power was thought of. A leap weighs 0.6 and a dash 0.5, so a power that suits the moment comes first, they fill the thinks the mob has no power for, and a leap wins where the two ranges meet. A raised crucifix is a held stance, not a cast: it counts as running, so it is not weighed again, and it stays up through the hunter's prayers until the foe leaves its sight or reach. Prayers such as Rebuke need it in hand, so a hunter raises it on one think and prays on the next.

**A reaction is a think.** On a new threat a vampire still rolls its stage's dodge chance and, if it passes, thinks at once with only the powers kept for dodging, then casts the one chosen without a tell, as before. It is the same think with the same scorer, not a separate path. Built-in actions are weighed only in ordinary thinks.

**The owner runs the moves, then the think.** `MobFighting` ticks the mob's move set, lets it react, and then thinks. The move set carries out a chosen action.

## Consequences

- A data pack can retune or switch off any built-in action by its file, and a bad use is reported at start with the action's name.
- `/nightborn brain` shows actions beside powers, under ids such as `nightborn:leap`.
- A hunter throws holy water and raises its crucifix at its own think pace rather than every tick, so a crucifix prayer comes one think after the crucifix goes up.
- A vampire that owns a ready power in range leaps less often than before, since it now casts the power instead of doing both.
- The Eldest and the Grand Inquisitor do not use this brain and are unchanged.
