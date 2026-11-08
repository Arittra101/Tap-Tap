/**
 * The quit counter: 18 days free, the ring filling toward the 21-day badge, and the one
 * action the screen carries — "I said no".
 */
import React from 'react';
import {AbsoluteFill, interpolate, useCurrentFrame} from 'remotion';
import {Stage, useSceneFade} from '../Stage';
import {sceneFrames} from '../timing';
import {Sfx, Voice} from '../components/Voice';
import {colors, radius, space, type, DISPLAY} from '../theme';
import {
  DaysRing,
  GhostButton,
  MilestoneBadge,
  PrimaryButton,
  Screen,
  ScreenTopBar,
  SectionHeader,
  StatStrip,
  Theme,
  useTheme,
} from '../components/ui';
import * as Icon from '../components/Icon';

export const QUIT_FRAMES = sceneFrames('Quit');

const DAYS = 18;
const HOURS = 6;
/** TimeUtils.milestoneProgress(18, 6): previous badge 14, next 21. */
const PROGRESS = (DAYS - 14 + HOURS / 24) / (21 - 14);
const SAID_NO = 36;
const PRESS_AT = 150;

const QuitScreen: React.FC = () => {
  const frame = useCurrentFrame();
  const c = useTheme();

  const since = frame - PRESS_AT;
  const pressed = since >= 0;
  const buttonScale =
    pressed && since < 7
      ? interpolate(since, [0, 2.7, 7], [0.97, 0.97, 1], {extrapolateRight: 'clamp'})
      : 1;
  const saidNo = pressed ? SAID_NO + 1 : SAID_NO;
  // The stat that just changed lifts for a moment, so the eye goes to it.
  const statLift = pressed ? interpolate(since, [0, 6, 16], [1.12, 1, 1], {extrapolateRight: 'clamp'}) : 1;

  // The ring's sweep settles in over 180ms, as it does on open.
  const sweep = interpolate(frame, [8, 34], [0, PROGRESS], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: (x) => 1 - Math.pow(1 - x, 3),
  });

  return (
    <Screen>
      <ScreenTopBar title="Quit smoking" />
      <div
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          padding: `0 ${space.s4}px ${space.s6}px`,
          gap: space.s3,
        }}
      >
        <span style={{...type.caption, color: c.inkMuted}}>Free since 21 Oct 2026</span>

        <DaysRing progress={sweep} size={258} stroke={16}>
          <div style={{display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
            <span style={{...type.countLg, color: c.ink}}>{DAYS}</span>
            <span style={{...type.body, fontWeight: 600, color: c.ink}}>days free</span>
            <span style={{...type.caption, color: c.inkMuted, paddingTop: 6}}>
              {DAYS} d {HOURS} h without it
            </span>
          </div>
        </DaysRing>

        <div style={{width: '100%', transform: `scale(${statLift})`}}>
          <StatStrip
            valueSize={20}
            stats={[
              {value: '৳2,700', label: 'Money saved'},
              {value: String(saidNo), label: 'Times you said no'},
              {value: '18 days', label: 'Best streak'},
            ]}
          />
        </div>

        <div style={{width: '100%'}}>
          <PrimaryButton
            label="I said no"
            scale={buttonScale}
            icon={<Icon.Plus size={18} color={c.onAccent} />}
          />
        </div>

        <div
          style={{
            width: '100%',
            background: c.surface,
            border: `1px solid ${c.line}`,
            borderRadius: radius.lg,
            padding: space.s4,
            display: 'flex',
            flexDirection: 'column',
            gap: 4,
            boxSizing: 'border-box',
          }}
        >
          <span style={{...type.caption, color: c.inkMuted, fontFamily: DISPLAY}}>
            Your reason
          </span>
          <span style={{...type.body, fontStyle: 'italic', color: c.ink}}>
            {'“To breathe better on my morning walk”'}
          </span>
        </div>

        {/* The badges start here and run on below the fold — scene four scrolls to them. */}
        <div style={{width: '100%', display: 'flex', flexDirection: 'column', gap: 10}}>
          <SectionHeader title="Milestones" trailing="4 of 10 earned" />
          <div style={{display: 'flex', gap: space.s3}}>
            {[1, 3, 7, 14, 21].map((days) => (
              <MilestoneBadge
                key={days}
                days={days}
                label={days === 1 ? '1 day' : `${days} days`}
                state={days <= DAYS ? 'earned' : days === 21 ? 'next' : 'locked'}
              />
            ))}
          </div>
        </div>

        <div style={{width: '100%'}}>
          <GhostButton
            label="I slipped"
            danger
            icon={<Icon.Reset size={18} color={c.danger} />}
          />
        </div>
      </div>
    </Screen>
  );
};

export const Quit: React.FC = () => {
  const fade = useSceneFade(QUIT_FRAMES);
  return (
    <AbsoluteFill style={{opacity: fade}}>
      <Voice scene="Quit" />
      <Sfx file="press" at={PRESS_AT} volume={0.45} />
      <Theme color="teal" dark={false}>
        <Stage
          accent={colors('teal', false).accent}
          headline={<>Quit a habit,<br />day by day</>}
          sub="Days free count themselves. Money saved, every time you said no, your best streak."
        >
          <QuitScreen />
        </Stage>
      </Theme>
    </AbsoluteFill>
  );
};
