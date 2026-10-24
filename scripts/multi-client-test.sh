#!/bin/bash
# Test nhieu client cung luc: moi client gui 1 tin nhom, kiem tra moi client nhan du N tin.
# Dung: bash scripts/multi-client-test.sh [host] [so client]
# Yeu cau: Server dang chay, da build vao target/classes.
HOST=${1:-localhost}
N=${2:-10}
GSON="${GSON:-$USERPROFILE/.m2/repository/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar}"
case "$(uname)" in MINGW*|MSYS*|CYGWIN*) SEP=";";; *) SEP=":";; esac
CP="target/classes${SEP}${GSON}"
OUT=$(mktemp -d)

echo "== Chay $N client ket noi toi $HOST:5000 =="
for i in $(seq 1 "$N"); do
  ( sleep 4; echo "/all xin chao tu s$i"; sleep 6; echo "/quit" ) \
    | java -cp "$CP" com.nhom15.chat.consoleclient.ConsoleClient "$HOST" 5000 "s$i" > "$OUT/s$i.txt" 2>&1 &
done
wait

OK=0
for i in $(seq 1 "$N"); do
  GOT=$(grep -c '"action":"MSG_GROUP"' "$OUT/s$i.txt")
  if [ "$GOT" -eq "$N" ]; then OK=$((OK+1)); else echo "s$i chi nhan $GOT/$N tin"; fi
done
echo "Co $OK/$N client nhan du $N tin nhom."
rm -rf "$OUT"
if [ "$OK" -eq "$N" ]; then echo "KET QUA: DAT"; else echo "KET QUA: KHONG DAT"; fi
