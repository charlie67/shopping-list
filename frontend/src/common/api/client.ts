export async function apiGet<T>(url: string): Promise<T> {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`API error: ${response.status} ${response.statusText}`);
    }
    return response.json();
}

export async function apiPost<T>(url: string, body?: unknown): Promise<T> {
    const response = await fetch(url, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: body ? JSON.stringify(body) : undefined,
    });
    if (!response.ok) {
        throw new Error(`API error: ${response.status} ${response.statusText}`);
    }
    return response.json();
}

export async function apiPatch<T>(url: string, body: unknown): Promise<T> {
    const response = await fetch(url, {
        method: 'PATCH',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(body),
    });
    if (!response.ok) {
        throw new Error(`API error: ${response.status} ${response.statusText}`);
    }
    return response.json();
}

export async function apiDelete(url: string): Promise<void> {
    const response = await fetch(url, {method: 'DELETE'});
    if (!response.ok) {
        throw new Error(`API error: ${response.status} ${response.statusText}`);
    }
}
