# ADR 0001: A linter forbids comments rather than a formatter stripping them

## Context

The standards in [`../standards.md`](../standards.md) say the source carries no comments. A rule that relies on memory does not hold, so the build has to enforce it.

## Decision

A Checkstyle `MatchXpath` rule over the parsed comment nodes fails the build at `file:line` on any comment in any Java source set. It reports and never edits source. `NoCommentsRuleTest` runs the rule against a fixture of every comment form and every comment-like string, so the gate cannot quietly stop working.

Build scripts and property files are outside the rule and may keep their comments.

## Consequences

- A comment fails the build, so it is removed by the person who wrote it, who can see whether the code still reads clearly without it.
- A formatter never removes text from a file, so nothing can be stripped by accident.
- The family's other mods run the same gate, so a class can move between them under one rule.
