/**
 * The frame around the app: the headline, the sub-line and the device.
 *
 * Both compositions share it, so a scene is written once and laid out twice — portrait
 * stacks the copy above the phone, landscape sets it beside. A scene never positions
 * anything itself; it says what the words are and what is on the screen.
 */
import React from 'react';
import {AbsoluteFill, interpolate, useCurrentFrame, useVideoConfig} from 'remotion';
import {DISPLAY, TEXT} from './theme';
import {Phone} from './components/ui';

export type Layout = 'vertical' | 'landscape';

export const LayoutContext = React.createContext<Layout>('vertical');

/** The stage is deliberately darker than any app theme, so the phone reads as lit. */
export const StageBackdrop: React.FC<{accent: string}> = ({accent}) => (
  <AbsoluteFill style={{backgroundColor: '#100F0E'}}>
    <AbsoluteFill
      style={{
        background: `radial-gradient(60% 45% at 50% 18%, ${accent}33 0%, ${accent}00 70%)`,
      }}
    />
    <AbsoluteFill
      style={{
        background: `radial-gradient(70% 50% at 50% 100%, ${accent}1F 0%, ${accent}00 65%)`,
      }}
    />
  </AbsoluteFill>
);

/** Fade in over the first 10 frames, out over the last 8 — scene to scene. */
export const useSceneFade = (durationInFrames: number) => {
  const frame = useCurrentFrame();
  return interpolate(
    frame,
    [0, 10, durationInFrames - 8, durationInFrames],
    [0, 1, 1, 0],
    {extrapolateLeft: 'clamp', extrapolateRight: 'clamp'},
  );
};

/** A word rising into place — the entrance every headline uses. */
export const Rise: React.FC<{
  delay?: number;
  distance?: number;
  children: React.ReactNode;
  style?: React.CSSProperties;
}> = ({delay = 0, distance = 26, children, style}) => {
  const frame = useCurrentFrame();
  const t = interpolate(frame - delay, [0, 18], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: (x) => 1 - Math.pow(1 - x, 3),
  });
  return (
    <div
      style={{
        ...style,
        opacity: t,
        transform: `translateY(${(1 - t) * distance}px)`,
      }}
    >
      {children}
    </div>
  );
};

export const Stage: React.FC<{
  headline: React.ReactNode;
  sub?: React.ReactNode;
  accent: string;
  children?: React.ReactNode;
  /** Stage furniture under the copy — swatches, chips. Never app UI. */
  aside?: React.ReactNode;
  /** A scene with no device fills the frame with its copy instead. */
  phone?: boolean;
}> = ({headline, sub, accent, children, aside, phone = true}) => {
  const layout = React.useContext(LayoutContext);
  const {width} = useVideoConfig();
  const vertical = layout === 'vertical';

  // One scale per layout: the tallest phone that leaves the copy room to breathe.
  const scale = vertical ? 1.52 : 1.06;

  const headlineStyle: React.CSSProperties = {
    fontFamily: DISPLAY,
    fontWeight: 600,
    letterSpacing: '-0.025em',
    color: '#F7F5F2',
    fontSize: vertical ? 74 : 62,
    lineHeight: 1.08,
    margin: 0,
  };
  const subStyle: React.CSSProperties = {
    fontFamily: TEXT,
    fontWeight: 400,
    color: '#B5ADA4',
    fontSize: vertical ? 32 : 27,
    lineHeight: 1.4,
    margin: 0,
  };

  const copy = (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: vertical ? 18 : 22,
        textAlign: vertical ? 'center' : 'left',
        alignItems: vertical ? 'center' : 'flex-start',
        maxWidth: vertical ? width - 140 : 760,
      }}
    >
      <Rise style={headlineStyle}>{headline}</Rise>
      {sub ? (
        <Rise delay={6} style={subStyle}>
          {sub}
        </Rise>
      ) : null}
      {aside ? <Rise delay={12}>{aside}</Rise> : null}
    </div>
  );

  if (!phone) {
    return (
      <AbsoluteFill>
        <StageBackdrop accent={accent} />
        <AbsoluteFill
          style={{
            alignItems: 'center',
            justifyContent: 'center',
            padding: vertical ? 110 : 140,
          }}
        >
          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: vertical ? 56 : 44,
              textAlign: 'center',
            }}
          >
            <Rise style={{...headlineStyle, textAlign: 'center'}}>{headline}</Rise>
            {sub ? (
              <Rise delay={6} style={{...subStyle, textAlign: 'center'}}>
                {sub}
              </Rise>
            ) : null}
            {children}
          </div>
        </AbsoluteFill>
      </AbsoluteFill>
    );
  }

  if (vertical) {
    return (
      <AbsoluteFill>
        <StageBackdrop accent={accent} />
        <AbsoluteFill
          style={{
            flexDirection: 'column',
            alignItems: 'center',
            paddingTop: 112,
          }}
        >
          {copy}
          <Rise
            delay={10}
            distance={44}
            style={{marginTop: 54, filter: 'drop-shadow(0 30px 60px rgba(0,0,0,0.5))'}}
          >
            <Phone scale={scale}>{children}</Phone>
          </Rise>
        </AbsoluteFill>
      </AbsoluteFill>
    );
  }

  return (
    <AbsoluteFill>
      <StageBackdrop accent={accent} />
      <AbsoluteFill
        style={{
          flexDirection: 'row',
          alignItems: 'center',
          // Centred as a pair rather than pushed to the edges, so the copy and the
          // device read as one object with air around it.
          justifyContent: 'center',
          gap: 96,
          padding: `0 100px`,
        }}
      >
        {copy}
        <Rise
          delay={10}
          distance={44}
          style={{filter: 'drop-shadow(0 30px 60px rgba(0,0,0,0.5))'}}
        >
          <Phone scale={scale}>{children}</Phone>
        </Rise>
      </AbsoluteFill>
    </AbsoluteFill>
  );
};
