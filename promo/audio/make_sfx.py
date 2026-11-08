#!/usr/bin/env python3
"""
The interface sounds: one per thing the finger does.

Short, soft and tuned to the music's key, so they read as part of the score rather than
as beeps on top of it. The app itself plays a click and a haptic on a tap (`Feedback.kt`);
these stand in for that on screen.

    python3 audio/make_sfx.py      ->  public/sfx/*.wav
"""
import pathlib
import wave

import numpy as np

SR = 48_000
ROOT = pathlib.Path(__file__).resolve().parent.parent


def write(name: str, mono: np.ndarray) -> None:
    out = ROOT / "public" / "sfx"
    out.mkdir(parents=True, exist_ok=True)
    mono = np.clip(mono, -1, 1)
    data = (mono * 32767).astype("<i2")
    path = out / f"{name}.wav"
    with wave.open(str(path), "wb") as f:
        f.setnchannels(1)
        f.setsampwidth(2)
        f.setframerate(SR)
        f.writeframes(data.tobytes())
    print(f"  {path.relative_to(ROOT)}  {len(mono) / SR * 1000:.0f} ms")


def t(seconds: float) -> np.ndarray:
    return np.arange(int(seconds * SR)) / SR


def tap() -> np.ndarray:
    """A tap on the dial: a wooden click with a short pitched body (E5)."""
    time = t(0.16)
    body = np.sin(2 * np.pi * 659.25 * time) * np.exp(-time * 26)
    click = np.random.default_rng(3).standard_normal(len(time))
    click = np.diff(click, prepend=0.0) * np.exp(-time * 420)
    return (body * 0.5 + click * 0.22) * 0.55


def success() -> np.ndarray:
    """Target reached: a two-note lift, C6 into E6, inside the key."""
    out = np.zeros(int(0.9 * SR))
    for offset, freq in ((0.0, 1046.5), (0.085, 1318.5)):
        time = t(0.8)
        tone = np.sin(2 * np.pi * freq * time) + 0.3 * np.sin(2 * np.pi * freq * 2 * time)
        tone *= np.exp(-time * 5.5)
        start = int(offset * SR)
        out[start : start + len(tone)] += tone[: len(out) - start] * 0.33
    return out


def press() -> np.ndarray:
    """A button taking a press: softer and lower than a tap on the dial."""
    time = t(0.22)
    body = np.sin(2 * np.pi * 392.0 * time) * np.exp(-time * 18)
    return body * 0.38


def whoosh() -> np.ndarray:
    """Scene change: filtered noise swelling and falling away."""
    time = t(0.55)
    noise = np.random.default_rng(11).standard_normal(len(time))
    kernel = np.ones(60) / 60
    noise = np.convolve(noise, kernel, mode="same")
    shape = np.exp(-((time - 0.18) ** 2) / 0.012)
    return noise * shape * 0.5


if __name__ == "__main__":
    write("tap", tap())
    write("success", success())
    write("press", press())
    write("whoosh", whoosh())
