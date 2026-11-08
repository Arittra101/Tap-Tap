# HabitTap promo video

The store and social video, generated from code rather than screen-recorded. Scenes are
React components rendered by [Remotion](https://remotion.dev) and encoded to MP4.

Why code: the video is built from the app's **own design tokens and copy** — every colour
in `src/theme.ts` comes from `app/src/main/java/com/habitTap/ui/theme/Color.kt`, the type
scale from `Type.kt`, the badge days from `TimeUtils.MILESTONES`, and the strings from
`app/src/main/res/values/strings.xml`. The two typefaces in `public/` are copies of the
ones the app bundles. Change a token in the app and the video can follow it, which a
screen recording cannot.

## Render

```bash
npm install                # once
python3 -m pip install --user numpy   # once, for the score

npm run audio              # voice + music + interface sounds -> public/
npm run render             # 1080x1920 -> out/habittap-promo-vertical.mp4
npm run render:landscape   # 1920x1080 -> out/habittap-promo-landscape.mp4
npm run render:all         # both
npm run studio             # live preview + scrubbing at localhost:3000
npm run still              # a poster frame
npm run feature            # 1024x500 -> out/feature-graphic.png (Play Console)
```

Remotion brings its own ffmpeg and headless Chrome, so nothing else needs installing.
`npm run audio` only has to be re-run when the script, the voice or the score changes —
it rewrites `src/timeline.json`, which is what sets the scene lengths. Each render ends
with a mastering pass (`audio/master.py`) that brings the file to -14 LUFS.

A full render takes about two minutes per cut on an M-series Mac.

| Output | Size | Where it is for |
| --- | --- | --- |
| `habittap-promo-vertical.mp4` | 1080x1920, 45s | Reels, Shorts, TikTok, store screenshots |
| `habittap-promo-landscape.mp4` | 1920x1080, 45s | YouTube — where a Play Store promo video has to be hosted |
| `feature-graphic.png` | 1024x500 | Play Console -> Store listing -> Feature graphic |

Both videos carry narration, music and interface sounds, mastered to -14 LUFS.

The feature graphic (`src/FeatureGraphic.tsx`) is laid out around two Play constraints:
the centre column stays empty because Play draws a play button there once a promo video
is attached, and nothing that has to be read comes within 64px of an edge, because some
surfaces crop it. It renders in a couple of seconds and needs no audio pass.

## Scenes

`src/Promo.tsx` is the running order. Lengths come from `src/timeline.json`, which
`audio/build_vo.py` writes from the measured voice track.

| # | Scene | Frames | Shows |
| --- | --- | --- | --- |
| 1 | `Intro` | 122 | App mark, name, "Count anything. Quit anything." |
| 2 | `Count` | 249 | The counter dial: eight taps from 12 to the 20 target, ring closing |
| 3 | `Quit` | 229 | The quit screen: 18 days free, money saved, "I said no" |
| 4 | `Milestones` | 220 | The ten badges, 4 earned, the 21-day badge next |
| 5 | `Themes` | 150 | Four colours x light and dark, cycling on Home |
| 6 | `Privacy` | 177 | No account, no internet, no ads |
| 7 | `Outro` | 191 | Name, store title, the four claims |

## Sound

Everything you hear is generated on this machine; no track is licensed and no service is
called.

| Part | Made by | Lives in |
| --- | --- | --- |
| Narration | macOS `say`, voice **Samantha** at 162 wpm, clause by clause | `audio/script.json` -> `public/vo/*.wav` |
| Vocal chain | EQ, de-esser, compressor, small room, room tone, loudness match | `audio/polish_vo.py` |
| Score | additive synthesis in numpy, 84 BPM, Am7-Fmaj7-Cmaj7-G6 | `audio/make_music.py` -> `public/music/bed.wav` |
| Interface sounds | synthesised blips tuned to the key | `audio/make_sfx.py` -> `public/sfx/*.wav` |
| Balance | ducking under each line, in code | `musicVolume` in `src/timing.ts` |
| Loudness | two-pass linear `loudnorm`, -14 LUFS / -1.5 dBTP | `audio/master.py` |

The score is arranged to the film: `ARRANGEMENT` in `make_music.py` sets how loud each
layer (pad, arpeggio, bass, percussion, bells) sits in each scene, and the privacy scene
deliberately drops to almost nothing so that line lands quietly.

**Phrasing is written, not guessed.** Each scene's line in `script.json` is a list of
clauses with the pause that follows it, in milliseconds. Every clause is spoken on its
own, trimmed to its first and last sound, and reassembled with exactly that gap — one
long utterance is what makes text-to-speech sound like a machine reading, and breath
between clauses is what a person does. `polish_vo.py` then puts the result through a
vocal chain, because a dry, anechoic, uncompressed voice reads as synthetic however good
the synthesiser is.

**The edit follows the voice.** `audio/build_vo.py` speaks each line, measures it, and
writes the scene lengths into `src/timeline.json`. Change a line and the scene that holds
it gets longer or shorter by itself.

### Changing the voice

`out/voice-samples/` holds the same sentence in every installed female voice — play them
and pick. Then:

```bash
VOICE="Karen" RATE=160 npm run vo && npm run music && npm run render:all
```

`say -v '?'` lists what is installed. The built-in voices are system text-to-speech and
sound like it; two ways up from there, in order of effort:

1. **Download a Premium voice** — System Settings > Accessibility > Spoken Content >
   System Voice > Manage Voices. "Ava (Premium)" and "Zoe (Premium)" are a clear step up,
   and `build_vo.py` will use one as soon as it is installed.
2. **Record or buy a real voice.** Drop one WAV per scene into `public/vo/` named
   `Intro.wav`, `Count.wav` and so on, then run `python3 audio/build_vo.py --keep` — or
   just edit the `voSeconds` / `frames` numbers in `src/timeline.json` to match the new
   files. Nothing else needs to change.

## Changing it

- **Copy** lives in the scene that says it. Keep it to the voice rules in the design
  system README: short sentences, sentence case, verbs on buttons, no emoji.
- **Colours, type, spacing, radii, milestones**: `src/theme.ts` only. Nothing else in
  this project may hard-code a colour, the same rule as FR-7.4 in the app.
- **Components** in `src/components/ui.tsx` mirror the Compose components one for one and
  name their counterpart in a comment. Match the app when you change one.
- **Length and shape**: frame counts in each scene file; the two compositions are in
  `src/Root.tsx`.
- **Money** shows as taka (`৳2,700`) in `Quit.tsx` and `Themes.tsx`. The app formats money
  from the device locale (`Formatters.money`), so change those two strings if the video is
  aimed at another market.

## Known limits

- The device shell, the 9:41 status bar and the stage background are promo furniture, not
  app UI. Everything inside the screen is the app.
- The narration is macOS system text-to-speech. It is clear and correctly paced, but it
  is not a human read; see "Changing the voice" above.
- Numerals are Latin. The app renders Bangla and Devanagari digits in those locales
  (`localNumber`), which this video does not try to show.
