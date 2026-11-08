/**
 * The normal counter: eight taps from 12 to the 20 target, and the dial turning green.
 *
 * The tap schedule is the only timing in here — the dial, the strip and the pill all
 * read from it, the same way the app's screen reads from one count.
 */
import React from 'react';
import {AbsoluteFill, interpolate, useCurrentFrame} from 'remotion';
import {Stage, useSceneFade} from '../Stage';
import {colors, space, type} from '../theme';
import {
  ControlBar,
  Dial,
  Screen,
  ScreenTopBar,
  StatStrip,
  Theme,
  useTheme,
} from '../components/ui';

export const COUNT_FRAMES = 170;

const TAPS = [30, 42, 54, 66, 78, 90, 102, 114];
const START = 12;
const TARGET = 20;
const STEP = 1;

const CounterScreen: React.FC = () => {
  const frame = useCurrentFrame();
  const c = useTheme();

  const tapsDone = TAPS.filter((f) => frame >= f).length;
  const count = START + tapsDone * STEP;
  const lastTap = TAPS.filter((f) => frame >= f).pop();
  const since = lastTap === undefined ? Infinity : frame - lastTap;

  // The design's press: 97% for 90ms, no bounce.
  const pressScale = Number.isFinite(since)
    ? interpolate(since, [0, 2.7, 6], [0.97, 0.97, 1], {
        extrapolateRight: 'clamp',
        extrapolateLeft: 'clamp',
      })
    : 1;

  const ripples = TAPS.filter((f) => frame >= f && frame - f < 20).map(
    (f) => (frame - f) / 20,
  );

  // The number jumps; the ring tweens behind it, as `animateFloatAsState` does at 180ms.
  const previous = START + Math.max(0, tapsDone - 1) * STEP;
  const ringCount =
    Number.isFinite(since) && since < 5.4
      ? previous + (count - previous) * (since / 5.4)
      : count;

  const reached = count >= TARGET;
  const todayStat = 4 + tapsDone * STEP;

  return (
    <Screen>
      <ScreenTopBar title="Push-ups" />
      <div
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          padding: `0 ${space.s4}px ${space.s6}px`,
          gap: space.s4,
        }}
      >
        <div
          style={{
            flex: 1,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            gap: space.s4,
          }}
        >
          <Dial
            count={count}
            state={reached ? 'targetReached' : 'counting'}
            pillText={reached ? 'Target reached' : `+${STEP} per tap`}
            hintText={reached ? 'Done for today' : `Tap to add ${STEP}`}
            ofText={`of ${TARGET}`}
            progress={ringCount / TARGET}
            pressScale={pressScale}
            ripples={ripples}
          />
          <div style={{...type.caption, color: c.inkMuted}}>
            Tap anywhere on the circle
          </div>
        </div>
        <div style={{width: '100%', display: 'flex', flexDirection: 'column', gap: space.s3}}>
          <StatStrip
            stats={[
              {value: `+${todayStat}`, label: 'Today'},
              {
                value: reached ? '0' : String(TARGET - count),
                label: 'Left to target',
              },
              {
                value: `${Math.round(Math.min(1, count / TARGET) * 100)}%`,
                label: 'Progress',
              },
            ]}
          />
          <ControlBar />
        </div>
      </div>
    </Screen>
  );
};

export const Count: React.FC = () => {
  const fade = useSceneFade(COUNT_FRAMES);
  const accent = colors('saffron', false).accent;
  return (
    <AbsoluteFill style={{opacity: fade}}>
      <Theme color="saffron" dark={false}>
        <Stage
          accent={accent}
          headline={<>One big tap<br />counts it</>}
          sub="Reps, water, pages, cups of tea. Set a target and watch the ring close."
        >
          <CounterScreen />
        </Stage>
      </Theme>
    </AbsoluteFill>
  );
};
