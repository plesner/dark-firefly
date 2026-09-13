import { useContext } from "react";
import "./App.css";
import { EngineContext } from "./engine/engine";

import * as flatbuffers from 'flatbuffers'
import { StopPackageHeader } from "./gen/flatbuf/dafi/flatbuf/stop-package-header";

function App() {
  var engine = useContext(EngineContext);
  let builder = new flatbuffers.Builder(1024);
  StopPackageHeader.startStopPackageHeader(builder);
  StopPackageHeader.addQuad(builder, 101);
  let stopHeader = StopPackageHeader.endStopPackageHeader(builder);
  builder.finish(stopHeader);
  let data = builder.asUint8Array();
  return <>{engine.getEight(data).toString()}</>;
}

export default App;
