const BASE_URL = import.meta.env.VITE_API_URL;

function getToken() {
    return localStorage.getItem("token");
}

function setToken(token) {
    localStorage.setItem("token", token);
}

function clearToken() {
    localStorage.removeItem("token");
}

async function request(path, options = {}) {
    const token = getToken();

    const headers = { ...options.headers };
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }
    if (options.body && !(options.body instanceof FormData)) {
        headers["Content-Type"] = "application/json";
    }

    const res = await fetch(`${BASE_URL}${path}`, { ...options, headers });

    if (res.status === 401) {
        clearToken();
        window.location.reload();
        return;
    }

    if (!res.ok) {
        const text = await res.text();
        throw new Error(text || `Request failed with ${res.status}`);
    }

    if (res.status === 204) return null;
    return res.json();
}

export const api = {
    login: (email, password) =>
        request("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({ email, password }),
        }),

    register: (username, email, password, fullName) =>
        request("/api/auth/register", {
            method: "POST",
            body: JSON.stringify({ username, email, password, fullName }),
        }),

    getRepositories: () => request("/api/repositories"),

    createRepository: (name, description, language) =>
        request("/api/repositories", {
            method: "POST",
            body: JSON.stringify({ name, description, language }),
        }),

    getFiles: (repoId) => request(`/api/repositories/${repoId}/files`),

    uploadFile: (repoId, file) => {
        const formData = new FormData();
        formData.append("file", file);
        return request(`/api/repositories/${repoId}/files`, {
            method: "POST",
            body: formData,
        });
    },

    getPullRequests: (repoId) => request(`/api/repositories/${repoId}/pulls`),

    createPullRequest: (repoId, title, description) =>
        request(`/api/repositories/${repoId}/pulls`, {
            method: "POST",
            body: JSON.stringify({ title, description }),
        }),
    getComments: (prId) => request(`/api/pulls/${prId}/comments`),

    addComment: (prId, content) =>
        request(`/api/pulls/${prId}/comments`, {
            method: "POST",
            body: JSON.stringify({ content }),
        }),
};

export { getToken, setToken, clearToken };