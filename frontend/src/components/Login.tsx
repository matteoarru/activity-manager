import { FormEvent, useState } from "react";
import { request } from "../api/client";
import { loginProfiles, Profile } from "../domain/activity";

type LoginProps = {
  onLogin: (profile: Profile) => void;
};

export function Login({ onLogin }: LoginProps) {
  const [username, setUsername] = useState<string>(loginProfiles[0][1]);
  const [password, setPassword] = useState("demo-password");
  const [error, setError] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      onLogin(
        await request<Profile>("/auth/login", {
          method: "POST",
          body: JSON.stringify({ username, password }),
        }),
      );
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to sign in");
    }
  }

  return (
    <>
      <a className="skip-link" href="#login-title">
        Skip to sign in
      </a>
      <main className="login">
        <section aria-labelledby="login-title">
          <p className="eyebrow">Activity Management Suite</p>
          <h1 id="login-title">Event Operations</h1>
          <p>Sign in with a test profile to view the operational workspace.</p>
          <form onSubmit={submit}>
            <label htmlFor="profile">Test profile</label>
            <select
              id="profile"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
            >
              {loginProfiles.map(([label, name]) => (
                <option key={name} value={name}>
                  {label} · {name}
                </option>
              ))}
            </select>
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
            />
            <button type="submit">Sign in</button>
            {error && (
              <p role="alert" className="error">
                {error}
              </p>
            )}
          </form>
          <p className="hint">
            All local profiles use <code>demo-password</code>; these accounts
            must not be used in production.
          </p>
        </section>
      </main>
    </>
  );
}
