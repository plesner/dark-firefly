CAPNP="capnp"

$CAPNP compile \
  -ots:web/commonjs/capnp-gen \
  --src-prefix proto \
  proto/test.capnp
