import { useState } from "react";
import { getToken, clearToken } from "./api";
import Login from "./Login";
import Repositories from "./Repositories";
import RepoDetail from "./RepoDetail";
import PRDetail from "./PRDetail";

export default function App() {
  const [user, setUser] = useState(null);
  const [loggedIn, setLoggedIn] = useState(!!getToken());
  const [selectedRepo, setSelectedRepo] = useState(null);
  const [selectedPR, setSelectedPR] = useState(null);

  function handleLogin(user) {
    setUser(user);
    setLoggedIn(true);
  }

  function handleLogout() {
    clearToken();
    setUser(null);
    setLoggedIn(false);
    setSelectedRepo(null);
    setSelectedPR(null);
  }

  if (!loggedIn) {
    return <Login onLogin={handleLogin} />;
  }

  return (
      <div className="page">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <h1 style={{ fontSize: 22 }}>
            {selectedPR ? selectedPR.title : selectedRepo ? selectedRepo.name : "Repositories"}
          </h1>
          <div>
            {(selectedRepo || selectedPR) && (
                <button
                    className="btn"
                    style={{ background: "#59636e", marginRight: 8 }}
                    onClick={() => {
                      if (selectedPR) setSelectedPR(null);
                      else setSelectedRepo(null);
                    }}
                >
                  Back
                </button>
            )}
            <button className="btn" onClick={handleLogout}>Sign out</button>
          </div>
        </div>

        {selectedPR ? (
            <PRDetail pr={selectedPR} />
        ) : selectedRepo ? (
            <RepoDetail repo={selectedRepo} onSelectPR={setSelectedPR} />
        ) : (
            <Repositories onSelect={setSelectedRepo} />
        )}
      </div>
  );
}