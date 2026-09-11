import { FormEvent, useState } from "react";
import { request } from "../api/client";
import { Activity } from "../domain/activity";

type EditActivityPanelProps = {
  activity: Activity;
  onUpdated: (activity: Activity) => void;
  onCancel: () => void;
};

export function EditActivityPanel({
  activity,
  onUpdated,
  onCancel,
}: EditActivityPanelProps) {
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);

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
            title: form.get("title"),
            description: form.get("description"),
            venue: form.get("venue"),
            startsOn: form.get("startsOn"),
            endsOn: form.get("endsOn"),
            expectedParticipants: Number(form.get("expectedParticipants")),
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
        <button disabled={busy} type="submit">
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
