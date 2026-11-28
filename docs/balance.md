# NPC Fight Balance

The balance scenario fights vampire mobs against hunter mobs and reports who wins. Every balance pass reruns the same scenario, so results can be compared. It lives in its own source set, `src/balance`, and is never shipped in the mod jar.

## Running It

```bash
./gradlew runBalance
```

This starts a dedicated server on a fresh flat world in `build/balance`, fights every matchup at once in sprint mode, writes the results and stops. A full run of 20 rounds a matchup takes about half a minute.

- `-PbalanceRounds=200` sets the rounds per matchup. The default is 20.
- `-PbalanceMatchups=adult-hunter-3,mixed-mixed-5` runs only those matchups.

It writes two files to `build/balance`:

- `balance-report.md`: one row per matchup, then the win share of every kit, path, bloodline, temperament and power.
- `balance-results.jsonl`: one line per round with the winner, the length, the hits and damage on each side, and the tags of everyone who fought.

## The Arenas

- One walled and roofed stone arena per matchup, 21 by 21 blocks, all running at once, at midnight with mob spawning off.
- Each team spawns at one end with the entity type, stage or rank and data a natural spawn gives it. Kits, paths, bloodlines, temperaments and powers are rolled as in the world. A vampire team shares a home, as a household does, so housemates join each other's fights.
- Nothing scripts the targeting. Hunters attack on sight, and vampires fight as they would.
- A round ends when one side has no one standing. A downed vampire is not standing. After 3 minutes the round is a draw. The arena is cleared and rebuilt and the next round starts.
- A hit is melee when it is a direct mob attack or a bite. Everything else, such as powers, holy water and bolts, counts as other.

## Matchups

| Vampires | Hunters | Team sizes |
|---|---|---|
| Fledgling | Initiate | 1 v 1, 3 v 3, 5 v 5 |
| Adult | Hunter | 1 v 1, 3 v 3, 5 v 5 |
| Elder | Veteran rank | 1 v 1, 3 v 3, 5 v 5 |
| Ancient | Master | 1 v 1, 3 v 3, 5 v 5 |
| Vampire lord | Veteran hunter | 1 v 1 |
| Vampire lord | Guild Master | 1 v 1 |
| Mixed stages | Mixed ranks, in natural spawn proportions | 5 v 5 |

## Pass Rule

A matchup passes when all of these hold:

- Each side wins between 45% and 55% of the decided rounds.
- Fewer than 10% of rounds are draws.
- Both sides land melee hits, and melee is at least 30% of each side's damage.
- The average round lasts at least 20 seconds for 1 v 1, 30 for 3 v 3 and 40 for 5 v 5.

A kit, path, bloodline, temperament or power passes when it wins no more than 60% of the decided rounds it appears in. It needs at least 10 rounds to be judged.

At 20 rounds a perfectly even matchup lands inside 45% to 55% only about half the time, so judge a balance pass on a few hundred rounds a matchup.

## Where The Balance Lives

A stage gives a vampire player and a vampire mob the same body, and a rank gives a hunter player and a hunter mob the same gifts. Each lives in one place, so a change there changes both. Tune in data, then rerun the scenario.

| Lever | File | What it moves |
|---|---|---|
| A stage's health, damage and speed | `vampire_stage/<stage>.json`, `body` | That stage for players and mobs alike, against every rank, at every team size |
| A rank's health and damage against vampires | `guild_rank/<rank>.json`, `gifts` | That rank for players and mobs alike, against every stage, at every team size |
| A mob's own base health and arm | `VampireMobAttributes`, `HunterEntity` and `GuildMasterEntity` attributes, and `health_scale` in `veteran/veteran.json` | Every stage or rank of that mob at once, and how long fights last. Players are not touched |
| A kit's damage, and its step for each rank | `hunter_kit/<kit>.json`, `damage` and `rank_damage` | One kit against the others. A crossbow needs the step, since a bolt does not grow with its hunter's skills as a stake does |
| A stage's retreat point | `vampire_stage/<stage>.json`, `combat.retreat_health` | Team fights against duels. A vampire that falls back heals while its housemates fight on, so a higher point helps teams and hurts a lone vampire. It also makes fights longer |
| How often a rank thinks | `hunter_build/hunter.json`, `think_ticks` | How many powers a rank uses. It matters most in 3 v 3 |
| When a mob uses a power | The `mob.uses` of a skill in `skill/` | Powers that hit a crowd, such as a flash trap inside a Killing Ground, matter far more in 5 v 5 than in a duel |
| How ranks and stages are mixed | `rank_weights` in `hunter_build/hunter.json`, `weight` in each stage | The mixed matchup |

A small edge in a duel grows in a team fight, because the side that drops one fighter first then outnumbers the other. Level each kit in the duels before tuning the team sizes.
