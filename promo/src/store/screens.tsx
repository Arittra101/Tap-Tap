/**
 * The app's screens, as a Play Store screenshot shows them: one still frame, 390 x 844.
 *
 * Every screen below follows its Compose file's layout order exactly — Home's quote
 * slider above the focus card, the counter's stat strip *above* the dial and the control
 * bar below it, the quit screen's ring then badge line then strip. The sample data is the
 * app's own preview data (`HomePreviewData`, `CounterScreen.sample`, `QuitScreen.sample`),
 * with two deliberate departures, both noted where they are made.
 *
 * `scrollTop` moves the scrolling region only, exactly as scrolling the app does: the top
 * bar stays put. That is how one screen gives two screenshots.
 */
import React from 'react';
import {colors, radius, space, swatch, ThemeColor, type} from '../theme';
import * as Icon from '../components/Icon';
import {
  ControlBar,
  CounterCard,
  Dial,
  DaysRing,
  FocusCard,
  MilestoneBadge,
  QuitCard,
  Screen,
  StatStrip,
  StatusPill,
  useTheme,
} from '../components/ui';
import {
  BigChoiceRow,
  Button,
  Chip,
  ColorSwatch,
  HomeTopBar,
  NewCounterCard,
  QuoteSlider,
  RadioDot,
  RowHeading,
  Section,
  SettingDivider,
  SettingGroup,
  SettingRow,
  SegmentedControl,
  Switch,
  TopBar,
  WelcomeMark,
  FramedIconButton,
} from './parts';

// --- The data ----------------------------------------------------------------------------

/**
 * `HomePreviewData` sets the focus counter's spend at 150 a day, which is right for the
 * taka the design canvas was drawn in and wrong for the dollars an English listing shows.
 * The screenshots use 12 a day instead and carry the arithmetic through: 18 days x $12 is
 * the $216 below. Everything else — the names, the colours, the counts — is the app's.
 */
const MONEY_PER_DAY = 12;

/** The focus counter, 18 days in. Every number here is `TimeUtils`' own arithmetic. */
export const FOCUS = {
  name: 'Quit cigarettes',
  color: 'pink' as ThemeColor,
  days: 18,
  hours: 6,
  quitDate: '21 Oct 2026',
  // previousMilestone(18) = 14, nextMilestone(18) = 21.
  nextBadge: 21,
  progress: (18 - 14 + 6 / 24) / (21 - 14),
  earnedBadges: 4,
  money: `$${18 * MONEY_PER_DAY}`,
  urgesToday: 3,
  urgesTotal: 41,
  bestStreak: 18,
  reason: 'To breathe better on my morning walk.',
};

export const MILESTONES = [1, 3, 7, 14, 21, 30, 60, 90, 180, 365];

const badgeState = (m: number, days: number, next: number) =>
  days >= m ? 'earned' : m === next ? 'next' : 'locked';

// --- Home ---------------------------------------------------------------------------------

