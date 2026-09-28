BINDIR=out
CPP_BINDIR=$(BINDIR)/cpp
ENGINE_WASM=$(CPP_BINDIR)/engine.wasm
WEB_WASM_ROOT=web/src/gen/wasm

cmake:
	mkdir -p $(CPP_BINDIR)
	cd $(CPP_BINDIR) && emcmake cmake ../../cpp

.PHONY: $(CPP_BINDIR)/engine.wasm
$(CPP_BINDIR)/engine.wasm:
	make -C $(CPP_BINDIR)

$(WEB_WASM_ROOT)/engine.wasm: $(CPP_BINDIR)/engine.wasm
	cp $(CPP_BINDIR)/engine.* $(WEB_WASM_ROOT)

.PHONY: build-cpp
build-cpp: $(CPP_BINDIR)/engine.wasm

.PHONY: build-web
build-web: $(WEB_WASM_ROOT)/engine.wasm
	cd web && npm run build
