# ADR 0018: Mobs choose powers through one brain that scores uses from data

## Context

Vampire mobs and hunter mobs use the same powers as players (HOME-16, GUILD-17). Until now each chose with the same simple rule: cast the first owned power that is ready, in range of its target and affordable. Vampires wired that rule in one class and hunters in another, and the mob facts on a power were only its range and its tell. Later work asks mobs to pick the right power for the moment, to help allies, and to use powers outside a fight. If each of those grew its own rule, as holy water, bat form and dodging already have, the rules would drift apart and every new skill would need code.

## Decision

**One owner.** `MobFighting` owns power use for vampire mobs, hunter mobs and the Guild Master. It runs the power think through `PowerCasting` and then the mob's own moves, which are vampire leaps, dashes and dodges or the hunter's kit. The Eldest and the Grand Inquisitor keep their own fights and do not use it.

**A think is a picture and a score.** Each think builds one `Situation`: the mob's own health, blood, form, effects, sun and downed state, its target's distance, sight and health, and its intent. Goals that already move the mob report the intent through `IntentGoal`: retreat, close in, travel and rest today. A mob with a target and no other report is fighting.

**Uses are data.** A power's `mob` block lists its uses. A use names the intent it serves, what it aims at, a weight, and considerations. A consideration is one named fact read through a simple curve, `between`, `rising` or `falling`, to a value from 0 to 1. The facts are a short fixed list in `Fact`, and adding one is one line there and one test case. A power with no uses gets two from its range, one for a fight and one for closing in, so every shipped power is chosen exactly as before.

**The scorer is one pure function.** `PowerChoice` scores each ready power's best use as its weight times each consideration. Any zero rules it out. Powers are ranked by score, with ties kept in the order they were learned, and the first one the mob can afford and that beats the intent's threshold is cast. `Powers.check` stays the gate, so a mob obeys every rule a player does.

**Each intent has a budget.** The `nightborn:mob_intent` registry gives each intent a blood reserve, a cooldown price and a score threshold. A cast must leave the higher of the mob's own floor and the reserve, and the price is added to the pause before the next think. The fight costs nothing extra, so fights are unchanged.

**Mistakes are reported at load.** When the server starts it reports any use with an unknown intent, aim or fact, any intent with no budget, and any power that mobs will never use. A power that mobs should never use says so with `mobs_never_use`.

**Thinking stays cheap.** One situation per think, built only when the mob owns a power it could cast, and nothing is allocated otherwise. Each mob's first think is offset by its entity id, so a group spawned together does not think on the same tick.

## Consequences

- A new skill that mobs should use needs only its own JSON file. A data pack can add or retune uses and budgets the same way.
- `/nightborn brain <targets>` shows a mob's last think: its intent and its top three powers with their scores. This is how uses are tuned.
- On the tick a hunter starts a tell, its kit no longer steps that tick. Vampires already worked this way, and one owner means one order.
- Hunting and guarding are intents with budgets, but no goal reports them yet. They arrive with the work that gives mobs powers outside a fight.