export const HomeScreen: React.FC<{color: ThemeColor; dark: boolean; scrollTop?: number}> = ({
  color,
  dark,
  scrollTop = 0,
}) => {
  const c = useTheme();
  return (
    <Screen>
      <HomeTopBar />
      <div style={{flex: 1, overflow: 'hidden', position: 'relative'}}>
        <div
          style={{
            position: 'absolute',
            top: -scrollTop,
            left: 0,
            right: 0,
            display: 'flex',
            flexDirection: 'column',
            gap: space.s6,
            paddingTop: space.s6,
          }}
        >
          <div style={{padding: `0 ${space.s4}px`}}>
            <QuoteSlider
              quote="Every day you say no is a day you keep."
              index={2}
              count={5}
            />
          </div>

          <Section title="Your focus" trailing="One quit counter at a time">
            <FocusCard
              name={FOCUS.name}
              color={FOCUS.color}
              dark={dark}
              daysFree={FOCUS.days}
              milestoneProgress={FOCUS.progress}
              nextBadgeText={`${FOCUS.nextBadge - FOCUS.days} days to your ${FOCUS.nextBadge}-day badge`}
              moneySaved={FOCUS.money}
              urgesToday={String(FOCUS.urgesToday)}
              reason={FOCUS.reason}
            />
          </Section>

          <Section title="Counters" trailing="4 counters · pinned first" bleed>
            <div
              style={{
                display: 'flex',
                gap: space.s3,
                padding: `${space.s1}px ${space.s4}px`,
              }}
            >
              <CounterCard
                name="Glasses of water"
                color="saffron"
                dark={dark}
                count={5}
                ofText="/ 8"
                metaText="+1 per tap"
                progress={5 / 8}
                width={164}
                minHeight={190}
              />
              <CounterCard
                name="Push-ups"
                color="teal"
                dark={dark}
                count={32}
                ofText="/ 50"
                metaText="+1 per tap"
                progress={32 / 50}
                width={164}
                minHeight={190}
              />
              <CounterCard
                name="Pages read"
                color="blue"
                dark={dark}
                count={30}
                ofText="/ 30"
                metaText="Target reached"
                progress={1}
                width={164}
                minHeight={190}
                done
              />
              <NewCounterCard />
            </div>
          </Section>

          <Section title="Quit tracker" trailing="2 habits · pinned first">
            <div style={{display: 'flex', flexDirection: 'column', gap: space.s3}}>
              <QuitCard
                name="Less phone at night"
                color="blue"
                dark={dark}
                daysFree={5}
                sideText="You said no 5 times today"
                nextBadgeText="2 days to your 7-day badge"
                milestoneProgress={(5 - 3) / (7 - 3)}
                pinned
              />
              <QuitCard
                name="Quit soft drinks"
                color="saffron"
                dark={dark}
                daysFree={6}
                sideText="$18 saved"
                nextBadgeText="1 day to your 7-day badge"
                milestoneProgress={(6 - 3) / (7 - 3)}
              />
            </div>
          </Section>
          <div style={{height: 120}} />
        </div>

        {/* The floating "New counter" button: `ink` fill, so it stands apart from the
            coloured cards under it. */}
        <div style={{position: 'absolute', right: space.s4, bottom: 28}}>
          <Button
            label="New counter"
            variant="fab"
            minHeight={56}
            icon={(fg, size) => <Icon.Plus size={size} color={fg} />}
          />
        </div>
      </div>
      <div style={{height: 0, background: c.bg}} />
    </Screen>
  );
};

// --- Normal counter -------------------------------------------------------------------------

export const CounterScreen: React.FC<{
  name: string;
  notes?: string;
  count: number;
  target: number;
  step: number;
  todayChange: number;
  done?: boolean;
}> = ({name, notes, count, target, step, todayChange, done = false}) => {
  const c = useTheme();
  return (
    <Screen>
      <TopBar
        title={name}
        subtitle={notes}
        action={
          <FramedIconButton>
            <Icon.Edit size={24} color={c.ink} />
          </FramedIconButton>
        }
      />
      <div
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: space.s4,
          padding: space.s4,
          boxSizing: 'border-box',
          overflow: 'hidden',
        }}
      >
        <div style={{width: '100%'}}>
          <StatStrip
            stats={[
              {value: `+${todayChange}`, label: 'Today'},
              {value: String(Math.max(0, target - count)), label: 'Left to target'},
              {
                value: `${Math.round(Math.min(1, count / target) * 100)}%`,
                label: 'Progress',
              },
            ]}
          />
        </div>

        <div
          style={{
            flex: 1,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 14,
          }}
        >
          <Dial
            count={count}
            state={done ? 'targetReached' : 'counting'}
            pillText={done ? 'Target reached' : `+${step} per tap`}
            hintText={done ? 'Done for today' : `Tap to add ${step}`}
            ofText={`of ${target}`}
            progress={count / target}
          />
          <div style={{...type.caption, color: c.inkMuted, textAlign: 'center'}}>
            {done
              ? 'Turn on "Keep counting after target" in Edit to go on.'
              : 'Tap anywhere on the circle'}
          </div>
        </div>

        <div style={{width: '100%'}}>
          <ControlBar />
        </div>
      </div>
    </Screen>
  );
};

