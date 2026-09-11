# Deployment and release

Local development uses Java 25, Maven Wrapper, Node/npm and synthetic configuration. Production is conditional EU-hosted stateless API/worker with private PostgreSQL/object storage and managed secrets; it is not provisioned here. On Windows run the Bash documentation verifier through WSL/Git Bash.

Commands are `npm run verify`, `test:coverage`, `test:e2e`, `build` and `release:check`, mirrored in VS Code/CI. No tag, push or deployment is authorised.
