#!/usr/bin/env python3
"""
Speak the script, then cut the film to the voice.

macOS `say` does the speaking, so the voice track needs no network and no key. The part
that matters is *phrasing*: each scene's line is written as a list of clauses, each one
synthesised separately, trimmed to its first and last sound, and reassembled with an
exact pause between. A single long utterance is what makes text-to-speech sound like a
machine reading; clauses with breath between them is what a person does.

`audio/polish_vo.py` then runs the vocal chain over the result, and the measured length
of each scene's track decides how long that scene runs — the edit follows the voice.

The result is `src/timeline.json`, the one place the video and the music read timings
from.

    python3 audio/build_vo.py              # Samantha, the default
    VOICE="Karen" RATE=155 python3 audio/build_vo.py
    python3 audio/build_vo.py --keep       # re-measure WAVs already in public/vo/
"""
import json
import os
import pathlib
import subprocess
import sys
import tempfile
import wave

import numpy as np

from polish_vo import polish, read_wav, write_wav

HERE = pathlib.Path(__file__).resolve().parent
ROOT = HERE.parent
FPS = 30
SR = 48_000

# Frames held before the voice starts and after it ends, so no line begins on a cut.
LEAD = 12
TAIL = 18

# A scene never runs shorter than this, however short its line is: the animation inside
# needs its own time (the counter has eight taps to get through).
MINIMUM = {
    "Intro": 85,
    "Count": 190,
    "Quit": 185,
    "Milestones": 140,
    "Themes": 150,
    "Privacy": 110,
    "Outro": 110,
}

ORDER = ["Intro", "Count", "Quit", "Milestones", "Themes", "Privacy", "Outro"]


def speak(text: str, voice: str, rate: int, out: pathlib.Path) -> np.ndarray:
    """One clause, as 48 kHz mono samples."""
    with tempfile.TemporaryDirectory() as tmp:
        aiff = pathlib.Path(tmp) / "clause.aiff"
        subprocess.run(
            ["say", "-v", voice, "-r", str(rate), "-o", str(aiff), text], check=True
        )
        subprocess.run(
            ["afconvert", "-f", "WAVE", "-d", "LEI16@48000", "-c", "1",
             str(aiff), str(out)],
            check=True,
        )
    return read_wav(out)


def trim(samples: np.ndarray, floor_db: float = -45.0, margin_ms: float = 18.0) -> np.ndarray:
    """
    Cut the synthesiser's own leading and trailing silence.

    Without this the written pauses would be added on top of whatever padding `say`
    happens to leave, and the phrasing would drift clause by clause.
    """
    threshold = 10 ** (floor_db / 20)
    loud = np.where(np.abs(samples) > threshold)[0]
    if len(loud) == 0:
        return samples
    margin = int(margin_ms / 1000 * SR)
    start = max(0, loud[0] - margin)
    end = min(len(samples), loud[-1] + margin)
    return samples[start:end]


def duration_of(path: pathlib.Path) -> float:
    with wave.open(str(path)) as f:
        return f.getnframes() / f.getframerate()


def build_line(clauses: list[dict], voice: str, rate: int, out: pathlib.Path) -> None:
    pieces: list[np.ndarray] = []
    with tempfile.TemporaryDirectory() as tmp:
        scratch = pathlib.Path(tmp) / "clause.wav"
        for clause in clauses:
            samples = speak(clause["text"], voice, clause.get("rate", rate), scratch)
            pieces.append(trim(samples))
            pause = clause.get("pause", 0) / 1000
            if pause:
                pieces.append(np.zeros(int(pause * SR)))
    write_wav(out, np.concatenate(pieces))


def main() -> None:
    # --keep leaves public/vo/ alone and only re-measures it, which is how a recorded
    # or bought voice track gets into the film: drop one WAV per scene in, run this.
    keep = "--keep" in sys.argv

    script = json.loads((HERE / "script.json").read_text())
    voice = os.environ.get("VOICE", script["voice"])
    rate = int(os.environ.get("RATE", script["rate"]))

    installed = subprocess.run(["say", "-v", "?"], capture_output=True, text=True).stdout
    if not keep and voice.lower() not in installed.lower():
        sys.exit(
            f"Voice {voice!r} is not installed. `say -v '?'` lists the ones that are.\n"
            "More natural voices can be added in System Settings > Accessibility >\n"
            "Spoken Content > System Voice > Manage Voices (look for Premium)."
        )

    vo_dir = ROOT / "public" / "vo"
    vo_dir.mkdir(parents=True, exist_ok=True)

    scenes = []
    start = 0
    for name in ORDER:
        clauses = script["lines"][name]
        wav = vo_dir / f"{name}.wav"
        if keep:
            if not wav.exists():
                sys.exit(f"--keep needs {wav.relative_to(ROOT)} to exist already.")
        else:
            build_line(clauses, voice, rate, wav)
            polish(wav)

        seconds = duration_of(wav)
        frames = max(MINIMUM[name], LEAD + round(seconds * FPS) + TAIL)
        scenes.append(
            {
                "name": name,
                "frames": frames,
                "start": start,
                "voStart": LEAD,
                "voFrames": round(seconds * FPS),
                "voSeconds": round(seconds, 3),
                "file": f"vo/{name}.wav",
                "line": " ".join(c["text"] for c in clauses),
            }
        )
        start += frames

    timeline = {
        "fps": FPS,
        "voice": "supplied" if keep else voice,
        "rate": rate,
        "totalFrames": start,
        "scenes": scenes,
    }
    (ROOT / "src" / "timeline.json").write_text(json.dumps(timeline, indent=2) + "\n")

    print(f"voice: {'supplied files' if keep else f'{voice} at {rate} wpm'}")
    for s in scenes:
        print(f"  {s['name']:<11} {s['voSeconds']:>5.2f}s voice -> {s['frames']:>3} frames")
    print(f"  total {start} frames ({start / FPS:.1f}s)")


if __name__ == "__main__":
    main()
