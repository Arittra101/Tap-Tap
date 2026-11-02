# Tap Tap — Requirements

Version 1 · 2 November 2026 · Owner: Arittra Das

## 1. Overview

Tap Tap is an offline Android counter app with two kinds of counter: **Normal** (count anything) and **Quit** (stay away from a bad habit, day by day). Version 1 ships on Google Play.

**Goals**

- Counting must be fast: one big tap target, usable without looking.
- Quitting must feel supported: days free, money saved, badges and a personal reason keep the user going.
- Home shows the most important thing first: one Focus quit counter, then normal counters, then other quit counters.
- Works for everyone: 6 languages, 4 color themes, light and dark mode, no religious content.

**Users**

- People who count daily things: exercise reps, glasses of water, pages read, cups of tea.
- People who want to quit or reduce a habit: smoking, alcohol, soft drinks, phone use at night.

**Platform**

- Android phones, built with Kotlin and Jetpack Compose (Material 3).
- Fully offline. No account, no login, no server in v1.
- Design source: the Tap Tap design canvas (screens) and the Tap Tap design system (tokens, components, rules).

## 2. Counter types

Every counter is either Normal or Quit. The user picks the type first, and the type decides which fields and actions exist.

| | Normal counter | Quit counter |
| --- | --- | --- |
| Purpose | Count anything | Stay away from a bad habit |
| Main number | Count (changed by taps) | Days free (counted by itself from the quit date) |
| Main action | Tap the dial: + step, or − step in reverse | "I beat an urge" logs one urge beaten |
| Fields | Name, notes, start value, step value, target, keep counting after target | Habit name, quit date, money per day, goal in days, reason |
| Motivation | Progress to target | Milestone badges, money saved, reason, daily message |
| Reset | Reset to start value (asks first) | "I slipped": days start again from now (asks first) |
| Can be Focus | No | Yes (one at a time) |
| Can be pinned | Yes, first in "Counters" | Yes, first in "Quit tracker" |
| Home placement | Horizontal row "Counters" | Focus card, or vertical list "Quit tracker" |

**Milestone days:** 1, 3, 7, 14, 21, 30, 60, 90, 180, 365.

## 3. Screens and functional requirements

The app has 6 screens: Home, New counter, Normal counter, Quit counter, Edit counter and Settings.

### 3.1 Home

| ID | Requirement |
| --- | --- |
| FR-1.1 | Top bar shows the app mark, "Tap Tap" and a Settings button. A thin divider separates it from the page. |
| FR-1.2 | If a Focus quit counter exists, show the Focus card first: name, days free, progress to the next badge, money saved, urges beaten today, the reason, and an "I beat an urge" button. |
| FR-1.3 | If quit counters exist but none is Focus, show a "Pick one habit to focus on" card with one chip per quit counter. Tapping a chip makes it the Focus. |
| FR-1.4 | If there are no quit counters, hide the Focus area. |
| FR-1.5 | "Counters" shows all normal counters in a horizontal scroll row. Pinned counters come first. The row ends with a "New counter" card. |
| FR-1.6 | Each normal counter card shows name, count, target (or "no target"), a progress bar, a pin button and a + button that adds the step value. At target with "keep counting" off, + becomes a check. |
| FR-1.7 | "Quit tracker" shows all quit counters except the Focus in a vertical list. Pinned counters come first. |
| FR-1.8 | Each quit card shows name, color dot, days free, money saved (or urges beaten today if no money is set), progress to the next badge, a make-focus button and a pin button. |
| FR-1.9 | A floating "New counter" button opens New counter. |
| FR-1.10 | First open with no counters: show a welcome, two big choices (Normal counter, Quit counter) and idea chips (Push-ups, Glasses of water, Pages read, Quit smoking, Less phone at night). Hide the floating button. |
| FR-1.11 | Tapping a card name opens that counter's screen. |

### 3.2 New counter

| ID | Requirement |
| --- | --- |
| FR-2.1 | The first field is the type: Normal counter or Quit counter. The form below changes with the type. |
| FR-2.2 | Normal: name (required), start value (default 0, min 0), step value (default 1, min 1), target (optional switch + value), keep counting after target (switch, default off), notes (optional), pin to top. |
| FR-2.3 | Quit: habit chips (Cigarettes, Alcohol, Soft drinks, Phone at night, Other) that fill the name; habit name (required); quit date (Today or an earlier date); money spent per day (optional, local currency); goal in days (optional, default 30); reason (optional). |
| FR-2.4 | Quit motivation options: milestone badges (default on), daily motivation message (default on), make this my focus (default off), pin to top (default off). |
| FR-2.5 | The create button stays disabled until the name has text. |
| FR-2.6 | Making a new counter the Focus removes Focus from the old one. |
| FR-2.7 | After create, go to Home and show the new counter in its group. |

