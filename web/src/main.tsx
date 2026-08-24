import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.tsx";
import { createEngine, type Engine, EngineContext } from "./engine/engine.tsx";

createEngine().then((engine) => {
  main(engine);
});

function main(engine: Engine) {
  createRoot(document.getElementById("root")!).render(
    <StrictMode>
      <EngineContext value={engine}>
        <App />
      </EngineContext>
    </StrictMode>,
  );
}
