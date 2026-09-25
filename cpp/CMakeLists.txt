cmake_minimum_required(VERSION 3.8)
project(dafi)
add_definitions(-std=c++17)
set (CMAKE_CXX_STANDARD 17)

if (DEFINED EMSCRIPTEN)
  add_executable(engine src/wasm/bindings.cc)
  set_target_properties(engine PROPERTIES COMPILE_FLAGS "-Os -s SIDE_MODULE=1 ")
  set_target_properties(engine PROPERTIES LINK_FLAGS "-lembind -o engine.mjs --emit-tsd engine.d.ts --no-entry")
else()
  add_library(engine SHARED src/bindings.cc)
endif()

target_include_directories(engine PUBLIC "$ENV{FLATBUFS_ROOT}/include/")