### 3.3 Normal counter

| ID | Requirement |
| --- | --- |
| FR-3.1 | Top bar: back, counter name with its note, edit button. |
| FR-3.2 | Stat strip: today's change, left to target, percent done. |
| FR-3.3 | The whole dial is one tap button. Each tap adds the step value; the ring shows count ÷ target. |
| FR-3.4 | Lock: taps do nothing, the dial turns gray, the label says "Locked". |
| FR-3.5 | Reverse: each tap subtracts the step value. The count never goes below 0. |
| FR-3.6 | Reset asks first, then sets the count to the start value. |
| FR-3.7 | At target with "keep counting" off, taps stop and the dial shows "Target reached". With it on, counting goes on. |
| FR-3.8 | Each tap can vibrate and play a click (see Settings). |
| FR-3.9 | The background image and counter color from Edit apply here. A scrim keeps the number readable on an image. |

### 3.4 Quit counter

| ID | Requirement |
| --- | --- |
| FR-4.1 | Top bar: back, habit name, "Free since <date>", pin button. |
| FR-4.2 | A ring shows days free and time (days + hours) since the quit date, filled toward the next milestone. |
| FR-4.3 | Stat strip: money saved (days free × money per day), total urges beaten, best streak. |
| FR-4.4 | "I beat an urge" adds one to today's and the total urge count. It does not change days free. |
| FR-4.5 | Show the user's reason in its own card. |
| FR-4.6 | Milestone badge row with earned, next and locked states. |
| FR-4.7 | "Make focus" / "Your focus" toggle. |
| FR-4.8 | "I slipped" asks first, then sets the quit date to now. Best streak, badges and urges are kept. |

### 3.5 Edit counter

| ID | Requirement |
| --- | --- |
| FR-5.1 | Edit name and notes. |
| FR-5.2 | Pick a background: none or one of the default images (v1: Waves, Orbs, Hills). |
| FR-5.3 | Pick a counter color: "Same as app" or Pink, Teal, Saffron, Blue. Light or dark still follows the app. |
| FR-5.4 | Edit start value and step value. A new step works from the next tap; a new start value is used on the next reset. The current count does not change. |
| FR-5.5 | Target switch, target value, and "keep counting after target". |
| FR-5.6 | Pin to top switch. |
| FR-5.7 | Save changes; delete counter (asks first). |
| FR-5.8 | For a quit counter, show the quit fields from FR-2.3 and FR-2.4 in place of start, step and target. |

### 3.6 Settings

| ID | Requirement |
| --- | --- |
| FR-6.1 | Color theme: Pink, Teal, Saffron, Blue (default Pink). |
| FR-6.2 | Mode: Light, Dark, System (default System). A note shows the result, for example "Phone is in dark mode, so the app uses Pink ~ Dark". |
| FR-6.3 | Language: English, Bangla, Hindi, Russian, Spanish, Italian, each shown in its own script. |
| FR-6.4 | Vibration switch (default on) and sound switch (default off) for every + and −. |
| FR-6.5 | Privacy policy link. |

## 4. Themes and appearance

| ID | Requirement |
| --- | --- |
| FR-7.1 | An app theme is one color × one mode: pink-light, pink-dark, teal-light, teal-dark, saffron-light, saffron-dark, blue-light, blue-dark. |
| FR-7.2 | "System" mode uses the phone's light or dark setting and updates when the phone setting changes. |
| FR-7.3 | A counter's own color changes only its accent colors (count, progress, buttons). The mode always follows the app. |
| FR-7.4 | All colors, fonts, spacing and corner sizes come from the design system tokens. No hard-coded colors. |
| FR-7.5 | Fonts: Outfit for numbers and titles; Noto Sans (with Noto Sans Bengali and Noto Sans Devanagari) for text. Numbers use tabular figures. |
| FR-7.6 | Default background images are bundled in the app (no download). |

## 5. Languages, feedback and privacy

