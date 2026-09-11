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
  const role =
    profile.roles
      .find((item) => item.startsWith("ROLE_"))
      ?.replace("ROLE_", "") ?? "USER";
  const canManageActivities = canSetUpActivities(profile);

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

  function saveUpdatedActivity(updated: Activity) {
    setActivities((current) =>
      current.map((activity) =>
        activity.id === updated.id ? updated : activity,
      ),
    );
    setSelectedActivity(updated);
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
            onCreated={(activity) => {
              setActivities((current) => [...current, activity]);
              setShowSetup(false);
            }}
            onCancel={() => setShowSetup(false)}
          />
        ) : editingActivity ? (
          <EditActivityPanel
            activity={editingActivity}
            onUpdated={saveUpdatedActivity}
            onCancel={() => setEditingActivity(null)}
          />
        ) : selectedActivity ? (
          <ActivityDetail
            activity={selectedActivity}
            canSetUpActivities={canManageActivities}
            onBack={() => setSelectedActivity(null)}
            onEdit={() => setEditingActivity(selectedActivity)}
          />
        ) : (
          <ActivityList
            activities={activities}
            canSetUpActivities={canManageActivities}
            onAddActivity={() => setShowSetup(true)}
            onSelectActivity={setSelectedActivity}
          />
        )}
      </main>
    </>
  );
}
