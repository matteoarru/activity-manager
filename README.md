# Project template

A language- and architecture-neutral baseline for projects developed in VS Code. It keeps requirements, architecture, tests, documentation, release evidence and versioning aligned.

## Start here

1. Rename this repository and replace placeholders in [PRODUCT_BRIEF.md](docs/PRODUCT_BRIEF.md).
2. Select a stack profile from [.vscode/profiles](.vscode/profiles), copy it to `.vscode/`, and replace its command placeholders with the project’s actual commands.
3. Record every accepted requirement in [REQUIREMENTS.md](docs/REQUIREMENTS.md), assign it a stable ID, and map its automated evidence in [TRACEABILITY.md](docs/TRACEABILITY.md).
4. Record significant technical choices as ADRs in [docs/decisions](docs/decisions).
5. Make `verify`, `test:coverage`, `test:e2e` and `release:check` real project commands before the first release.

## Non-negotiable workflow

- Requirements drive acceptance criteria and tests.
- Every implemented requirement is traceable to code and automated evidence.
- Documentation changes with behaviour, architecture, data, operational assumptions and deployment.
- CI runs the same commands exposed through VS Code tasks.
- A minor or major release is committed, fully verified on that commit, tagged, and only then pushed.
- Coverage is measure-specific. Pure business/domain rules target 100%; UI behaviour also requires focused component and end-to-end evidence.

Run `bash scripts/verify-documentation.sh` to check repository-documentation links. It has no language runtime dependency beyond a POSIX shell and standard Unix tools.

## Repository structure

- `AGENTS.md`: concise instructions for developers and coding agents.
- `docs/`: product, requirements, architecture, data, test, deployment, security and decision records.
- `.vscode/`: shared VS Code tasks, recommended extensions and stack profiles.
- `scripts/`: portable documentation checks and release-evidence helpers.
- `.github/workflows/`: CI contract; adapt command placeholders to the selected stack.
