# Security and data handling

Assets include participation data, financial evidence and supplier records. Browser/API, API/storage, adapter and worker edges are trust boundaries. Every API/list/download/export checks role plus activity, organisation, provider, person and valid delegation; hidden UI is not a control.

The synthetic profile uses an in-memory, BCrypt-hashed fixture directory and server-side session cookie. Login is JSON over the same origin, protected endpoints require authentication, and CSRF is enabled except for the credential-submission endpoint. Production needs approved OIDC, secure HttpOnly/SameSite sessions, CSRF, MFA for privileged roles, managed secrets, encryption, quarantine and expiring downloads. Demo data is fictitious and no production bypass exists. Audit excludes passports and sensitive free text.
