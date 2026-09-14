import { Activity, formatDate, Profile } from "../domain/activity";
import { InvitationPanel } from "./InvitationPanel";
import { NominationPanel } from "./NominationPanel";

type ActivityDetailProps = {
  activity: Activity;
  canSetUpActivities: boolean;
  onBack: () => void;
  onEdit: () => void;
  onUpdated: (activity: Activity) => void;
  profile: Profile;
};

export function ActivityDetail({
  activity,
  canSetUpActivities,
  onBack,
  onEdit,
  onUpdated,
  profile,
}: ActivityDetailProps) {
  return (
    <section aria-labelledby="activity-detail-title" className="detail-page">
      <div className="detail-actions">
        <button
          className="icon-button back-link"
          type="button"
          onClick={onBack}
          aria-label="Back to activities"
          title="Back to activities"
        >
          ←
        </button>
        {canSetUpActivities && (
          <button
            className="icon-button"
            type="button"
            onClick={onEdit}
            aria-label="Edit activity"
            title="Edit activity"
          >
            ✎
          </button>
        )}
      </div>
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
        <div>
          <dt>Curricula file</dt>
          <dd>{activity.curriculaFileName ?? "Not attached"}</dd>
        </div>
      </dl>
      {canSetUpActivities && (
        <section
          aria-labelledby="invitations-title"
          className="connected-entity"
        >
          <p className="eyebrow">Connected entity</p>
          <h3 id="invitations-title">Nomination invitations</h3>
          <p>
            Select the CNUs that should receive an invitation for this activity.
          </p>
          <InvitationPanel activity={activity} onInvited={onUpdated} />
        </section>
      )}
      {profile.roles.includes("ROLE_CNU") && activity.status === "INVITED" && (
        <NominationPanel activity={activity} />
      )}
    </section>
  );
}
