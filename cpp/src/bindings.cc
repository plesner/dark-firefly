#include <emscripten/bind.h>

#include "gen/flatbuf/bundle_generated.h"

using namespace emscripten;

float get_eight(const std::string data) {
  auto tree = flatbuffers::GetRoot<dafi::flatbuf::ZQuadTree>(data.c_str());
  return tree->branches()->Get(0)->quarter();
}

EMSCRIPTEN_BINDINGS(module) {
  function("getEight", &get_eight);
}
