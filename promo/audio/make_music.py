#!/usr/bin/env python3
"""
The score, synthesised from scratch.

Written rather than licensed: the bed is additive synthesis in numpy, so it is ours, it
costs nothing, and it bends to the edit — the arrangement reads `src/timeline.json` and
changes with the scenes. Warm and unhurried under the voice, fuller where the video
opens up, and almost bare under the privacy line so that one lands quietly.

    python3 audio/make_music.py      ->  public/music/bed.wav
"""
import json
import pathlib

import numpy as np

HERE = pathlib.Path(__file__).resolve().parent
ROOT = HERE.parent
SR = 48_000
BPM = 84.0
BEAT = 60.0 / BPM
BAR = 4 * BEAT

# C major / A minor, the warm end of it. One chord per bar, looping.
PROGRESSION = [
    {"pad": [57, 60, 64, 67], "bass": 45},  # Am7
    {"pad": [53, 57, 60, 64], "bass": 41},  # Fmaj7
    {"pad": [52, 55, 60, 64], "bass": 48},  # Cmaj7
    {"pad": [55, 59, 62, 64], "bass": 43},  # G6
]

# How loud each layer sits in each scene, 0..1. This is the arrangement.
ARRANGEMENT = {
    "Intro":      {"pad": 0.85, "arp": 0.45, "bass": 0.50, "perc": 0.00, "bell": 0.35},
    "Count":      {"pad": 0.80, "arp": 0.85, "bass": 0.80, "perc": 0.70, "bell": 0.00},
    "Quit":       {"pad": 0.90, "arp": 0.55, "bass": 0.80, "perc": 0.45, "bell": 0.20},
    "Milestones": {"pad": 0.90, "arp": 0.90, "bass": 0.85, "perc": 0.75, "bell": 0.55},
    "Themes":     {"pad": 1.00, "arp": 1.00, "bass": 0.90, "perc": 0.90, "bell": 0.70},
    # The quietest moment in the film, on purpose.
    "Privacy":    {"pad": 0.75, "arp": 0.20, "bass": 0.45, "perc": 0.00, "bell": 0.00},
    "Outro":      {"pad": 1.00, "arp": 0.70, "bass": 0.90, "perc": 0.55, "bell": 0.85},
}

LAYERS = ["pad", "arp", "bass", "perc", "bell"]


def midi(note: int) -> float:
    return 440.0 * 2 ** ((note - 69) / 12)


def place(buf: np.ndarray, start: float, sound: np.ndarray) -> None:
    """Mix `sound` into `buf` at `start` seconds, clipped to the buffer."""
    i = int(start * SR)
    if i >= len(buf):
        return
    n = min(len(sound), len(buf) - i)
    buf[i : i + n] += sound[:n]


def env(n: int, attack: float, release: float) -> np.ndarray:
    """A plain attack / sustain / release shape, in samples."""
    a = max(1, int(attack * SR))
    r = max(1, int(release * SR))
    a = min(a, n)
    r = min(r, n - a) if n > a else 0
    out = np.ones(n)
    out[:a] = np.linspace(0, 1, a) ** 1.5
    if r:
        out[n - r :] = np.linspace(1, 0, r) ** 1.5
    return out


def pad_voice(freq: float, dur: float) -> np.ndarray:
    """Four harmonics, two slightly detuned copies: a soft, wide sustain."""
    t = np.arange(int(dur * SR)) / SR
    out = np.zeros_like(t)
    for detune in (-0.0025, 0.0025):
        f = freq * (1 + detune)
        for harmonic, amp in ((1, 1.0), (2, 0.38), (3, 0.16), (4, 0.07)):
            out += amp * np.sin(2 * np.pi * f * harmonic * t)
    # A slow breath, so a held chord never sounds frozen.
    out *= 1 + 0.05 * np.sin(2 * np.pi * 0.17 * t)
    return out * env(len(t), 0.9, 1.1) * 0.05


def pluck(freq: float, dur: float = 0.9) -> np.ndarray:
    """A marimba-ish blip: two harmonics under a fast exponential decay."""
    t = np.arange(int(dur * SR)) / SR
    decay = np.exp(-t * 7.5)
    tone = np.sin(2 * np.pi * freq * t) + 0.3 * np.sin(2 * np.pi * freq * 2 * t)
    return tone * decay * 0.12


def bass_note(freq: float, dur: float) -> np.ndarray:
    t = np.arange(int(dur * SR)) / SR
    tone = np.sin(2 * np.pi * freq * t) + 0.25 * np.sin(2 * np.pi * freq * 2 * t)
    return tone * env(len(t), 0.08, 0.35) * np.exp(-t * 0.5) * 0.16


def bell(freq: float, dur: float = 2.2) -> np.ndarray:
    """High sparkle, two octaves up, for the moments that open out."""
    t = np.arange(int(dur * SR)) / SR
    tone = np.sin(2 * np.pi * freq * t) + 0.5 * np.sin(2 * np.pi * freq * 2.76 * t)
    return tone * np.exp(-t * 2.2) * 0.05


def kick() -> np.ndarray:
    t = np.arange(int(0.26 * SR)) / SR
    sweep = 92 * np.exp(-t * 26) + 44
    phase = 2 * np.pi * np.cumsum(sweep) / SR
    return np.sin(phase) * np.exp(-t * 11) * 0.35


