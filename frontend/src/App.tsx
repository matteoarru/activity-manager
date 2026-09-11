import { useEffect, useState } from "react";
import { request } from "./api/client";
import { Profile } from "./domain/activity";
import { Login } from "./components/Login";
import { Workspace } from "./components/Workspace";

export function App() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [checked, setChecked] = useState(false);

  useEffect(() => {
    request<Profile>("/me")
      .then(setProfile)
      .catch(() => undefined)
      .finally(() => setChecked(true));
  }, []);

  if (!checked) {
    return (
      <main>
        <p>Loading…</p>
      </main>
    );
  }

  return profile ? (
    <Workspace profile={profile} onLogout={() => setProfile(null)} />
  ) : (
    <Login onLogin={setProfile} />
  );
}