| ID | Requirement |
| --- | --- |
| FR-8.1 | All text lives in string resources for en, bn, hi, ru, es, it. No hard-coded text. |
| FR-8.2 | Language can change inside the app without changing the phone language (per-app language). |
| FR-8.3 | Numbers and dates follow the chosen language (for example Bangla digits in Bangla). Money uses the local currency symbol. |
| FR-8.4 | Vibration: a short haptic (about 10 ms) per tap when on. A slightly longer haptic when a target or badge is reached. |
| FR-8.5 | Sound: a soft click per tap when on. It respects the phone's silent mode. |
| FR-8.6 | Daily motivation message: one local notification per quit counter per day (default 9:00 AM) with days free and the next badge. Needs the notification permission on Android 13+. |
| FR-8.7 | Privacy: all data stays on the phone. No account, no analytics with personal data, no ads in v1. A privacy policy page is linked from Settings and the Play Store listing. |

## 6. Data model

**Counter**

| Field | Type | Notes |
| --- | --- | --- |
| id | Long | Primary key |
| type | Enum | NORMAL, QUIT |
| name | String | Required |
| notes | String? | Normal only |
| color | Enum? | PINK, TEAL, SAFFRON, BLUE; null = same as app |
| background | Enum | NONE, WAVES, ORBS, HILLS |
| pinned | Boolean | First in its group |
| isFocus | Boolean | Quit only; at most one true |
| createdAt | Instant | |
| sortOrder | Int | Order inside the group after pins |

**Normal counter fields** (on Counter or a 1:1 table)

| Field | Type | Notes |
| --- | --- | --- |
| count | Long | Current value, ≥ 0 |
| startValue | Long | Default 0; reset goes here |
| stepValue | Int | Default 1, min 1 |
| target | Long? | Null = no target |
| keepCountingAfterTarget | Boolean | Default false |
| locked | Boolean | |
| reversed | Boolean | |

**Quit details** (1:1 with a QUIT counter)

| Field | Type | Notes |
| --- | --- | --- |
| quitAt | Instant | Days free = now − quitAt |
| moneyPerDay | Decimal? | Null = no money shown |
| goalDays | Int? | Optional |
| reason | String? | |
| bestStreakDays | Int | Updated on slip |
| badgesOn | Boolean | Default true |
| dailyMessageOn | Boolean | Default true |
| dailyMessageTime | LocalTime | Default 09:00 |

**Event log** (for "Today", urges and history)

| Field | Type | Notes |
| --- | --- | --- |
| id | Long | |
| counterId | Long | |
| kind | Enum | TAP, URGE_BEATEN, RESET, SLIP |
| delta | Int | +step / −step for TAP |
| at | Instant | |

**Settings** (DataStore)

| Key | Default |
| --- | --- |
| themeColor | PINK |
| themeMode | SYSTEM |
| language | Phone language if supported, else English |
| vibration | true |
| sound | false |

## 7. Non-functional requirements

| ID | Requirement |
| --- | --- |
| NFR-1 | Works fully offline. Data is stored with Room (counters, logs) and DataStore (settings). |
| NFR-2 | A tap updates the number in under 50 ms, and fast repeated taps are never lost. |
| NFR-3 | Data is saved at once, so a closed or killed app never loses a count. |
| NFR-4 | Touch targets are at least 48 dp. Every icon-only button has a content description. |
| NFR-5 | Text meets 4.5:1 contrast in all 8 themes (already checked in the design system). |
| NFR-6 | States never use color alone: target reached, locked and slips also show a word or icon. |
| NFR-7 | Supports font scaling up to 200% without cut text on the main screens. |
| NFR-8 | Min SDK 24 (Android 7.0); target the latest SDK that Google Play requires. |
| NFR-9 | App size under 15 MB. Backgrounds as compressed WebP or vector. |
| NFR-10 | Android Auto Backup is on, so counters move to a new phone. |

## 8. Out of scope for v1 and open questions

**Out of scope for v1**

- Accounts, cloud sync and sharing between users.
- History charts and a stats screen (the event log is stored now to make this easy later).
- Home screen widgets, Wear OS, iOS.
- Custom background images from the gallery.
- Reminders for normal counters.

**Open questions**

- What happens if the start value is higher than the target? Proposal: show a warning in Edit counter.
- Should "Today" reset at midnight or at a time the user picks?
- Final app name: keep "Tap Tap" or use "One More"? The Play Store title should add "Counter & Quit Habit".
- Should the daily motivation message also go out when the user slipped that day?