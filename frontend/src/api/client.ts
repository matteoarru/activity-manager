export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const isMultipart = init?.body instanceof FormData;
  const headers = isMultipart
    ? { ...(init?.headers ?? {}) }
    : { "Content-Type": "application/json", ...(init?.headers ?? {}) };
  const response = await fetch(`/api${path}`, {
    credentials: "include",
    ...init,
    headers,
  });
  if (!response.ok) {
    const body = await response.text();
    throw new Error(
      body ||
        (response.status === 401
          ? "Invalid username or password"
          : "Request failed"),
    );
  }
  return response.json() as Promise<T>;
}
