# Deployment and release

Document environments, prerequisites, configuration/secrets ownership, deployment invocation, health checks, rollback, monitoring and release artefacts.

## Release contract

1. Update version and changelog.
2. Commit the release candidate.
3. Run `release:check` on that exact commit.
4. Record command, date, commit, runtime/tool versions, lockfile state, result and artefact locations.
5. Tag and push only when the gate passes.

CI must call the same project commands that developers use locally and in VS Code tasks.
