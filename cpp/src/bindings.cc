#include <emscripten/bind.h>

#include "gen/flatc/bundle_generated.h"

using namespace emscripten;

float get_eight(const std::string data) {
  auto monster = flatbuffers::GetRoot<dafi::StopPackageHeader>(data.c_str());
  return monster->quad();
}

EMSCRIPTEN_BINDINGS(module) {
  function("getEight", &get_eight);
}
