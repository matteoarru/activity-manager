import { FormEvent, useState } from "react";
import { request } from "../api/client";
import { Activity } from "../domain/activity";

export function NominationPanel({ activity }: { activity: Activity }) {
  const [message, setMessage] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formElement = event.currentTarget;
    const form = new FormData(formElement);
    try {
      await request<void>(`/v1/activities/${activity.id}/nominations`, {
        method: "POST",
        body: JSON.stringify({ nomineeName: form.get("nomineeName") }),
      });
      formElement.reset();
      setMessage("Nomination submitted.");
    } catch (reason) {
      setMessage(reason instanceof Error ? reason.message : "Nomination failed");
    }
  }

  return (
    <section aria-labelledby="nomination-title" className="connected-entity">
      <p className="eyebrow">CNU action</p>
      <h3 id="nomination-title">Submit nominations</h3>
      <form onSubmit={submit}>
        <label>
          Nominee name
          <input required name="nomineeName" />
        </label>
        <button type="submit">Submit nomination</button>
      </form>
      {message && <p role="status">{message}</p>}
    </section>
  );
}