// --- Quit counter ---------------------------------------------------------------------------

export const QuitScreen: React.FC<{scrollTop?: number}> = ({scrollTop = 0}) => {
  const c = useTheme();
  const left = FOCUS.nextBadge - FOCUS.days;
  return (
    <Screen>
      <TopBar
        title={FOCUS.name}
        subtitle={`Free since ${FOCUS.quitDate}`}
        action={
          <FramedIconButton>
            <Icon.Edit size={24} color={c.ink} />
          </FramedIconButton>
        }
      />
      <div style={{flex: 1, overflow: 'hidden', position: 'relative'}}>
        <div
          style={{
            position: 'absolute',
            top: -scrollTop,
            left: 0,
            right: 0,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: 20,
            padding: space.s4,
            boxSizing: 'border-box',
          }}
        >
          <div style={{paddingTop: space.s2}}>
            <DaysRing progress={FOCUS.progress}>
              <div
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: 2,
                }}
              >
                <StatusPill
                  text="Your focus"
                  tone="accent"
                  icon={<Icon.Focus size={14} color={c.onAccentSoft} />}
                />
                <span
                  style={{...type.countXl, fontSize: 92, lineHeight: '96px', color: c.ink}}
                >
                  {FOCUS.days}
                </span>
                <span style={{...type.body, fontWeight: 600, color: c.ink}}>days free</span>
                <span style={{...type.caption, color: c.inkMuted}}>
                  {`${FOCUS.days} d ${FOCUS.hours} h without it`}
                </span>
              </div>
            </DaysRing>
          </div>

          <span
            style={{
              ...type.label,
              color: c.accentStrong,
              textAlign: 'center',
              marginTop: -26,
            }}
          >
            {`${left} days to your ${FOCUS.nextBadge}-day badge`}
          </span>

          <div style={{width: '100%'}}>
            <StatStrip
              valueSize={20}
              stats={[
                {value: FOCUS.money, label: 'Money saved'},
                {value: String(FOCUS.urgesTotal), label: 'Times you said no'},
                {value: `${FOCUS.bestStreak} days`, label: 'Best streak'},
              ]}
            />
          </div>

          <div
            style={{
              width: '100%',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: space.s2,
            }}
          >
            <Button
              label="I said no"
              minHeight={60}
              icon={(fg, size) => <Icon.Plus size={size} color={fg} />}
              style={{width: '100%'}}
            />
            <span style={{...type.caption, color: c.inkMuted}}>
              {`You said no ${FOCUS.urgesToday} times today`}
            </span>
          </div>

          <div
            style={{
              width: '100%',
              background: c.accentSoft,
              borderRadius: radius.lg,
              padding: space.s4,
              boxSizing: 'border-box',
              display: 'flex',
              flexDirection: 'column',
              gap: 6,
            }}
          >
            <span style={{...type.caption, fontWeight: 600, color: c.onAccentSoft}}>
              Your reason
            </span>
            <span
              style={{
                ...type.statValue,
                fontWeight: 500,
                lineHeight: '28px',
                color: c.onAccentSoft,
              }}
            >
              {`“${FOCUS.reason}”`}
            </span>
          </div>

          <div
            style={{width: '100%', display: 'flex', flexDirection: 'column', gap: 10}}
          >
            <RowHeading title="Milestones" trailing={`${FOCUS.earnedBadges} of 10 earned`} />
            {/* The row scrolls sideways in the app; a screenshot shows where it starts. */}
            <div style={{display: 'flex', gap: space.s3}}>
              {MILESTONES.map((m) => (
                <MilestoneBadge
                  key={m}
                  days={m}
                  label={m === 1 ? '1 day' : `${m} days`}
                  state={badgeState(m, FOCUS.days, FOCUS.nextBadge) as 'earned' | 'next' | 'locked'}
                />
              ))}
            </div>
          </div>

          <div style={{width: '100%', display: 'flex', gap: space.s3}}>
            <Button
              label="Pin"
              variant="secondary"
              icon={(fg, size) => <Icon.Pin size={size} color={fg} />}
              style={{flex: 1}}
            />
            <Button
              label="Your focus"
              variant="secondaryActive"
              icon={(fg, size) => <Icon.Focus size={size} color={fg} />}
              style={{flex: 1}}
            />
            <Button
              label="I slipped"
              variant="dangerOutline"
              icon={(fg, size) => <Icon.Reset size={size} color={fg} />}
              style={{flex: 1}}
            />
          </div>
          <div style={{height: space.s2}} />
        </div>
      </div>
    </Screen>
  );
};

