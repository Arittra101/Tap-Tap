# Play Store phone screenshots

8 PNGs, 1080 x 1920 (9:16), ~0.3 MB each — within Play's limits (PNG/JPEG, 16:9 or 9:16,
each side 320–3840 px, max 8 MB).

Upload in this order; the order tells the story.

| # | File | Screen | App theme | Headline |
| --- | --- | --- | --- | --- |
| 1 | `01_home.png` | Home — motivation slider, Your focus, Counters | Pink · Light | Count anything. Quit anything. |
| 2 | `02_quit.png` | Quit counter — Quit cigarettes | Pink · Light | Watch the days add up |
| 3 | `03_counter.png` | Normal counter — Push-ups | Teal · Light | One big tap to count |
| 4 | `04_milestones.png` | Quit counter — Quit soft drinks, milestones | Saffron · Dark | A badge for every milestone |
| 5 | `05_quitlist.png` | Home — Counters + Quit tracker | Teal · Dark | All your habits in one place |
| 6 | `06_create.png` | New counter — quit flow | Saffron · Light | Set one up in seconds |
| 7 | `07_edit.png` | Edit counter — background + color | Blue · Light | Make every counter yours |
| 8 | `08_settings.png` | Settings — theme, mode, language | Blue · Dark | Light, dark and six languages |

All four colour themes (pink, teal, saffron, blue) and both modes appear across the set.

## How they were made

The phone content is a **real screenshot of the app**, not a mock-up: the debug build was
installed on a Pixel 8a emulator (1080 x 2400, 420 dpi), the Room database seeded with the
same counters the design canvas uses (`HomePreviewData`), and each screen captured with
`adb exec-out screencap`. The status bar is SystemUI demo mode (9:41, full battery, wifi).

The frames around them use the app's own `outfit_variable.ttf` and the exact colour tokens
from `ui/theme/Color.kt`, so the marketing layer matches the product.

Nothing in the copy is a claim the app does not make: "no account, no ads, fully offline"
matches `PRIVACY.md`, and the app requests only `POST_NOTIFICATIONS` and `VIBRATE` — there
is no `INTERNET` permission.
