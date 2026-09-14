import { useEffect, useMemo, useState } from "react";
import { Activity, formatDate } from "../domain/activity";

type ActivitySort = "date-asc" | "date-desc" | "title-asc" | "status-asc";

type ActivityListProps = {
  activities: Activity[];
  canSetUpActivities: boolean;
  onAddActivity: () => void;
  onSelectActivity: (activity: Activity) => void;
  showAllActivities: boolean;
  onShowAllActivitiesChange: (showAll: boolean) => void;
};

export function ActivityList({
  activities,
  canSetUpActivities,
  onAddActivity,
  onSelectActivity,
  showAllActivities,
  onShowAllActivitiesChange,
}: ActivityListProps) {
  const [sort, setSort] = useState<ActivitySort>("date-asc");
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(5);
  const sortedActivities = useMemo(
    () =>
      [...activities].sort((left, right) =>
        compareActivities(left, right, sort),
      ),
    [activities, sort],
  );
  const pageCount = Math.max(1, Math.ceil(sortedActivities.length / pageSize));
  const visibleActivities = sortedActivities.slice(
    (page - 1) * pageSize,
    page * pageSize,
  );

  useEffect(() => setPage(1), [sort, pageSize]);
  useEffect(() => {
    if (page > pageCount) {
      setPage(pageCount);
    }
  }, [page, pageCount]);

  return (
    <section aria-labelledby="activities" className="activity-home">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Workspace</p>
          <h2 id="activities">Activities</h2>
          <p>{showAllActivities ? "All activities." : "Activities assigned to you."}</p>
        </div>
        {canSetUpActivities && (
          <button type="button" onClick={onAddActivity}>
            Add activity
          </button>
        )}
      </div>
      <div className="activity-controls" aria-label="Activity list controls">
        <label className="check-label">
          <input
            type="checkbox"
            checked={showAllActivities}
            onChange={(event) => onShowAllActivitiesChange(event.target.checked)}
          />
          Show all activities
        </label>
        {activities.length > 0 && (
          <div
            className="activity-sort-control"
          >
            <label>
              Order by
              <select
                value={sort}
                onChange={(event) =>
                  setSort(event.target.value as ActivitySort)
                }
              >
                <option value="date-asc">Start date (oldest first)</option>
                <option value="date-desc">Start date (newest first)</option>
                <option value="title-asc">Title (A–Z)</option>
                <option value="status-asc">Status (A–Z)</option>
              </select>
            </label>
          </div>
        )}
      </div>
      {activities.length === 0 ? (
        <p className="empty-state">No activities are assigned to this profile.</p>
      ) : (
        <>
          <ul className="activity-list" aria-label="Activities">
            {visibleActivities.map((activity) => (
              <li key={activity.id}>
                <button
                  className="activity-row"
                  type="button"
                  onClick={() => onSelectActivity(activity)}
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
          <nav className="activity-pagination" aria-label="Activity pages">
            <div className="pagination-summary">
              <span aria-live="polite">
                Page {page} of {pageCount}
              </span>
              <label>
                Activities per page
                <select
                  value={pageSize}
                  onChange={(event) => setPageSize(Number(event.target.value))}
                >
                  <option value="1">1</option>
                  <option value="2">2</option>
                  <option value="5">5</option>
                  <option value="10">10</option>
                </select>
              </label>
            </div>
            <button
              className="secondary pagination-button"
              type="button"
              onClick={() => setPage((current) => current - 1)}
              disabled={page === 1}
            >
              Previous
            </button>
            <button
              className="secondary pagination-button"
              type="button"
              onClick={() => setPage((current) => current + 1)}
              disabled={page === pageCount}
            >
              Next
            </button>
          </nav>
        </>
      )}
    </section>
  );
}

function compareActivities(
  left: Activity,
  right: Activity,
  sort: ActivitySort,
) {
  if (sort === "title-asc") {
    return left.title.localeCompare(right.title);
  }
  if (sort === "status-asc") {
    return (
      left.status.localeCompare(right.status) ||
      left.title.localeCompare(right.title)
    );
  }
  const direction = sort === "date-asc" ? 1 : -1;
  return (
    direction *
    (left.startsOn.localeCompare(right.startsOn) ||
      left.title.localeCompare(right.title))
  );
}
