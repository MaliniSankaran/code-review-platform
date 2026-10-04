import { useState, useEffect } from "react";
import { api } from "./api";

export default function RepoDetail({ repo, onSelectPR }) {
    const [files, setFiles] = useState([]);
    const [prs, setPrs] = useState([]);
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [error, setError] = useState("");
    const [status, setStatus] = useState("");

    useEffect(() => {
        loadAll();
    }, [repo.id]);

    async function loadAll() {
        try {
            const [fileData, prData] = await Promise.all([
                api.getFiles(repo.id),
                api.getPullRequests(repo.id),
            ]);
            setFiles(fileData);
            setPrs(prData);
        } catch (err) {
            setError("Could not load repository contents.");
        }
    }

    async function handleUpload(e) {
        const file = e.target.files[0];
        if (!file) return;

        setStatus("Uploading...");
        try {
            await api.uploadFile(repo.id, file);
            setStatus(`Uploaded ${file.name}`);
            setTimeout(() => setStatus(""), 3000);
            loadAll();
        } catch (err) {
            setError("Upload failed.");
            setStatus("");
        }
        e.target.value = "";
    }

    async function handleCreatePR(e) {
        e.preventDefault();
        setError("");
        try {
            await api.createPullRequest(repo.id, title, description);
            setTitle("");
            setDescription("");
            setStatus("Pull request created — AI analysis running");
            setTimeout(() => setStatus(""), 4000);
            loadAll();
        } catch (err) {
            setError("Could not create pull request.");
        }
    }

    return (
        <div>
            <div className="card">
                <h2 style={{ fontSize: 16, marginTop: 0 }}>Files</h2>
                <input type="file" onChange={handleUpload} />
                {files.length === 0 ? (
                    <p className="muted">No files yet.</p>
                ) : (
                    <ul style={{ paddingLeft: 18, margin: "12px 0 0" }}>
                        {files.map((f) => (
                            <li key={f.id}>{f.fileName}</li>
                        ))}
                    </ul>
                )}
            </div>

            <form onSubmit={handleCreatePR} className="card">
                <h2 style={{ fontSize: 16, marginTop: 0 }}>New pull request</h2>
                <input
                    className="input"
                    placeholder="Title"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    required
                />
                <input
                    className="input"
                    placeholder="Description"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                />
                <button className="btn" type="submit">Create pull request</button>
            </form>

            {status && <p className="success">{status}</p>}
            {error && <p className="error">{error}</p>}

            <h2 style={{ fontSize: 16 }}>Pull requests</h2>
            {prs.length === 0 && <p className="muted">No pull requests yet.</p>}
            {prs.map((pr) => (
                <div
                    key={pr.id}
                    className="card"
                    style={{ cursor: "pointer" }}
                    onClick={() => onSelectPR(pr)}
                >
                    <strong>{pr.title}</strong>
                    <p className="muted" style={{ margin: "4px 0 0" }}>
                        #{pr.id} · {pr.status}
                    </p>
                </div>
            ))}
        </div>
    );
}