def shaker(rng: np.random.Generator, amp: float) -> np.ndarray:
    n = int(0.06 * SR)
    noise = rng.standard_normal(n)
    # Differencing the noise is a cheap high-pass: keeps the hiss, drops the rumble.
    noise = np.diff(noise, prepend=0.0)
    t = np.arange(n) / SR
    return noise * np.exp(-t * 55) * amp * 0.05


def reverb_ir(seconds: float, decay: float, rng: np.random.Generator) -> np.ndarray:
    """Exponentially decaying noise — a plain, well-behaved hall."""
    n = int(seconds * SR)
    t = np.arange(n) / SR
    ir = rng.standard_normal(n) * np.exp(-t / decay)
    # Roll the top off so the tail is soft rather than hissy.
    kernel = np.ones(24) / 24
    ir = np.convolve(ir, kernel, mode="same")
    ir[: int(0.006 * SR)] = 0  # pre-delay
    return ir / np.abs(ir).sum() * 1.6


def convolve_fft(signal: np.ndarray, ir: np.ndarray) -> np.ndarray:
    n = 1 << int(np.ceil(np.log2(len(signal) + len(ir))))
    out = np.fft.irfft(np.fft.rfft(signal, n) * np.fft.rfft(ir, n), n)
    return out[: len(signal)]


def automation(timeline: dict, layer: str, length: int) -> np.ndarray:
    """A gain curve for one layer, stepped per scene then smoothed into a glide."""
    fps = timeline["fps"]
    curve = np.zeros(length)
    for scene in timeline["scenes"]:
        start = int(scene["start"] / fps * SR)
        end = min(length, int((scene["start"] + scene["frames"]) / fps * SR))
        curve[start:end] = ARRANGEMENT[scene["name"]][layer]
    curve[end:] = curve[end - 1] if end else 0
    # A half-second moving average: no layer ever snaps in or out on a cut.
    window = int(0.5 * SR)
    kernel = np.ones(window) / window
    return np.convolve(curve, kernel, mode="same")


def main() -> None:
    timeline = json.loads((ROOT / "src" / "timeline.json").read_text())
    fps = timeline["fps"]
    # A second of tail so the last chord can ring out past the final frame.
    total = timeline["totalFrames"] / fps + 1.5
    n = int(total * SR)
    rng = np.random.default_rng(7)

    tracks = {name: np.zeros(n) for name in LAYERS}

    bar = 0
    while bar * BAR < total:
        chord = PROGRESSION[bar % len(PROGRESSION)]
        t0 = bar * BAR

        for note in chord["pad"]:
            place(tracks["pad"], t0, pad_voice(midi(note), BAR + 0.6))

        place(tracks["bass"], t0, bass_note(midi(chord["bass"]), BAR))

        # Eighth notes, walking up the chord an octave above the pad.
        tones = [midi(note + 12) for note in chord["pad"]]
        pattern = [0, 2, 1, 3, 2, 0, 3, 1]
        for step, index in enumerate(pattern):
            place(tracks["arp"], t0 + step * BEAT / 2, pluck(tones[index]))

        for beat in (0, 2):
            place(tracks["perc"], t0 + beat * BEAT, kick())
        for step in range(8):
            amp = 1.0 if step % 2 else 0.55
            place(tracks["perc"], t0 + step * BEAT / 2, shaker(rng, amp))

        # One bell per two bars, on the downbeat, high above everything.
        if bar % 2 == 0:
            place(tracks["bell"], t0, bell(midi(chord["pad"][-1] + 24)))

        bar += 1

    for name in LAYERS:
        tracks[name] *= automation(timeline, name, n)

    # Wet layers go to the hall; the kick and bass stay dry so they keep their edge.
    wet_ir_l = reverb_ir(1.3, 0.42, rng)
    wet_ir_r = reverb_ir(1.3, 0.45, rng)
    wet_source = tracks["pad"] + tracks["arp"] + tracks["bell"]
    dry = tracks["pad"] + tracks["arp"] + tracks["bell"] + tracks["bass"] + tracks["perc"]

    left = dry + 0.42 * convolve_fft(wet_source, wet_ir_l)
    right = dry + 0.42 * convolve_fft(wet_source, wet_ir_r)

    # A few milliseconds of offset widens the image without hollowing the centre.
    widen = int(0.004 * SR)
    right = np.concatenate([np.zeros(widen), right[:-widen]])

    stereo = np.stack([left, right], axis=1)
    stereo = np.tanh(stereo * 1.1)  # gentle glue, no hard clipping
    stereo *= 0.72 / np.max(np.abs(stereo))

    # In and out, so the bed never starts or ends on a hard edge.
    fade_in = int(1.2 * SR)
    fade_out = int(2.0 * SR)
    stereo[:fade_in] *= np.linspace(0, 1, fade_in)[:, None] ** 1.5
    stereo[-fade_out:] *= np.linspace(1, 0, fade_out)[:, None] ** 1.5

    out = ROOT / "public" / "music"
    out.mkdir(parents=True, exist_ok=True)
    path = out / "bed.wav"
    data = (stereo * 32767).astype("<i2")
    import wave

    with wave.open(str(path), "wb") as f:
        f.setnchannels(2)
        f.setsampwidth(2)
        f.setframerate(SR)
        f.writeframes(data.tobytes())

    print(f"{path.relative_to(ROOT)}  {total:.1f}s  {path.stat().st_size / 1e6:.1f} MB")


if __name__ == "__main__":
    main()
