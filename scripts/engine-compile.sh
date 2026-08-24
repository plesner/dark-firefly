EMSCRIPTEN_ROOT="${EMSCRIPTEN_ROOT:-}"

if [ -n "$EMSCRIPTEN_ROOT" ]; then
  EMPLUSPLUS="$EMSCRIPTEN_ROOT/em++"
else
  EMPLUSPLUS="em++"
fi

"$EMPLUSPLUS" \
  -lembind \
  -o web/src/gen/wasm/engine.mjs \
  --no-entry \
  --emit-tsd engine.d.ts \
  engine/src/main.cc
