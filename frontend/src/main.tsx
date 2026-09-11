import { FormEvent, useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import { request } from "./api/client";
import {
  Activity,
  canSetUpActivities,
  Cnu,
  formatDate,
  loginProfiles,
  Profile,
} from "./domain/activity";
import "./style.css";

function Login({ onLogin }: { onLogin: (profile: Profile) => void }) {
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
          <p className="eyebrow">Synthetic local demonstration</p>
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

function SetupPanel({
  onCreated,
  onCancel,
}: {
  onCreated: (activity: Activity) => void;
  onCancel: () => void;
}) {
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setMessage("");
    const form = new FormData(event.currentTarget);
    const payload = {
      code: form.get("code"),
      title: form.get("title"),
      description: form.get("description"),
      countryCode: form.get("countryCode"),
      venue: form.get("venue"),
      timeZone: form.get("timeZone"),
      startsOn: form.get("startsOn"),
      endsOn: form.get("endsOn"),
      expectedParticipants: Number(form.get("expectedParticipants")),
      fundingRegime: form.get("fundingRegime"),
      invitationModality: "NOMINATION",
      cplReference: form.get("cplReference"),
      cplByCostType: {
        travel: form.get("travelCpl"),
        hotel: form.get("hotelCpl"),
        catering: form.get("cateringCpl"),
      },
    };
    try {
      const activity = await request<Activity>("/v1/activities", {
        method: "POST",
        body: JSON.stringify(payload),
      });
      const file = form.get("curriculum");
      if (file instanceof File && file.size > 0) {
        const upload = new FormData();
        upload.append("file", file);
        await request<Activity>(`/v1/activities/${activity.id}/curriculum`, {
          method: "POST",
          body: upload,
          headers: {},
        });
      }
      onCreated(activity);
      setMessage("Activity created. You can now invite CNUs to nominate.");
    } catch (reason) {
      setMessage(
        reason instanceof Error ? reason.message : "Activity setup failed",
      );
    } finally {
      setBusy(false);
    }
  }
  return (
    <section aria-labelledby="setup">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Activity setup</p>
          <h2 id="setup">Set up an activity</h2>
          <p>
            Create the course, define the CPL allocation and choose nomination
            invitations.
          </p>
        </div>
        <button className="secondary" type="button" onClick={onCancel}>
          Back to activities
        </button>
      </div>
      <form className="setup-form" onSubmit={submit}>
        <label>
          Course code
          <input required name="code" placeholder="2026-COURSE-01" />
        </label>
        <label>
          Title
          <input required name="title" />
        </label>
        <label>
          Description
          <textarea name="description" rows={3} />
        </label>
        <label>
          Funding regime
          <input required name="fundingRegime" defaultValue="STANDARD" />
        </label>
        <div className="form-grid">
          <label>
            Country code
            <input
              required
              name="countryCode"
              defaultValue="HU"
              maxLength={2}
            />
          </label>
          <label>
            Venue
            <input required name="venue" defaultValue="Budapest" />
          </label>
          <label>
            Time zone
            <input required name="timeZone" defaultValue="Europe/Budapest" />
          </label>
          <label>
            Expected participants
            <input
              required
              name="expectedParticipants"
              type="number"
              min="0"
              defaultValue="20"
            />
          </label>
          <label>
            Starts
            <input required name="startsOn" type="date" />
          </label>
          <label>
            Ends
            <input required name="endsOn" type="date" />
          </label>
        </div>
        <label>
          Default CPL reference (all orders)
          <input name="cplReference" placeholder="CPL-2026-001" />
        </label>
        <div className="form-grid">
          <label>
            Travel CPL
            <input name="travelCpl" />
          </label>
          <label>
            Hotel CPL
            <input name="hotelCpl" />
          </label>
          <label>
            Catering CPL
            <input name="cateringCpl" />
          </label>
        </div>
        <label>
          Curricula file
          <input name="curriculum" type="file" accept=".pdf,.doc,.docx" />
        </label>
        <button disabled={busy} type="submit">
          {busy ? "Creating…" : "Create activity"}
        </button>
        {message && (
          <p role="status" className="hint">
            {message}
          </p>
        )}
      </form>
    </section>
  );
}

function InvitationPanel({ activity }: { activity: Activity }) {
  const [cnus, setCnus] = useState<Cnu[]>([]);
  const [selected, setSelected] = useState<string[]>([]);
  const [message, setMessage] = useState("");
  useEffect(() => {
    request<Cnu[]>("/v1/cnus")
      .then(setCnus)
      .catch(() => undefined);
  }, []);
  async function invite() {
    try {
      const result = await request<{ invitedCount: number }>(
        `/v1/activities/${activity.id}/nomination-invitations`,
        { method: "POST", body: JSON.stringify({ cnuUsernames: selected }) },
      );
      setMessage(
        `${result.invitedCount} CNU invitation(s) recorded for nomination.`,
      );
    } catch (reason) {
      setMessage(
        reason instanceof Error ? reason.message : "Invitation failed",
      );
    }
  }
  return (
    <div className="invitation">
      <strong>Invite CNUs to nominate</strong>
      <div>
        {cnus.map((cnu) => (
          <label key={cnu.username}>
            <input
              type="checkbox"
              checked={selected.includes(cnu.username)}
              onChange={(event) =>
                setSelected((current) =>
                  event.target.checked
                    ? [...current, cnu.username]
                    : current.filter((name) => name !== cnu.username),
                )
              }
            />
            {cnu.username} · {cnu.organisation}
          </label>
        ))}
      </div>
      <button type="button" onClick={invite} disabled={selected.length === 0}>
        Send nomination invitation
      </button>
      {message && <small>{message}</small>}
    </div>
  );
}

function ActivityDetail({
  activity,
  canSetup,
  onBack,
}: {
  activity: Activity;
  canSetup: boolean;
  onBack: () => void;
}) {
  return (
    <section aria-labelledby="activity-detail-title" className="detail-page">
      <button className="secondary back-link" type="button" onClick={onBack}>
        Back to activities
      </button>
      <p className="eyebrow">Activity details</p>
      <div className="detail-heading">
        <div>
          <p className="record-code">{activity.courseReference}</p>
          <h2 id="activity-detail-title">{activity.title}</h2>
          <p className="status-badge">{activity.status}</p>
        </div>
        <div className="detail-dates">
          <span>Dates</span>
          <strong>
            {formatDate(activity.startsOn)} to {formatDate(activity.endsOn)}
          </strong>
        </div>
      </div>
      <p className="description">
        {activity.description || "Description not supplied."}
      </p>
      <dl className="detail-facts">
        <div>
          <dt>Venue</dt>
          <dd>{activity.venue}</dd>
        </div>
        <div>
          <dt>Expected participants</dt>
          <dd>{activity.expectedParticipants}</dd>
        </div>
        <div>
          <dt>Commitment</dt>
          <dd>{activity.cplReference ?? "Cost-type allocation"}</dd>
        </div>
        <div>
          <dt>Invitation modality</dt>
          <dd>Nomination</dd>
        </div>
      </dl>
      {canSetup && (
        <section
          aria-labelledby="invitations-title"
          className="connected-entity"
        >
          <p className="eyebrow">Connected entity</p>
          <h3 id="invitations-title">Nomination invitations</h3>
          <p>
            Select the CNUs that should receive an invitation for this activity.
          </p>
          <InvitationPanel activity={activity} />
        </section>
      )}
    </section>
  );
}

function Workspace({
  profile,
  onLogout,
}: {
  profile: Profile;
  onLogout: () => void;
}) {
  const [activities, setActivities] = useState<Activity[]>([]);
  const [showSetup, setShowSetup] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [selectedActivity, setSelectedActivity] = useState<Activity | null>(
    null,
  );
  const role =
    profile.roles
      .find((item) => item.startsWith("ROLE_"))
      ?.replace("ROLE_", "") ?? "USER";
  const canSetup = canSetUpActivities(profile);
  useEffect(() => {
    request<Activity[]>("/v1/activities")
      .then(setActivities)
      .catch(() => undefined);
  }, []);
  async function logout() {
    try {
      await request<{ status: string }>("/auth/logout", { method: "POST" });
    } finally {
      onLogout();
    }
  }
  return (
    <>
      <a
        className="skip-link"
        href={
          showSetup
            ? "#setup"
            : selectedActivity
              ? "#activity-detail-title"
              : "#activities"
        }
      >
        Skip to main content
      </a>
      <main id="main-content">
        <header>
          <div>
            <p className="eyebrow">Synthetic local demonstration</p>
            <h1>Event Operations</h1>
            <p>Signed-in workspace for {profile.username}</p>
          </div>
          <div className="identity">
            <div className="identity-summary">
              <div>
                <span>{profile.username}</span>
                <small>{role}</small>
              </div>
              <button
                className="user-avatar"
                type="button"
                aria-label={`Open account menu for ${profile.username}`}
                aria-expanded={showUserMenu}
                onClick={() => setShowUserMenu((current) => !current)}
              >
                {profile.username.split(".")[0].slice(0, 2).toUpperCase()}
              </button>
            </div>
            {showUserMenu && (
              <div className="user-menu">
                <button
                  className="utility-button"
                  type="button"
                  onClick={logout}
                >
                  Sign out
                </button>
              </div>
            )}
          </div>
        </header>
        {showSetup ? (
          <SetupPanel
            onCreated={(activity) => {
              setActivities((current) => [...current, activity]);
              setShowSetup(false);
            }}
            onCancel={() => setShowSetup(false)}
          />
        ) : selectedActivity ? (
          <ActivityDetail
            activity={selectedActivity}
            canSetup={canSetup}
            onBack={() => setSelectedActivity(null)}
          />
        ) : (
          <section aria-labelledby="activities" className="activity-home">
            <div className="page-heading">
              <div>
                <p className="eyebrow">Workspace</p>
                <h2 id="activities">Activities</h2>
                <p>Activities assigned to your role.</p>
              </div>
              {canSetup && (
                <button type="button" onClick={() => setShowSetup(true)}>
                  Add activity
                </button>
              )}
            </div>
            {activities.length === 0 ? (
              <p className="empty-state">
                No activities are assigned to this profile.
              </p>
            ) : (
              <ul className="activity-list" aria-label="Activities">
                {activities.map((activity) => (
                  <li key={activity.id}>
                    <button
                      className="activity-row"
                      type="button"
                      onClick={() => setSelectedActivity(activity)}
                    >
                      <span className="record-code">
                        {activity.courseReference}
                      </span>
                      <span className="activity-summary">
                        <strong>{activity.title}</strong>
                        <small>
                          {activity.venue} · {formatDate(activity.startsOn)} to{" "}
                          {formatDate(activity.endsOn)}
                        </small>
                      </span>
                      <span className="status-badge">{activity.status}</span>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </section>
        )}
      </main>
    </>
  );
}
function App() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [checked, setChecked] = useState(false);
  useEffect(() => {
    request<Profile>("/me")
      .then(setProfile)
      .catch(() => undefined)
      .finally(() => setChecked(true));
  }, []);
  if (!checked)
    return (
      <main>
        <p>Loading…</p>
      </main>
    );
  return profile ? (
    <Workspace profile={profile} onLogout={() => setProfile(null)} />
  ) : (
    <Login onLogin={setProfile} />
  );
}
createRoot(document.getElementById("root")!).render(<App />);
