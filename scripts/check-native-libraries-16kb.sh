#!/usr/bin/env bash
set -euo pipefail

archive="${1:?Usage: check-native-libraries-16kb.sh <APK-or-AAB>}"
test -s "$archive"

if ! command -v readelf >/dev/null 2>&1; then
  echo "::error::readelf is required to inspect native ELF load segments."
  exit 1
fi

mapfile -t native_entries < <(
  unzip -Z1 "$archive" |
    grep -E '(^|/)lib/(arm64-v8a|x86_64)/[^/]+\.so$' || true
)

if (("${#native_entries[@]}" == 0)); then
  echo "No arm64-v8a or x86_64 native libraries in $archive; ELF segment alignment is not applicable."
  exit 0
fi

tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT

index=0
for entry in "${native_entries[@]}"; do
  index=$((index + 1))
  library="$tmp_dir/library-$index.so"
  unzip -p "$archive" "$entry" > "$library"
  mapfile -t alignments < <(readelf -lW "$library" | awk '$1 == "LOAD" { print $NF }')

  if (("${#alignments[@]}" == 0)); then
    echo "::error::No ELF LOAD segments found in $entry"
    exit 1
  fi

  for alignment in "${alignments[@]}"; do
    if [[ ! "$alignment" =~ ^0x[0-9a-fA-F]+$ ]]; then
      echo "::error::Unrecognized ELF LOAD alignment '$alignment' in $entry"
      exit 1
    fi
    if (( alignment < 0x4000 )); then
      echo "::error::ELF LOAD alignment $alignment is below 16 KiB in $entry"
      exit 1
    fi
  done
  echo "16 KiB ELF LOAD alignment OK: $entry"
done
