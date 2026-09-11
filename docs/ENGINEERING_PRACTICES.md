# Engineering practices

- Use formatter, lint, static analysis and tests in every change.
- Prefer current compatible stable dependencies; document a concrete compatibility or functional reason and review date for every deferred upgrade.
- Review requirements, architecture, data model, traceability, tests, deployment guide and changelog whenever a change affects them.
- Keep pull requests small, reviewable and evidence-backed.
- Treat warnings and failed quality gates as work to assess, not output to suppress.
- Use ADRs for decisions that affect more than a local implementation detail.
The visual rules are in [STYLE_GUIDE.md](STYLE_GUIDE.md), and the implementation conventions are in [CODING_STYLE.md](CODING_STYLE.md). Java-specific guidance and the XP baseline are also retained in [JAVA_CODING_AND_XP.md](JAVA_CODING_AND_XP.md).
