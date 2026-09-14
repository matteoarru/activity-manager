import { FormEvent, useEffect, useState } from "react";
import { request } from "../api/client";
import { Activity, ActivityManager } from "../domain/activity";

type SetupPanelProps = {
  onCreated: (activity: Activity) => void;
  onCancel: () => void;
  currentUsername: string;
};

export function SetupPanel({ onCreated, onCancel, currentUsername }: SetupPanelProps) {
  const [message, setMessage] = useState("");
  const [busy, setBusy] = useState(false);
  const [managers, setManagers] = useState<ActivityManager[]>([]);
  const [managerUsernames, setManagerUsernames] = useState<string[]>([currentUsername]);

  useEffect(() => {
    request<ActivityManager[]>("/v1/activity-managers").then(setManagers).catch(() => undefined);
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setMessage("");
    const form = new FormData(event.currentTarget);
    const payload = {
      arn: form.get("arn"),
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
      nominationDeadline: form.get("nominationDeadline"),
      cplReference: form.get("cplReference"),
      cplByCostType: {
        travel: form.get("travelCpl"),
        hotel: form.get("hotelCpl"),
        catering: form.get("cateringCpl"),
      },
      managerUsernames,
    };
    try {
      let activity = await request<Activity>("/v1/activities", {
        method: "POST",
        body: JSON.stringify(payload),
      });
      const file = form.get("curriculum");
      if (file instanceof File && file.size > 0) {
        const upload = new FormData();
        upload.append("file", file);
        activity = await request<Activity>(
          `/v1/activities/${activity.id}/curriculum`,
          {
            method: "POST",
            body: upload,
            headers: {},
          },
        );
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
          Activity Reference Number (ARN)
          <input required name="arn" placeholder="2026-ARN-01" />
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
          <label>
            Nomination deadline
            <input required name="nominationDeadline" type="date" />
          </label>
        </div>
        <fieldset>
          <legend>Activity managers</legend>
          <p className="hint">Assign one or more AM, PO, IA or AO users.</p>
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
        <button disabled={busy || managerUsernames.length === 0} type="submit">
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
