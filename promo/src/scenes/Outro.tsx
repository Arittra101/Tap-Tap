/** The name, the store title, and the three things that set it apart. */
import React from 'react';
import {AbsoluteFill, interpolate, spring, useCurrentFrame, useVideoConfig} from 'remotion';
import {StageBackdrop, Rise, useSceneFade} from '../Stage';
import {colors, DISPLAY, radius, TEXT} from '../theme';
import * as Icon from '../components/Icon';

export const OUTRO_FRAMES = 100;

export const Outro: React.FC = () => {
  const frame = useCurrentFrame();
  const {fps} = useVideoConfig();
  const fade = useSceneFade(OUTRO_FRAMES);
  const c = colors('saffron', true);
  const pop = spring({frame, fps, config: {damping: 15, mass: 0.6}});

  return (
    <AbsoluteFill style={{opacity: fade}}>
      <StageBackdrop accent={c.accent} />
      <AbsoluteFill
        style={{alignItems: 'center', justifyContent: 'center', flexDirection: 'column'}}
      >
        <div
          style={{
            width: 190,
            height: 190,
            borderRadius: 54,
            background: `linear-gradient(145deg, #E68A2E, #9C4607)`,
            display: 'grid',
            placeItems: 'center',
            transform: `scale(${interpolate(pop, [0, 1], [0.8, 1])})`,
            boxShadow: '0 30px 70px rgba(230, 138, 46, 0.25)',
          }}
        >
          <Icon.AppMark size={118} color="#FFFFFF" />
        </div>

        <Rise
          delay={10}
          style={{
            fontFamily: DISPLAY,
            fontWeight: 600,
            fontSize: 96,
            letterSpacing: '-0.03em',
            color: '#F7F5F2',
            marginTop: 48,
          }}
        >
          HabitTap
        </Rise>
        <Rise
          delay={16}
          style={{fontFamily: TEXT, fontSize: 36, color: '#B5ADA4', marginTop: 12}}
        >
          Counter &amp; Quit Habit
        </Rise>

        <Rise delay={26} style={{marginTop: 56}}>
          <div style={{display: 'flex', gap: 16}}>
            {['Free', 'Offline', 'No ads', '6 languages'].map((chip) => (
              <span
                key={chip}
                style={{
                  fontFamily: TEXT,
                  fontSize: 24,
                  fontWeight: 600,
                  color: c.accentStrong,
                  background: c.accentSoft,
                  borderRadius: radius.pill,
                  padding: '14px 28px',
                }}
              >
                {chip}
              </span>
            ))}
          </div>
        </Rise>
      </AbsoluteFill>
    </AbsoluteFill>
  );
};