// --- Settings ---------------------------------------------------------------------------------

const LANGUAGES = [
  {native: 'English', english: 'English'},
  {native: 'বাংলা', english: 'Bangla'},
  {native: 'हिन्दी', english: 'Hindi'},
  {native: 'Русский', english: 'Russian'},
  {native: 'Español', english: 'Spanish'},
  {native: 'Italiano', english: 'Italian'},
];

const COLOR_NAMES: {value: ThemeColor; name: string}[] = [
  {value: 'pink', name: 'Pink'},
  {value: 'teal', name: 'Teal'},
  {value: 'saffron', name: 'Saffron'},
  {value: 'blue', name: 'Blue'},
];

export const SettingsScreen: React.FC<{
  color: ThemeColor;
  dark: boolean;
  scrollTop?: number;
}> = ({color, dark, scrollTop = 0}) => {
  const c = useTheme();
  const colorName = COLOR_NAMES.find((x) => x.value === color)!.name;
  return (
    <Screen>
      <TopBar title="Settings" />
      <div style={{flex: 1, overflow: 'hidden', position: 'relative'}}>
        <div
          style={{
            position: 'absolute',
            top: -scrollTop,
            left: 0,
            right: 0,
            display: 'flex',
            flexDirection: 'column',
            gap: space.s6,
            padding: `${space.s6}px ${space.s4}px`,
            boxSizing: 'border-box',
          }}
        >
          <SettingGroup heading="App appearance">
            <div
              style={{
                padding: space.s4,
                display: 'flex',
                flexDirection: 'column',
                gap: 18,
              }}
            >
              <div style={{display: 'flex', flexDirection: 'column', gap: space.s3}}>
                <span style={{...type.body, color: c.ink}}>Color theme</span>
                <div style={{display: 'flex', gap: space.s2}}>
                  {COLOR_NAMES.map((entry) => (
                    <ColorSwatch
                      key={entry.value}
                      name={entry.name}
                      fill={swatch(entry.value, dark)}
                      selected={entry.value === color}
                    />
                  ))}
                </div>
              </div>

              <div style={{height: 1, background: c.line}} />

              <div style={{display: 'flex', flexDirection: 'column', gap: space.s3}}>
                <span style={{...type.body, color: c.ink}}>Mode</span>
                <SegmentedControl
                  segments={['Light', 'Dark', 'System']}
                  selected={dark ? 'Dark' : 'Light'}
                />
                {/* FR-6.2: the note says which theme the user actually got. */}
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: space.s2,
                    background: c.accentSoft,
                    borderRadius: radius.md,
                    padding: `10px ${space.s3}px`,
                  }}
                >
                  <Icon.Focus size={18} color={c.onAccentSoft} />
                  <span style={{...type.label, color: c.onAccentSoft}}>
                    {`The app uses ${colorName} ~ ${dark ? 'Dark' : 'Light'}`}
                  </span>
                </div>
              </div>
            </div>
          </SettingGroup>

          <SettingGroup heading="Language">
            {LANGUAGES.map((language, i) => {
              const selected = i === 0;
              return (
                <React.Fragment key={language.english}>
                  {i > 0 ? <SettingDivider /> : null}
                  <div
                    style={{
                      minHeight: 60,
                      display: 'flex',
                      alignItems: 'center',
                      gap: space.s3,
                      padding: `${space.s2}px ${space.s4}px`,
                      background: selected ? c.accentSoft : 'transparent',
                      boxSizing: 'border-box',
                    }}
                  >
                    <div style={{flex: 1, display: 'flex', flexDirection: 'column'}}>
                      <span
                        style={{...type.body, color: selected ? c.onAccentSoft : c.ink}}
                      >
                        {language.native}
                      </span>
                      <span style={{...type.caption, color: c.inkMuted}}>
                        {language.english}
                      </span>
                    </div>
                    <RadioDot selected={selected} />
                  </div>
                </React.Fragment>
              );
            })}
          </SettingGroup>

          <SettingGroup heading="When you tap">
            <SettingRow
              label="Vibration"
              help={'Short buzz on every + and −'}
              icon={(col, size) => <Icon.Vibrate size={size} color={col} />}
              trailing={<Switch checked />}
            />
            <SettingDivider />
            <SettingRow
              label="Sound"
              help={'Soft click on every + and −'}
              icon={(col, size) => <Icon.Sound size={size} color={col} />}
              trailing={<Switch checked={false} />}
            />
          </SettingGroup>

          <SettingGroup heading="About">
            <SettingRow
              label="Privacy policy"
              icon={(col, size) => <Icon.Shield size={size} color={col} />}
              trailing={<Icon.ChevronRight size={20} color={c.inkMuted} />}
            />
          </SettingGroup>
        </div>
      </div>
    </Screen>
  );
};

