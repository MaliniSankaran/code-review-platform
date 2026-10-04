import { useState, useEffect } from "react";
import { api } from "./api";

export default function PRDetail({ pr }) {
    const [comments, setComments] = useState([]);
    const [content, setContent] = useState("");
    const [error, setError] = useState("");

    useEffect(() => {
        loadComments();
    }, [pr.id]);

    async function loadComments() {
        try {
            const data = await api.getComments(pr.id);
            setComments(data);
        } catch (err) {
            setError("Could not load comments.");
        }
    }

    async function handleAddComment(e) {
        e.preventDefault();
        if (!content.trim()) return;
        try {
            await api.addComment(pr.id, content);
            setContent("");
            loadComments();
        } catch (err) {
            setError("Could not post comment.");
        }
    }

    return (
        <div>
            <div className="card">
                <strong>{pr.title}</strong>
                <p className="muted" style={{ margin: "4px 0 0" }}>
                    #{pr.id} · {pr.status} · opened by {pr.authorUsername}
                </p>
                {pr.description && <p style={{ marginBottom: 0 }}>{pr.description}</p>}
            </div>

            <h2 style={{ fontSize: 16 }}>
                Comments {comments.length > 0 && `(${comments.length})`}
            </h2>

            {comments.length === 0 && <p className="muted">No comments yet.</p>}

            {comments.map((c) => (
                <div key={c.id} className="card">
                    <p className="muted" style={{ margin: "0 0 6px" }}>
                        {c.authorUserName}
                        {c.authorUserName === "ai-service" && " · AI"}
                        {c.lineNumber && ` · line ${c.lineNumber}`}
                    </p>
                    <div style={{ whiteSpace: "pre-wrap" }}>{c.content}</div>
                </div>
            ))}

            <form onSubmit={handleAddComment} className="card">
        <textarea
            className="input"
            rows={3}
            placeholder="Leave a comment"
            value={content}
            onChange={(e) => setContent(e.target.value)}
        />
                <button className="btn" type="submit">Comment</button>
            </form>

            {error && <p className="error">{error}</p>}
        </div>
    );
}