set -e

ALL_FILES="""
data/bundle.fbs
data/protocol.fbs
data/utils.fbs
"""

rm -rf engine/src/gen/flatc
$FLATBUFS_ROOT/flatc -o cpp/src/gen/flatc --cpp $ALL_FILES

rm -rf web/src/gen/flatc
$FLATBUFS_ROOT/flatc -o web/src/gen/flatc --ts $ALL_FILES --gen-all

rm -rf scala/src/gen/flatc
$FLATBUFS_ROOT/flatc -o scala/src/gen/flatc --java $ALL_FILES
