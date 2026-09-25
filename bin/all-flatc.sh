set -e

FLATBUFS_ROOT="${FLATBUFS_ROOT:-/usr/bin}"

ALL_FILES="""
data/bundle.fbs
data/columns.fbs
data/protocol.fbs
data/stops.fbs
data/utils.fbs
"""

rm -rf engine/src/gen/flatbuf
$FLATBUFS_ROOT/flatc -o cpp/src/gen/flatbuf --cpp $ALL_FILES

rm -rf web/src/gen/flatbuf
$FLATBUFS_ROOT/flatc -o web/src/gen/flatbuf --ts $ALL_FILES --gen-all

rm -rf scala/src/main/java/
$FLATBUFS_ROOT/flatc -o scala/src/main/java/ --java $ALL_FILES
