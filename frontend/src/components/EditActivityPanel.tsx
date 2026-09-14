import { FormEvent, useEffect, useState } from "react";
import { request } from "../api/client";
import { Activity, ActivityManager } from "../domain/activity";

type EditActivityPanelProps = {
  activity: Activity;
  onUpdated: (activity: Activity) => void;
  onCancel: () => void;
  currentUsername: string;
};

export function EditActivityPanel({
  activity,
  onUpdated,
  onCancel,
  currentUsername,
}: EditActivityPanelProps) {
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);
  const [managers, setManagers] = useState<ActivityManager[]>([]);
  const [managerUsernames, setManagerUsernames] = useState(() => assignedManagers(activity, currentUsername));

  useEffect(() => {
    request<ActivityManager[]>("/v1/activity-managers").then(setManagers).catch(() => undefined);
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setMessage("");
    const form = new FormData(event.currentTarget);
    try {
      onUpdated(
        await request<Activity>(`/v1/activities/${activity.id}`, {
          method: "PUT",
          body: JSON.stringify({
            arn: form.get("arn"),
            title: form.get("title"),
            description: form.get("description"),
            venue: form.get("venue"),
            startsOn: form.get("startsOn"),
            endsOn: form.get("endsOn"),
            expectedParticipants: Number(form.get("expectedParticipants")),
            managerUsernames,
          }),
        }),
      );
    } catch (reason) {
      setMessage(
        reason instanceof Error ? reason.message : "Activity update failed",
      );
    } finally {
      setBusy(false);
    }
  }

  return (
    <section aria-labelledby="edit-activity-title">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Activity management</p>
          <h2 id="edit-activity-title">Edit activity</h2>
          <p>Update the operational details for {activity.courseReference}.</p>
        </div>
        <button className="secondary" type="button" onClick={onCancel}>
          Cancel
        </button>
      </div>
      <form className="setup-form" onSubmit={submit}>
        <label>
          Activity Reference Number (ARN)
          <input required name="arn" defaultValue={activity.courseReference} />
        </label>
        <label>
          Title
          <input required name="title" defaultValue={activity.title} />
        </label>
        <label>
          Description
          <textarea
            name="description"
            rows={3}
            defaultValue={activity.description}
          />
        </label>
        <div className="form-grid">
          <label>
            Venue
            <input required name="venue" defaultValue={activity.venue} />
          </label>
          <label>
            Expected participants
            <input
              required
              name="expectedParticipants"
              type="number"
              min="0"
              defaultValue={activity.expectedParticipants}
            />
          </label>
          <label>
            Starts
            <input
              required
              name="startsOn"
              type="date"
              defaultValue={activity.startsOn}
            />
          </label>
          <label>
            Ends
            <input
              required
              name="endsOn"
              type="date"
              defaultValue={activity.endsOn}
            />
          </label>
        </div>
        <fieldset>
          <legend>Activity managers</legend>
          {managers.map((manager) => (
            <label className="check-label" key={manager.username}>
              <input
                type="checkbox"
                checked={managerUsernames.includes(manager.username)}
                onChange={(event) => setManagerUsernames((current) =>
                  event.target.checked
                    ? [...current, manager.username]
                    : current.filter((username) => username !== manager.username),
                )}
              />
              {manager.username} · {manager.role}
            </label>
          ))}
        </fieldset>
        <button disabled={busy || managerUsernames.length === 0} type="submit">
          {busy ? "Saving…" : "Save changes"}
        </button>
        {message && (
          <p role="alert" className="error">
            {message}
          </p>
        )}
      </form>
    </section>
  );
}

function assignedManagers(activity: Activity, fallback: string): string[] {
  const assigned = [activity.amUsername, ...activity.supportUsernames.split(",")].filter(Boolean);
  return assigned.length ? assigned : [fallback];
}
