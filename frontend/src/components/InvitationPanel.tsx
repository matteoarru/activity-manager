import { useEffect, useState } from "react";
import { request } from "../api/client";
import { Activity, Cnu } from "../domain/activity";

export function InvitationPanel({ activity }: { activity: Activity }) {
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