// --- First run -----------------------------------------------------------------------------------

export const WelcomeScreen: React.FC<{scrollTop?: number}> = ({scrollTop = 0}) => {
  const c = useTheme();
  return (
    <Screen>
      <HomeTopBar />
      <div style={{flex: 1, overflow: 'hidden', position: 'relative'}}>
        <div
          style={{
            position: 'absolute',
            top: -scrollTop,
            left: 0,
            right: 0,
            display: 'flex',
            flexDirection: 'column',
            gap: space.s6,
            paddingTop: space.s6,
          }}
        >
          <div style={{padding: `0 ${space.s4}px`}}>
            <QuoteSlider
              quote="You are not losing a habit. You are getting time back."
              index={3}
              count={5}
            />
          </div>

          <div
            style={{
              padding: `0 ${space.s4}px`,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: space.s6,
            }}
          >
            <WelcomeMark />

            <div
              style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: space.s2,
              }}
            >
              <span style={{...type.screenTitle, fontSize: 26, color: c.ink}}>
                Welcome to HabitTap
              </span>
              <span
                style={{
                  ...type.body,
                  color: c.inkMuted,
                  textAlign: 'center',
                  width: 300,
                }}
              >
                Count anything you do, or quit a bad habit day by day. Start with your
                first counter.
              </span>
            </div>

            <div
              style={{
                width: '100%',
                display: 'flex',
                flexDirection: 'column',
                gap: space.s3,
              }}
            >
              <BigChoiceRow
                title="Normal counter"
                subtitle="Reps, glasses of water, pages, anything"
                accentFill
                glyph={(col, size) => <Icon.Plus size={size} color={col} />}
              />
              <BigChoiceRow
                title="Quit counter"
                subtitle="Stay away from a bad habit and watch the days add up"
                accentFill={false}
                glyph={(col, size) => <Icon.Quit size={size} color={col} />}
              />
            </div>

            <div
              style={{width: '100%', display: 'flex', flexDirection: 'column', gap: 10}}
            >
              <span style={{...type.label, color: c.inkMuted}}>Popular ideas</span>
              <div style={{display: 'flex', flexWrap: 'wrap', gap: space.s2}}>
                {['Push-ups', 'Glasses of water', 'Pages read', 'Quit smoking', 'Less phone at night'].map(
                  (idea) => (
                    <Chip key={idea} label={idea} />
                  ),
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </Screen>
  );
};

/** The app's eight themes, so a shot can name one. */
export const palette = (color: ThemeColor, dark: boolean) => colors(color, dark);
