import { useContext, useState } from "react";
import "./App.css";
import { EngineContext } from "./engine/engine";
import { useEffect } from "react";

function EngineOutput(params: { data: Uint8Array }) {
  var engine = useContext(EngineContext);
  return <>{engine.getEight(params.data).toString()}</>;
}

function App() {
  const [bytes, setBytes] = useState<Uint8Array>();

  useEffect(() => {
    fetch("http://localhost:7120/test.ctfs")
      .then(response => response.bytes())
      .then(b => setBytes(b))
  }, []);

  return <>{bytes && <EngineOutput data={bytes} />}</>;
}

export default App;
