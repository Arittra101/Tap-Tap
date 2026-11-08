#!/usr/bin/env python3
"""
The mastering pass: bring each render to the loudness platforms expect.

Remotion mixes the voice, the score and the interface sounds, but it does not set a
programme level. YouTube, Instagram and TikTok all normalise to about -14 LUFS; a file
delivered quieter gets turned up by the platform, and one delivered louder gets turned
down and squashed. So: measure, then apply a linear gain with a true-peak ceiling.

Linear, not dynamic — the mix already has a narrow range and dynamic mode would pump
the music up in the gaps between lines.

    python3 audio/master.py out/habittap-promo-vertical.mp4 ...
"""
import json
import pathlib
import re
import subprocess
import sys

TARGET_I = -14.0
TARGET_TP = -1.5
TARGET_LRA = 11.0

FFMPEG = ["npx", "remotion", "ffmpeg"]


def run(args: list[str]) -> str:
    done = subprocess.run(FFMPEG + args, capture_output=True, text=True)
    return done.stdout + done.stderr


def measure(path: pathlib.Path) -> dict:
    out = run([
        "-i", str(path), "-vn",
        "-af", f"loudnorm=I={TARGET_I}:TP={TARGET_TP}:LRA={TARGET_LRA}:print_format=json",
        "-f", "null", "/dev/null",
    ])
    blocks = re.findall(r"\{[^{}]*\"input_i\"[^{}]*\}", out, re.S)
    if not blocks:
        sys.exit(f"Could not measure {path}:\n{out[-1500:]}")
    return json.loads(blocks[-1])


def master(path: pathlib.Path) -> None:
    m = measure(path)
    tmp = path.with_suffix(".mastered.mp4")
    filt = (
        f"loudnorm=I={TARGET_I}:TP={TARGET_TP}:LRA={TARGET_LRA}"
        f":measured_I={m['input_i']}:measured_TP={m['input_tp']}"
        f":measured_LRA={m['input_lra']}:measured_thresh={m['input_thresh']}"
        f":offset={m['target_offset']}:linear=true:print_format=summary"
    )
    out = run([
        "-y", "-i", str(path),
        "-c:v", "copy",                 # the picture is untouched
        "-af", filt,
        "-c:a", "aac", "-b:a", "192k", "-ar", "48000",
        "-movflags", "+faststart",      # starts playing before it has fully loaded
        str(tmp),
    ])
    if not tmp.exists() or tmp.stat().st_size == 0:
        sys.exit(f"Mastering failed for {path}:\n{out[-1500:]}")
    tmp.replace(path)
    after = measure(path)
    print(
        f"{path.name}: {m['input_i']} LUFS / {m['input_tp']} dBTP"
        f"  ->  {after['input_i']} LUFS / {after['input_tp']} dBTP"
    )


if __name__ == "__main__":
    targets = [pathlib.Path(a) for a in sys.argv[1:]]
    if not targets:
        sys.exit("usage: master.py <file.mp4> [...]")
    for target in targets:
        master(target)
