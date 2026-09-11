import { Profile } from "../domain/activity";

type WorkspaceHeaderProps = {
  profile: Profile;
  role: string;
  showUserMenu: boolean;
  onToggleUserMenu: () => void;
  onLogout: () => void;
};

export function WorkspaceHeader({
  profile,
  role,
  showUserMenu,
  onToggleUserMenu,
  onLogout,
}: WorkspaceHeaderProps) {
  return (
    <header>
      <div>
        <p className="eyebrow">Activity Management Suite</p>
        <h1>Event Operations</h1>
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
            onClick={onToggleUserMenu}
          >
            {profile.username.split(".")[0].slice(0, 2).toUpperCase()}
          </button>
        </div>
        {showUserMenu && (
          <div className="user-menu">
            <button className="utility-button" type="button" onClick={onLogout}>
              Sign out
            </button>
          </div>
        )}
      </div>
    </header>
  );
}
