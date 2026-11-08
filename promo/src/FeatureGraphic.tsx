/**
 * The Play Console feature graphic: 1024 x 500, the one still the store listing leads with.
 *
 * Built from the same tokens, mark and components as the film, so the banner is the
 * product rather than a poster of it. Two rules shape the layout:
 *   - Play lays a play button over the middle when a promo video is attached, so the
 *     centre column is left empty and the copy and the card sit either side of it.
 *   - Play crops the edges on some surfaces, so nothing that has to be read comes
 *     within 64px of one.
 */
import React from 'react';
import {AbsoluteFill} from 'remotion';
import {colors, DISPLAY, TEXT, radius} from './theme';
import * as Icon from './components/Icon';
import {FocusCard, Theme} from './components/ui';

const WIDTH = 1024;
const HEIGHT = 500;

/** The glass chips under the tagline — the three things the listing has to say. */
const Chip: React.FC<{children: React.ReactNode}> = ({children}) => (
  <span
    style={{
      fontFamily: TEXT,
      fontSize: 17,
      fontWeight: 600,
      letterSpacing: '0.01em',
      color: '#D8D2CA',
      background: 'rgba(255,255,255,0.055)',
      border: '1px solid rgba(255,255,255,0.13)',
      borderRadius: radius.pill,
      padding: '9px 17px',
      whiteSpace: 'nowrap',
    }}
  >
    {children}
  </span>
);

export const FeatureGraphic: React.FC = () => {
  // Saffron on dark: the theme the film opens and closes on.
  const c = colors('saffron', true);

  return (
    <AbsoluteFill style={{width: WIDTH, height: HEIGHT, backgroundColor: '#100F0E'}}>
      {/* The stage's own light, pulled to the two halves the content sits in. */}
      <AbsoluteFill
        style={{
          background: `radial-gradient(52% 78% at 18% 24%, ${c.accent}2B 0%, ${c.accent}00 70%)`,
        }}
      />
      <AbsoluteFill
        style={{
          background: `radial-gradient(48% 80% at 82% 82%, ${c.accent}22 0%, ${c.accent}00 68%)`,
        }}
      />

      {/* One tap, spreading — the Intro's rings, held still. */}
      <AbsoluteFill style={{alignItems: 'flex-start', justifyContent: 'center'}}>
        {[0, 1, 2].map((i) => {
          const size = 220 + i * 150;
          return (
            <div
              key={i}
              style={{
                position: 'absolute',
                left: 118 - size / 2,
                top: 142 - size / 2,
                width: size,
                height: size,
                borderRadius: radius.pill,
                border: `1.5px solid ${c.accent}`,
                opacity: 0.16 - i * 0.045,
              }}
            />
          );
        })}
      </AbsoluteFill>

      <AbsoluteFill style={{flexDirection: 'row', alignItems: 'center'}}>
        {/* --- The name and the promise. --- */}
        <div
          style={{
            paddingLeft: 72,
            display: 'flex',
            flexDirection: 'column',
            gap: 26,
            width: 470,
          }}
        >
          <div style={{display: 'flex', alignItems: 'center', gap: 22}}>
            <div
              style={{
                width: 92,
                height: 92,
                borderRadius: 26,
                background: c.accentSoft,
                border: `1px solid ${c.accent}3D`,
                display: 'grid',
                placeItems: 'center',
                flexShrink: 0,
              }}
            >
              <Icon.AppMark size={58} color={c.accent} />
            </div>
            <span
              style={{
                fontFamily: DISPLAY,
                fontWeight: 600,
                fontSize: 68,
                letterSpacing: '-0.032em',
                color: '#F7F5F2',
              }}
            >
              HabitTap
            </span>
          </div>

          <span
            style={{
              fontFamily: DISPLAY,
              fontWeight: 600,
              fontSize: 34,
              lineHeight: 1.18,
              letterSpacing: '-0.02em',
              color: c.accentStrong,
            }}
          >
            Count anything.
            <br />
            Quit anything.
          </span>

          <div style={{display: 'flex', flexWrap: 'wrap', gap: 9, maxWidth: 440}}>
            <Chip>Tally counters</Chip>
            <Chip>Quit timers</Chip>
            <Chip>Milestones</Chip>
            <Chip>No account, no ads</Chip>
          </div>
        </div>

        {/* --- The app itself: one quit counter, the card Home leads with. --- */}
        <div
          style={{
            position: 'absolute',
            right: 76,
            top: '50%',
            width: 358,
            transform: 'translateY(-50%) scale(1.12)',
            transformOrigin: 'center right',
            filter: 'drop-shadow(0 26px 52px rgba(0,0,0,0.55))',
          }}
        >
          <Theme color="saffron" dark={false}>
            <FocusCard
              name="Your focus"
              color="saffron"
              dark={false}
              daysFree={21}
              milestoneProgress={0.7}
              nextBadgeText="9 days to 30-day badge"
              moneySaved="$168"
              urgesToday="4"
            />
          </Theme>
        </div>
      </AbsoluteFill>
    </AbsoluteFill>
  );
};

export const FEATURE_GRAPHIC = {width: WIDTH, height: HEIGHT};
