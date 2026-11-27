# ADR 0018: Mobs choose powers through one brain that scores uses from data

## Context

Vampire mobs and hunter mobs use the same powers as players (HOME-16, GUILD-17). Until now each chose with the same simple rule: cast the first owned power that is ready, in range of its target and affordable. Vampires wired that rule in one class and hunters in another, and the mob facts on a power were only its range and its tell. Later work asks mobs to pick the right power for the moment, to help allies, and to use powers outside a fight. If each of those grew its own rule, as holy water, bat form and dodging already have, the rules would drift apart and every new skill would need code.

## Decision

**One owner.** `MobFighting` owns power use for vampire mobs, hunter mobs and the Guild Master. It runs the power think through `PowerCasting` alongside the mob's own moves, which are vampire leaps, dashes and dodges or the hunter's kit. The Eldest and the Grand Inquisitor keep their own fights and do not use it.

**A think is a picture and a score.** Each think builds one `Situation`: the mob's own health, blood, form, sun and downed state, its target's distance, sight and health and its signs (winding up, running, slowed or held, in the air, vampire or hunter), how far it is to the end of the route the mob is walking, and its intent. Goals that already move the mob report the intent through `IntentGoal`: retreat and seeking shade, close in, travel and rest today. A mob with a target and no other report is fighting. A vampire thinks while it fights a target, and also while it falls back or seeks shade, with or without a target.

**Uses are data.** A power's `mob` block lists its uses. A use names the intent it serves, what it aims at, a weight, and considerations. A consideration is one named fact read through a simple curve, `between`, `rising` or `falling`, to a value from 0 to 1. The facts are a short fixed list in `Fact`, and adding one is one line there and one test case. A power with no uses gets two from its range, one for a fight and one for closing in, so every shipped power is chosen exactly as before.

**The scorer is one pure function.** `PowerChoice` scores each ready power's best use as its weight times each consideration, then cuts it by the intent's thrift. Any zero rules it out, and a power whose buff or effects are already on the mob is not weighed. Powers are ranked by score, with ties kept in the order they were learned. Of the ones the mob can afford that beat the intent's threshold, it casts the best, or by a roll made once per think any whose score is within the intent's spread of the best. `Powers.check` stays the gate, so a mob obeys every rule a player does.

**A cast has an aim.** The chosen use's aim goes through the tell to the cast as a `CastAim`: the mob's target, the mob itself, or a spot at the end of the route its navigator already holds. Every power that aims reads its subject or spot from it, so a Vanish, Shadow Step, Long Leap or trap goes where the brain chose rather than always at the target. No new path search is made for it. A power marked as a dodge is weighed only by the dodge in a fight, and with every other power out of one, so Vanish and Mist Form can also carry a vampire away.

**Each intent has a budget.** The `nightborn:mob_intent` registry gives each intent a blood reserve, a cooldown price, a score threshold, a thrift and a spread. A cast must leave the higher of the mob's own floor and the reserve, and the price is added to the pause before the next think. Thrift cuts a costly power's score in proportion to the mob's own health, so a healthy mob saves its blood and long cooldowns and a losing one spends them. Spread lets chance settle close scores, so fights do not all look the same. Fighting and closing in have both. Retreat has neither, so a mob getting away spends what it must.

**Mistakes are reported at load.** When the server starts it reports any use with an unknown intent, aim or fact, any use aimed at a spot that does not weigh `spot_distance`, any intent with no budget, and any power that mobs will never use. A power that mobs should never use says so with `mobs_never_use`.

**Thinking stays cheap.** A mob builds a situation only when it owns a power that is ready to cast, and nothing is built otherwise. The situation reads the costly facts, line of sight and the sun, only when a ready power's uses ask for them. Thinks are not offset per mob: each mob starts thinking when it takes its target, which already spreads a group out, and an offset would hold back a fresh mob's first power.

## Consequences

- A new skill that mobs should use needs only its own JSON file. A data pack can add or retune uses and budgets the same way.
- `/nightborn brain <targets>` shows a mob's last think: its intent and its top three powers with their scores. This is how uses are tuned.
- Hunters step their kit before they think about a power, and vampires move after it, as each did before. The owner keeps both orders.
- A vampire in bat form does not think about powers, so the `close_in` that bat flight reports is not seen yet. The default `close_in` use waits for the work that lets a flying vampire choose a power.
- Hunting and guarding are intents with budgets, but no goal reports them yet. They arrive with the work that gives mobs powers outside a fight.
- Baseline server tick time, for later brain work to compare with. Measured in a single player dev client at night on a flat stone arena, with `/tick query` averaging 100 ticks, three samples each, before and after this change:

| Scene | Before (ms per tick) | After (ms per tick) |
|---|---|---|
| Ten vampires against ten hunters, from the moment they meet | 5.7, 4.9, 4.7 | 5.6, 5.1, 6.8 |
| Forty idle vampires | 4.3, 5.8, 4.8 | 4.6, 4.2, 4.1 |

The samples swing by more than the gap between the two columns, so both scenes cost the same within what this measures.
