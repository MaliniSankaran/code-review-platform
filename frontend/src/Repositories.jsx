import { useState, useEffect } from "react";
import { api } from "./api";

export default function Repositories({ onSelect }) {
    const [repos, setRepos] = useState([]);
    const [name, setName] = useState("");
    const [description, setDescription] = useState("");
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [language, setLanguage] = useState("Java");

    useEffect(() => {
        loadRepos();
    }, []);

    async function loadRepos() {
        try {
            const data = await api.getRepositories();
            setRepos(data);
        } catch (err) {
            setError("Could not load repositories.");
        }
    }

    async function handleCreate(e) {
        e.preventDefault();
        setError("");
        try {
            await api.createRepository(name, description, language);
            setName("");
            setDescription("");
            setSuccess(`Repository "${name}" created`);
            setTimeout(() => setSuccess(""), 3000);
            loadRepos();
        } catch (err) {
            setError("Could not create repository.");
        }
    }

    return (
        <div>
            <form onSubmit={handleCreate} className="card">
                <input
                    className="input"
                    placeholder="Repository name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    required
                />
                <input
                    className="input"
                    placeholder="Description"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                />
                <select
                    className="input"
                    value={language}
                    onChange={(e) => setLanguage(e.target.value)}
                >
                    <option>Java</option>
                    <option>Python</option>
                    <option>JavaScript</option>
                    <option>TypeScript</option>
                    <option>Go</option>
                </select>
                <button className="btn" type="submit">Create repository</button>
            </form>

            {error && <p className="error">{error}</p>}
            {success && <p className="success">{success}</p>}

            {repos.length === 0 && <p className="muted">No repositories yet.</p>}

            {repos.map((repo) => (
                <div
                    key={repo.id}
                    className="card"
                    style={{ cursor: "pointer" }}
                    onClick={() => onSelect(repo)}
                >
                    <strong>{repo.name}</strong>
                    <p className="muted" style={{ margin: "4px 0 0" }}>
                        {repo.description || "No description"}
                    </p>
                </div>
            ))}
        </div>
    );
}