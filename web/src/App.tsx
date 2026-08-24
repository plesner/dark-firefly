import { useContext } from "react";
import "./App.css";
import { EngineContext } from "./engine/engine";
import { Person } from "capnp-gen/test.capnp.js";
import * as capnp from "capnp-ts";

function App() {
  var engine = useContext(EngineContext);
  var m0 = new capnp.Message();
  var p0 = m0.initRoot(Person);
  p0.setEmail("a@b.com");
  var bytes = m0.toArrayBuffer();

  var m1 = new capnp.Message(bytes, false);
  var p1 = m1.getRoot(Person);
  console.log(p1);

  return <>{engine.getEight().toString()}</>;
}

export default App;
