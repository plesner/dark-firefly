import createModule from "../gen/wasm/engine";
import type { MainModule } from "../gen/wasm/engine";
import { createContext } from "react";

export class Engine {
  wasm: MainModule;

  constructor(wasm: MainModule) {
    this.wasm = wasm;
  }

  getEight(): number {
    return this.wasm.getEight();
  }
}

export const EngineContext = createContext<Engine>(null!);

export async function createEngine() {
  const wasm = await createModule();
  return new Engine(wasm);
}
