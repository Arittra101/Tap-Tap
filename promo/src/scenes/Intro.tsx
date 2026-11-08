/** The app mark, the name, the promise. */
import React from 'react';
import {AbsoluteFill, interpolate, spring, useCurrentFrame, useVideoConfig} from 'remotion';
import {StageBackdrop, Rise, useSceneFade} from '../Stage';
import {colors, DISPLAY, TEXT} from '../theme';
import * as Icon from '../components/Icon';

export const INTRO_FRAMES = 80;

export const Intro: React.FC = () => {
  const frame = useCurrentFrame();
  const {fps} = useVideoConfig();
  const c = colors('saffron', true);
  const fade = useSceneFade(INTRO_FRAMES);

  // The mark lands with the app's own press-and-settle feel, not a bounce.
  const pop = spring({frame, fps, config: {damping: 16, mass: 0.7}});
  const markScale = interpolate(pop, [0, 1], [0.72, 1]);

  return (
    <AbsoluteFill style={{opacity: fade}}>
      <StageBackdrop accent={c.accent} />
      <AbsoluteFill
        style={{alignItems: 'center', justifyContent: 'center', flexDirection: 'column'}}
      >
        <div style={{position: 'relative', width: 260, height: 260}}>
          {/* One tap, spreading. */}
          {[0, 1, 2].map((i) => {
            const t = ((frame - 10 - i * 12) % 48) / 48;
            if (frame < 10 + i * 12) return null;
            return (
              <div
                key={i}
                style={{
                  position: 'absolute',
                  inset: 0,
                  borderRadius: 999,
                  border: `3px solid ${c.accent}`,
                  opacity: (1 - t) * 0.5,
                  transform: `scale(${0.6 + t * 0.9})`,
                }}
              />
            );
          })}
          <div
            style={{
              position: 'absolute',
              inset: 0,
              borderRadius: 999,
              background: c.accentSoft,
              display: 'grid',
              placeItems: 'center',
              transform: `scale(${markScale})`,
            }}
          >
            <Icon.AppMark size={132} color={c.accent} />
          </div>
        </div>

        <Rise
          delay={14}
          style={{
            fontFamily: DISPLAY,
            fontWeight: 600,
            fontSize: 112,
            letterSpacing: '-0.03em',
            color: '#F7F5F2',
            marginTop: 64,
          }}
        >
          HabitTap
        </Rise>
        <Rise
          delay={24}
          style={{
            fontFamily: TEXT,
            fontSize: 38,
            color: '#B5ADA4',
            marginTop: 20,
          }}
        >
          Count anything. Quit anything.
        </Rise>
      </AbsoluteFill>
    </AbsoluteFill>
  );
};
