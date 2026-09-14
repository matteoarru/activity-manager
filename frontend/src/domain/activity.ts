export type Profile = { username: string; roles: string[] };

export type Activity = {
  id: string;
  courseReference: string;
  title: string;
  description: string;
  venue: string;
  startsOn: string;
  endsOn: string;
  status: string;
  expectedParticipants: number;
  cplReference?: string;
  curriculaFileName?: string;
  amUsername: string;
  supportUsernames: string;
  nominationDeadline?: string;
};

export type ActivityManager = { username: string; role: string };

export type Cnu = {
  username: string;
  organisation: string;
  countryCode: string;
};

export const formatDate = (value: string): string =>
  value
    ? new Intl.DateTimeFormat("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      }).format(new Date(`${value}T00:00:00`))
    : "Date not supplied";

export const loginProfiles = [
  ["AM", "am.alex"],
  ["PO", "po.petra"],
  ["IA", "ia.ines"],
  ["Authorising Officer", "ao.aaron"],
  ["Finance", "finance.fran"],
  ["Provider contact", "provider.pavel"],
  ["CNU", "cnu.clara"],
  ["CNU", "cnu.niko"],
  ["Attendee", "attendee.aria"],
  ["Technical administrator", "admin.taylor"],
] as const;

export const canSetUpActivities = (profile: Profile): boolean =>
  profile.roles.some((role) =>
    ["ROLE_AM", "ROLE_PO", "ROLE_IA", "ROLE_AO"].includes(role),
  );
