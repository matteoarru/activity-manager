import { useEffect, useState } from "react";
import { request } from "../api/client";
import { Activity, canSetUpActivities, Profile } from "../domain/activity";
import { ActivityDetail } from "./ActivityDetail";
import { ActivityList } from "./ActivityList";
import { EditActivityPanel } from "./EditActivityPanel";
import { SetupPanel } from "./SetupPanel";
import { WorkspaceHeader } from "./WorkspaceHeader";

type WorkspaceProps = {
  profile: Profile;
  onLogout: () => void;
};

export function Workspace({ profile, onLogout }: WorkspaceProps) {
  const [activities, setActivities] = useState<Activity[]>([]);
  const [showSetup, setShowSetup] = useState(false);
  const [editingActivity, setEditingActivity] = useState<Activity | null>(null);
  const [selectedActivity, setSelectedActivity] = useState<Activity | null>(
    null,
  );
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [showAllActivities, setShowAllActivities] = useState(false);
  const role =
    profile.roles
      .find((item) => item.startsWith("ROLE_"))
      ?.replace("ROLE_", "") ?? "USER";
  const canManageActivities = canSetUpActivities(profile);

  useEffect(() => {
    request<Activity[]>(`/v1/activities${showAllActivities ? "?showAll=true" : ""}`)
      .then(setActivities)
      .catch(() => undefined);
  }, [showAllActivities]);

  async function logout() {
    try {
      await request<{ status: string }>("/auth/logout", { method: "POST" });
    } finally {
      onLogout();
    }
  }

  function saveUpdatedActivity(updated: Activity) {
    const remainsAssigned = isAssignedTo(updated, profile.username);
    setActivities((current) =>
      !showAllActivities && !remainsAssigned
        ? current.filter((activity) => activity.id !== updated.id)
        : current.map((activity) =>
            activity.id === updated.id ? updated : activity,
          ),
    );
    setSelectedActivity(!showAllActivities && !remainsAssigned ? null : updated);
    setEditingActivity(null);
  }

  const mainTarget = showSetup
    ? "#setup"
    : editingActivity
      ? "#edit-activity-title"
      : selectedActivity
        ? "#activity-detail-title"
        : "#activities";

  return (
    <>
      <a className="skip-link" href={mainTarget}>
        Skip to main content
      </a>
      <main id="main-content">
        <WorkspaceHeader
          profile={profile}
          role={role}
          showUserMenu={showUserMenu}
          onToggleUserMenu={() => setShowUserMenu((current) => !current)}
          onLogout={logout}
        />
        {showSetup ? (
          <SetupPanel
            currentUsername={profile.username}
            onCreated={(activity) => {
              setActivities((current) => [...current, activity]);
              setShowSetup(false);
            }}
            onCancel={() => setShowSetup(false)}
          />
        ) : editingActivity ? (
          <EditActivityPanel
            activity={editingActivity}
            currentUsername={profile.username}
            onUpdated={saveUpdatedActivity}
            onCancel={() => setEditingActivity(null)}
          />
        ) : selectedActivity ? (
          <ActivityDetail
            activity={selectedActivity}
            canSetUpActivities={
              canManageActivities &&
              (role === "AO" || isAssignedTo(selectedActivity, profile.username))
            }
            onBack={() => setSelectedActivity(null)}
            onEdit={() => setEditingActivity(selectedActivity)}
            onUpdated={saveUpdatedActivity}
            profile={profile}
          />
        ) : (
          <ActivityList
            activities={activities}
            canSetUpActivities={canManageActivities}
            onAddActivity={() => setShowSetup(true)}
            onSelectActivity={setSelectedActivity}
            showAllActivities={showAllActivities}
            onShowAllActivitiesChange={setShowAllActivities}
          />
        )}
      </main>
    </>
  );
}

function isAssignedTo(activity: Activity, username: string): boolean {
  return [activity.amUsername, ...activity.supportUsernames.split(",")].includes(username);
}
