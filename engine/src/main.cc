#include <emscripten/bind.h>

using namespace emscripten;

float get_eight() {
  return 8;
}



EMSCRIPTEN_BINDINGS(module) {
  function("getEight", &get_eight);
}
