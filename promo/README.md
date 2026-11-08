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
npm install          # once
npm run render             # 1080x1920 -> out/habittap-promo-vertical.mp4
npm run render:landscape   # 1920x1080 -> out/habittap-promo-landscape.mp4
npm run studio             # live preview + scrubbing at localhost:3000
npm run still              # a poster frame
```

Remotion brings its own ffmpeg and headless Chrome, so nothing else needs installing.
A full render takes about a minute per cut on an M-series Mac.

| Output | Size | Where it is for |
| --- | --- | --- |
| `habittap-promo-vertical.mp4` | 1080x1920, 29s | Reels, Shorts, TikTok, store screenshots |
| `habittap-promo-landscape.mp4` | 1920x1080, 29s | YouTube — where a Play Store promo video has to be hosted |

## Scenes

`src/Promo.tsx` is the running order; each entry owns its own frame count.

| # | Scene | Frames | Shows |
| --- | --- | --- | --- |
| 1 | `Intro` | 80 | App mark, name, "Count anything. Quit anything." |
| 2 | `Count` | 170 | The counter dial: eight taps from 12 to the 20 target, ring closing |
| 3 | `Quit` | 170 | The quit screen: 18 days free, money saved, "I said no" |
| 4 | `Milestones` | 130 | The ten badges, 4 earned, the 21-day badge next |
| 5 | `Themes` | 130 | Four colours x light and dark, cycling on Home |
| 6 | `Privacy` | 100 | No account, no internet, no ads |
| 7 | `Outro` | 100 | Name, store title, the four claims |

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
- There is no narration or music; the file carries a silent audio track so social
  platforms accept it.
- Numerals are Latin. The app renders Bangla and Devanagari digits in those locales
  (`localNumber`), which this video does not try to show.
