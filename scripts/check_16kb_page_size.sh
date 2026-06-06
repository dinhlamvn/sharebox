#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <app.apk|app.aab>" >&2
  exit 64
fi

artifact="$1"

if [[ ! -f "$artifact" ]]; then
  echo "Artifact not found: $artifact" >&2
  exit 66
fi

find_android_tool() {
  local tool="$1"
  local sdk_root="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}}"

  if command -v "$tool" >/dev/null 2>&1; then
    command -v "$tool"
    return 0
  fi

  if [[ -d "$sdk_root" ]]; then
    local found
    found="$(find "$sdk_root" -name "$tool" \( -type f -o -type l \) 2>/dev/null | sort | tail -1 || true)"
    if [[ -n "$found" ]]; then
      echo "$found"
      return 0
    fi
  fi

  return 1
}

readelf_bin="$(find_android_tool llvm-readelf || find_android_tool readelf || true)"
if [[ -z "$readelf_bin" ]]; then
  echo "Could not find llvm-readelf/readelf. Install Android NDK r28+ or set ANDROID_HOME." >&2
  exit 69
fi

tmpdir="$(mktemp -d "${TMPDIR:-/tmp}/sharebox-16kb.XXXXXX")"
cleanup() {
  rm -rf "$tmpdir"
}
trap cleanup EXIT

case "$artifact" in
  *.apk)
    unzip -q "$artifact" 'lib/*.so' -d "$tmpdir" >/dev/null 2>&1 || true
    ;;
  *.aab)
    unzip -q "$artifact" 'base/lib/*.so' -d "$tmpdir" >/dev/null 2>&1 || true
    ;;
  *)
    echo "Unsupported artifact type. Expected .apk or .aab: $artifact" >&2
    exit 64
    ;;
esac

shared_libs_file="$tmpdir/shared-libs.txt"
find "$tmpdir" -name '*.so' -type f | sort > "$shared_libs_file"

if [[ ! -s "$shared_libs_file" ]]; then
  echo "No native shared libraries found. 16 KB page-size ELF alignment is not required."
else
  failed=0
  while IFS= read -r lib; do
    rel="${lib#$tmpdir/}"
    aligns="$("$readelf_bin" -l "$lib" | awk '/^[[:space:]]+LOAD/ { print $NF }' | sort -u)"

    while IFS= read -r align; do
      [[ -z "$align" ]] && continue
      case "$align" in
        0x[4-9][0-9][0-9][0-9]|0x[1-9][0-9][0-9][0-9][0-9]*)
          ;;
        *)
          echo "UNALIGNED $rel LOAD align=$align"
          failed=1
          ;;
      esac
    done <<< "$aligns"

    if [[ "$aligns" != *"0x1000"* && "$aligns" != *"0x2000"* ]]; then
      echo "ALIGNED   $rel LOAD align=$(echo "$aligns" | paste -sd, -)"
    fi
  done < "$shared_libs_file"

  if [[ $failed -ne 0 ]]; then
    echo "One or more native libraries are not 16 KB ELF aligned." >&2
    exit 1
  fi
fi

if [[ "$artifact" == *.apk ]]; then
  zipalign_bin="$(find_android_tool zipalign || true)"
  if [[ -z "$zipalign_bin" ]]; then
    echo "Skipping APK zip alignment check; zipalign was not found." >&2
    exit 0
  fi

  "$zipalign_bin" -c -P 16 4 "$artifact"
fi

echo "16 KB page-size checks passed for $artifact"
