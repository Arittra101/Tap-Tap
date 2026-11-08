/**
 * Four colours x light and dark — the app's eight themes (FR-7.1), cycling on Home.
 *
 * Two copies of the same Home screen are stacked and cross-faded, so the switch looks
 * like the app repainting rather than a cut.
 */
import React from 'react';
import {AbsoluteFill, interpolate, useCurrentFrame} from 'remotion';
import {Stage, useSceneFade} from '../Stage';
import {sceneFrames} from '../timing';
import {Voice} from '../components/Voice';
import {colors, radius, space, swatch, ThemeColor} from '../theme';
import {
  CounterCard,
  FocusCard,
  HomeTopBar,
  QuitCard,
  Screen,
  SectionHeader,
  Theme,
  useTheme,
} from '../components/ui';

export const THEMES_FRAMES = sceneFrames('Themes');

const SEQUENCE: {color: ThemeColor; dark: boolean}[] = [
  {color: 'saffron', dark: false},
  {color: 'teal', dark: false},
  {color: 'blue', dark: true},
  {color: 'pink', dark: true},
];

const PER = Math.floor(THEMES_FRAMES / SEQUENCE.length);

const HomeScreen: React.FC<{color: ThemeColor; dark: boolean}> = ({color, dark}) => {
  const c = useTheme();
  return (
    <Screen>
      <HomeTopBar />
      <div
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          gap: space.s6,
          padding: `${space.s2}px 0 0`,
        }}
      >
        <div style={{padding: `0 ${space.s4}px`, display: 'flex', flexDirection: 'column', gap: 10}}>
          <SectionHeader title="Your focus" trailing="One quit counter at a time" />
          <FocusCard
            name="Quit smoking"
            color={color}
            dark={dark}
            daysFree={18}
            milestoneProgress={0.6}
            nextBadgeText="3 days to your 21-day badge"
            moneySaved="৳2,700"
            urgesToday="3"
          />
        </div>

        <div style={{display: 'flex', flexDirection: 'column', gap: 10}}>
          <div style={{padding: `0 ${space.s4}px`}}>
            <SectionHeader title="Counters" trailing="2 counters · pinned first" />
          </div>
          <div
            style={{
              display: 'flex',
              gap: space.s3,
              padding: `0 ${space.s4}px`,
            }}
          >
            <CounterCard
              name="Push-ups"
              color={color}
              dark={dark}
              count={20}
              ofText="/ 20"
              metaText="+1 per tap"
              progress={1}
              done
            />
            <CounterCard
              name="Glasses of water"
              color={color}
              dark={dark}
              count={6}
              ofText="/ 8"
              metaText="+1 per tap"
              progress={6 / 8}
            />
          </div>
        </div>

        <div style={{padding: `0 ${space.s4}px`, display: 'flex', flexDirection: 'column', gap: 10}}>
          <SectionHeader title="Quit tracker" trailing="1 habit" />
          <QuitCard
            name="Less phone at night"
            color={color}
            dark={dark}
            daysFree={7}
            sideText="You said no 2 times"
            nextBadgeText="7 days to your 14-day badge"
            milestoneProgress={0.5}
            pinned
          />
        </div>
        <div style={{height: space.s4, background: c.bg}} />
      </div>
    </Screen>
  );
};

/** The four swatches, as the Settings colour picker draws them. */
const Swatches: React.FC<{active: ThemeColor; dark: boolean}> = ({active, dark}) => (
  <div style={{display: 'flex', gap: 18, justifyContent: 'center'}}>
    {(['pink', 'teal', 'saffron', 'blue'] as ThemeColor[]).map((col) => (
      <div
        key={col}
        style={{
          width: 54,
          height: 54,
          borderRadius: radius.pill,
          background: swatch(col, dark),
          border: col === active ? '4px solid #F7F5F2' : '4px solid transparent',
          boxSizing: 'border-box',
          transform: col === active ? 'scale(1.1)' : 'scale(1)',
        }}
      />
    ))}
  </div>
);

export const Themes: React.FC = () => {
  const frame = useCurrentFrame();
  const fade = useSceneFade(THEMES_FRAMES);

  const index = Math.min(SEQUENCE.length - 1, Math.floor(frame / PER));
  const next = Math.min(SEQUENCE.length - 1, index + 1);
  const within = (frame % PER) / PER;
  const cross = index === next ? 0 : interpolate(within, [0.88, 1], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  const current = SEQUENCE[index];
  const upcoming = SEQUENCE[next];
  const shown = cross > 0.5 ? upcoming : current;

  return (
    <AbsoluteFill style={{opacity: fade}}>
      <Voice scene="Themes" />
      <Theme color={shown.color} dark={shown.dark}>
        <Stage
          accent={colors(shown.color, shown.dark).accent}
          headline={<>Four colours,<br />light and dark</>}
          sub="Eight themes, six languages, and a colour per counter if you want one."
          aside={<Swatches active={shown.color} dark={shown.dark} />}
        >
          <div style={{position: 'relative', width: 390, height: 844}}>
            <div style={{position: 'absolute', inset: 0}}>
              <Theme color={current.color} dark={current.dark}>
                <HomeScreen color={current.color} dark={current.dark} />
              </Theme>
            </div>
            <div style={{position: 'absolute', inset: 0, opacity: cross}}>
              <Theme color={upcoming.color} dark={upcoming.dark}>
                <HomeScreen color={upcoming.color} dark={upcoming.dark} />
              </Theme>
            </div>
          </div>
        </Stage>
      </Theme>
    </AbsoluteFill>
  );
};
