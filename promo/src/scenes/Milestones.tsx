/** The ten badges, from day 1 to day 365 — `TimeUtils.MILESTONES`. */
import React from 'react';
import {AbsoluteFill, interpolate, spring, useCurrentFrame, useVideoConfig} from 'remotion';
import {Stage, useSceneFade} from '../Stage';
import {sceneFrames} from '../timing';
import {Voice} from '../components/Voice';
import {colors, MILESTONES, radius, space, type, DISPLAY} from '../theme';
import {
  BadgeState,
  GhostButton,
  MilestoneBadge,
  Screen,
  ScreenTopBar,
  SectionHeader,
  Theme,
  useTheme,
} from '../components/ui';

export const MILESTONES_FRAMES = sceneFrames('Milestones');

const DAYS = 18;

const stateFor = (badge: number): BadgeState => {
  if (badge <= DAYS) return 'earned';
  const next = MILESTONES.find((m) => m > DAYS);
  return badge === next ? 'next' : 'locked';
};

const MilestonesScreen: React.FC = () => {
  const frame = useCurrentFrame();
  const {fps} = useVideoConfig();
  const c = useTheme();
  const earned = MILESTONES.filter((m) => m <= DAYS).length;

  return (
    <Screen>
      <ScreenTopBar title="Quit smoking" />
      <div
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          padding: `${space.s2}px ${space.s4}px ${space.s6}px`,
          gap: space.s4,
        }}
      >
        <SectionHeader title="Milestones" trailing={`${earned} of 10 earned`} />

        <div style={{display: 'flex', flexDirection: 'column', gap: space.s4}}>
          {[MILESTONES.slice(0, 5), MILESTONES.slice(5)].map((row, rowIndex) => (
            <div key={rowIndex} style={{display: 'flex', gap: space.s3}}>
              {row.map((days, i) => {
                const index = rowIndex * 5 + i;
                // Each badge lands on its own beat, earned ones first.
                const s = spring({
                  frame: frame - 14 - index * 5,
                  fps,
                  config: {damping: 14, mass: 0.6},
                });
                const next = stateFor(days) === 'next';
                // The next badge keeps breathing, so the eye knows where it is going.
                const pulse = next
                  ? 1 + 0.04 * Math.sin(((frame - 60) / fps) * Math.PI * 2)
                  : 1;
                return (
                  <div key={days} style={{opacity: s}}>
                    <MilestoneBadge
                      days={days}
                      label={days === 1 ? '1 day' : `${days} days`}
                      state={stateFor(days)}
                      scale={interpolate(s, [0, 1], [0.6, 1]) * pulse}
                    />
                  </div>
                );
              })}
            </div>
          ))}
        </div>

        <div
          style={{
            background: c.accentSoft,
            borderRadius: radius.lg,
            padding: space.s4,
            display: 'flex',
            flexDirection: 'column',
            gap: 4,
          }}
        >
          <span style={{...type.statValue, color: c.onAccentSoft, fontFamily: DISPLAY}}>
            3 days to your 21-day badge
          </span>
          <span style={{...type.caption, color: c.onAccentSoft}}>
            Badges you earn stay yours, even if you slip.
          </span>
        </div>

        <div
          style={{
            background: c.surface,
            border: `1px solid ${c.line}`,
            borderRadius: radius.lg,
            padding: space.s4,
            display: 'flex',
            flexDirection: 'column',
            gap: 4,
          }}
        >
          <span style={{...type.caption, color: c.inkMuted, fontFamily: DISPLAY}}>
            Your reason
          </span>
          <span style={{...type.body, fontStyle: 'italic', color: c.ink}}>
            {'“To breathe better on my morning walk”'}
          </span>
        </div>

        <div
          style={{
            background: c.surface,
            border: `1px solid ${c.line}`,
            borderRadius: radius.lg,
            padding: space.s4,
            display: 'flex',
            flexDirection: 'column',
            gap: 4,
          }}
        >
          <span style={{...type.caption, color: c.inkMuted, fontFamily: DISPLAY}}>
            Daily motivation message
          </span>
          <span style={{...type.body, color: c.ink}}>
            One gentle nudge a morning, with your progress in it.
          </span>
        </div>

        <GhostButton label="I slipped" danger />
      </div>
    </Screen>
  );
};

export const Milestones: React.FC = () => {
  const fade = useSceneFade(MILESTONES_FRAMES);
  return (
    <AbsoluteFill style={{opacity: fade}}>
      <Voice scene="Milestones" />
      <Theme color="teal" dark={false}>
        <Stage
          accent={colors('teal', false).accent}
          headline={<>10 badges,<br />day 1 to day 365</>}
          sub="A badge at 1, 3, 7, 14, 21, 30, 60, 90, 180 and 365 days."
        >
          <MilestonesScreen />
        </Stage>
      </Theme>
    </AbsoluteFill>
  );
};
