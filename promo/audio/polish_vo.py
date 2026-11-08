#!/usr/bin/env python3
"""
The vocal chain: what makes a raw text-to-speech file sound recorded.

Synthesised speech arrives dry, flat and anechoic, and that combination — not the voice
itself — is most of what people hear as "robot". Four things move it a long way:

1. EQ, so it sits where a voice sits: rumble out, boxiness trimmed, presence lifted.
2. De-essing, because synthesised sibilance is harsher than a real mouth's.
3. Compression, so quiet syllables keep up with loud ones the way a mic'd voice does.
4. A small room and a whisper of room tone. Nothing in nature is anechoic, and the
   absence of any space is the strongest clue that a voice was not recorded.

All of it is numpy; no plugins, no services.
"""
import pathlib
import wave

import numpy as np

SR = 48_000


def read_wav(path: pathlib.Path) -> np.ndarray:
    with wave.open(str(path)) as f:
        assert f.getframerate() == SR, f"{path} is not {SR} Hz"
        data = np.frombuffer(f.readframes(f.getnframes()), dtype="<i2")
        if f.getnchannels() == 2:
            data = data.reshape(-1, 2).mean(axis=1)
    return data.astype(np.float64) / 32768.0


def write_wav(path: pathlib.Path, mono: np.ndarray) -> None:
    data = (np.clip(mono, -1, 1) * 32767).astype("<i2")
    with wave.open(str(path), "wb") as f:
        f.setnchannels(1)
        f.setsampwidth(2)
        f.setframerate(SR)
        f.writeframes(data.tobytes())


def eq(x: np.ndarray) -> np.ndarray:
    """
    Shape the voice in the frequency domain.

    FFT filtering rather than biquads: the clips are a few seconds long, so the whole
    thing fits in memory and the curve is exactly what is written here, with no filter
    design to get wrong.
    """
    n = 1 << int(np.ceil(np.log2(len(x) + 1)))
    spectrum = np.fft.rfft(x, n)
    freq = np.fft.rfftfreq(n, 1 / SR)

    gain = np.ones_like(freq)
    # Rumble and plosive energy: nothing useful lives below 85 Hz.
    gain *= 1 / (1 + (85 / np.maximum(freq, 1)) ** 4)
    # -2.5 dB around 300 Hz takes the cardboard out without thinning the voice.
    gain *= 1 - 0.25 * np.exp(-(((freq - 300) / 180) ** 2))
    # +3 dB presence: consonants, and the difference between clear and muffled.
    gain *= 1 + 0.42 * np.exp(-(((freq - 3200) / 1800) ** 2))
    # A little air on top, where synthetic voices are usually dull.
    gain *= 1 + 0.22 * np.exp(-(((freq - 9000) / 3500) ** 2))

    return np.fft.irfft(spectrum * gain, n)[: len(x)]


def _envelope(x: np.ndarray, attack: float, release: float) -> np.ndarray:
    """A one-pole follower — the detector every compressor is built on."""
    a = np.exp(-1 / (attack * SR))
    r = np.exp(-1 / (release * SR))
    out = np.empty_like(x)
    level = 0.0
    for i, sample in enumerate(np.abs(x)):
        coeff = a if sample > level else r
        level = coeff * level + (1 - coeff) * sample
        out[i] = level
    return out


def deess(x: np.ndarray) -> np.ndarray:
    """Duck 5-9 kHz only while an 's' is actually happening."""
    n = 1 << int(np.ceil(np.log2(len(x) + 1)))
    spectrum = np.fft.rfft(x, n)
    freq = np.fft.rfftfreq(n, 1 / SR)
    band = ((freq > 5000) & (freq < 9500)).astype(float)
    sibilance = np.fft.irfft(spectrum * band, n)[: len(x)]

    level = _envelope(sibilance, 0.001, 0.05)
    threshold = np.percentile(level, 97) * 0.6 + 1e-9
    # Up to 6 dB off the band, in proportion to how far over it goes.
    reduction = np.clip(1 - 0.5 * np.maximum(level / threshold - 1, 0), 0.5, 1.0)
    return x - sibilance * (1 - reduction)


def compress(
    x: np.ndarray, threshold_db: float = -24.0, ratio: float = 3.0
) -> np.ndarray:
    """Soft-knee compression, then makeup — evens the read out."""
    level = _envelope(x, 0.005, 0.12)
    level_db = 20 * np.log10(level + 1e-9)
    over = np.maximum(level_db - threshold_db, 0)
    gain_db = -over * (1 - 1 / ratio)
    out = x * 10 ** (gain_db / 20)
    peak = np.max(np.abs(out))
    return out / peak * 0.89 if peak > 0 else out


def room(x: np.ndarray, wet: float = 0.085) -> np.ndarray:
    """
    A small, dry room: early reflections and a short tail.

    Kept quiet on purpose. The goal is a voice that was recorded somewhere, not a voice
    in a hall — any more than this and it stops sounding like narration.
    """
    rng = np.random.default_rng(5)
    length = int(0.26 * SR)
    t = np.arange(length) / SR
    ir = rng.standard_normal(length) * np.exp(-t / 0.075)
    # A few discrete early reflections, which is what makes a space read as small.
    for delay_ms, amp in ((11, 0.5), (17, 0.38), (23, 0.3), (31, 0.22)):
        ir[int(delay_ms / 1000 * SR)] += amp
    ir[: int(0.004 * SR)] = 0
    ir /= np.abs(ir).sum()

    n = 1 << int(np.ceil(np.log2(len(x) + len(ir))))
    tail = np.fft.irfft(np.fft.rfft(x, n) * np.fft.rfft(ir, n), n)[: len(x)]
    return x * (1 - wet) + tail * wet * 6.0


def room_tone(n: int) -> np.ndarray:
    """A breath of filtered noise under everything, at about -64 dBFS."""
    rng = np.random.default_rng(23)
    noise = rng.standard_normal(n)
    kernel = np.ones(180) / 180  # heavily low-passed: air, not hiss
    return np.convolve(noise, kernel, mode="same") * 0.0006


def match_loudness(x: np.ndarray, target_db: float = -20.0) -> np.ndarray:
    """
    Level every line by the loudness of its *speech*, not its peak.

    A line with long pauses has the same peak as a dense one but far less energy, so
    peak normalising leaves some scenes sounding further away than others.
    """
    speech = x[np.abs(x) > 10 ** (-45 / 20)]
    if len(speech) == 0:
        return x
    rms = np.sqrt(np.mean(speech**2))
    gain = 10 ** (target_db / 20) / rms
    peak = np.max(np.abs(x)) * gain
    if peak > 0.92:
        gain *= 0.92 / peak
    return x * gain


def polish(path: pathlib.Path) -> None:
    x = read_wav(path)
    x = eq(x)
    x = deess(x)
    x = compress(x)
    x = room(x)
    x = x + room_tone(len(x))
    x = match_loudness(x)
    # Short fades so a clip never starts or stops on a sample edge.
    edge = int(0.008 * SR)
    x[:edge] *= np.linspace(0, 1, edge)
    x[-edge:] *= np.linspace(1, 0, edge)
    write_wav(path, x)


if __name__ == "__main__":
    import sys

    targets = [pathlib.Path(a) for a in sys.argv[1:]]
    if not targets:
        targets = sorted((pathlib.Path(__file__).parent.parent / "public" / "vo").glob("*.wav"))
    for target in targets:
        polish(target)
        print(f"  polished {target.name}")
