/**
 * HabitTap's components, rebuilt in React at 1dp = 1px.
 *
 * Each one mirrors the Compose component of the same name — same radii, same paddings,
 * same type styles, same token for every colour — so a frame of this video is a frame of
 * the app rather than an illustration of it. The Compose file is named above each part.
 */
import React, {createContext, useContext} from 'react';
import {Colors, DISPLAY, colors as palette, radius, space, type, ThemeColor} from '../theme';
import * as Icon from './Icon';

/** The phone the video films: a 390 x 844 screen, the width the design lays out at. */
export const SCREEN_W = 390;
export const SCREEN_H = 844;

// --- Theme -----------------------------------------------------------------------------

const ThemeContext = createContext<Colors>(palette('saffron', false));

export const useTheme = () => useContext(ThemeContext);

/** `TapTapTheme` — the app's theme, one colour x one mode. */
export const Theme: React.FC<{
  color: ThemeColor;
  dark: boolean;
  children: React.ReactNode;
}> = ({color, dark, children}) => (
  <ThemeContext.Provider value={palette(color, dark)}>{children}</ThemeContext.Provider>
);

/**
 * `CounterTheme` — one counter's own colour, inside the app's theme (FR-7.3). The mode
 * still follows the app, which is why only the colour is a parameter here.
 */
export const CounterTheme: React.FC<{
  color: ThemeColor;
  dark: boolean;
  children: (own: Colors) => React.ReactNode;
}> = ({color, dark, children}) => <>{children(palette(color, dark))}</>;

// --- Phone -----------------------------------------------------------------------------

/** The device shell. Nothing inside it knows it is being filmed. */
export const Phone: React.FC<{children: React.ReactNode; scale?: number}> = ({
  children,
  scale = 1,
}) => {
  const bezel = 14;
  return (
    <div
      style={{
        width: (SCREEN_W + bezel * 2) * scale,
        height: (SCREEN_H + bezel * 2) * scale,
        borderRadius: 62 * scale,
        background: 'linear-gradient(145deg, #3A3733, #15141300)',
        backgroundColor: '#201E1C',
        padding: bezel * scale,
        boxSizing: 'border-box',
        boxShadow: `0 ${40 * scale}px ${90 * scale}px rgba(0,0,0,0.45), inset 0 0 0 ${
          1.5 * scale
        }px rgba(255,255,255,0.14)`,
      }}
    >
      <div
        style={{
          width: SCREEN_W * scale,
          height: SCREEN_H * scale,
          borderRadius: 48 * scale,
          overflow: 'hidden',
          position: 'relative',
        }}
      >
        <div
          style={{
            width: SCREEN_W,
            height: SCREEN_H,
            transform: `scale(${scale})`,
            transformOrigin: 'top left',
            position: 'absolute',
            top: 0,
            left: 0,
          }}
        >
          {children}
        </div>
      </div>
    </div>
  );
};

/** A screen: the app's `bg`, with the status bar the system draws above it. */
export const Screen: React.FC<{children: React.ReactNode}> = ({children}) => {
  const c = useTheme();
  return (
    <div
      style={{
        width: SCREEN_W,
        height: SCREEN_H,
        background: c.bg,
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
      }}
    >
      <StatusBar />
      {children}
    </div>
  );
};

const StatusBar: React.FC = () => {
  const c = useTheme();
  return (
    <div
      style={{
        height: 44,
        flexShrink: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: `0 ${space.s6}px`,
        color: c.ink,
        ...type.label,
        fontFamily: DISPLAY,
      }}
    >
      <span>9:41</span>
      <span style={{display: 'flex', gap: 5, alignItems: 'center', opacity: 0.9}}>
        <svg width="17" height="11" viewBox="0 0 17 11" fill={c.ink}>
          <rect x="0" y="7" width="3" height="4" rx="1" />
          <rect x="4.5" y="5" width="3" height="6" rx="1" />
          <rect x="9" y="2.5" width="3" height="8.5" rx="1" />
          <rect x="13.5" y="0" width="3" height="11" rx="1" />
        </svg>
        <svg width="25" height="12" viewBox="0 0 25 12">
          <rect
            x="0.75"
            y="0.75"
            width="21"
            height="10.5"
            rx="3"
            fill="none"
            stroke={c.ink}
            strokeWidth="1.2"
            opacity="0.5"
          />
          <rect x="2.5" y="2.5" width="15" height="7" rx="1.8" fill={c.ink} />
          <path d="M23.2 4.2v3.6a2.2 2.2 0 0 0 0-3.6z" fill={c.ink} opacity="0.5" />
        </svg>
      </span>
    </div>
  );
};

// --- Bars: TopBar.kt -------------------------------------------------------------------

/** `HomeTopBar` — the wordmark and the settings button, 64dp tall. */
export const HomeTopBar: React.FC = () => {
  const c = useTheme();
  return (
    <div
      style={{
        height: space.s16,
        flexShrink: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: `0 ${space.s4}px`,
      }}
    >
      <div style={{display: 'flex', alignItems: 'center', gap: space.s2}}>
        <div
          style={{
            width: 32,
            height: 32,
            borderRadius: radius.pill,
            background: c.accentSoft,
            display: 'grid',
            placeItems: 'center',
          }}
        >
          <Icon.AppMark size={20} color={c.onAccentSoft} />
        </div>
        <span style={{...type.titleLg, color: c.ink}}>HabitTap</span>
      </div>
      <Icon.Settings size={24} color={c.ink} />
    </div>
  );
};

/** `CounterTopBar` — back, the counter's name, edit. */
export const ScreenTopBar: React.FC<{title: string}> = ({title}) => {
  const c = useTheme();
  return (
    <div
      style={{
        height: space.s16,
        flexShrink: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: `0 ${space.s4}px`,
      }}
    >
      <div style={{transform: 'rotate(180deg)'}}>
        <Icon.ChevronRight size={26} color={c.ink} />
      </div>
      <span style={{...type.screenTitle, color: c.ink}}>{title}</span>
      <Icon.Settings size={22} color={c.ink} />
    </div>
  );
};

// --- StatusPill.kt ---------------------------------------------------------------------

export type PillTone = 'accent' | 'muted' | 'success';

/** A 26dp pill that says a counter's state in a word (NFR-6: never colour alone). */
export const StatusPill: React.FC<{
  text: string;
  tone?: PillTone;
  icon?: React.ReactNode;
}> = ({text, tone = 'accent', icon}) => {
  const c = useTheme();
  const container =
    tone === 'accent' ? c.accentSoft : tone === 'muted' ? c.surfaceSunken : c.success;
  const content =
    tone === 'accent' ? c.onAccentSoft : tone === 'muted' ? c.ink : c.onSuccess;
  return (
    <div
      style={{
        minHeight: 26,
        display: 'inline-flex',
        alignItems: 'center',
        gap: space.s1,
        background: container,
        borderRadius: radius.pill,
        padding: '4px 10px',
        color: content,
        ...type.caption,
        fontWeight: 600,
      }}
    >
      {icon}
      <span>{text}</span>
    </div>
  );
};

// --- ProgressBar.kt --------------------------------------------------------------------

export const ProgressBar: React.FC<{
  progress: number;
  height?: number;
  trackColor?: string;
  fillColor?: string;
}> = ({progress, height = 6, trackColor, fillColor}) => {
  const c = useTheme();
  return (
    <div
      style={{
        width: '100%',
        height,
        background: trackColor ?? c.surfaceSunken,
        borderRadius: radius.pill,
        overflow: 'hidden',
      }}
    >
      <div
        style={{
          width: `${Math.max(0, Math.min(1, progress)) * 100}%`,
          height: '100%',
          background: fillColor ?? c.accent,
          borderRadius: radius.pill,
        }}
      />
    </div>
  );
};

// --- CounterDial.kt --------------------------------------------------------------------

const Ring: React.FC<{
  size: number;
  stroke: number;
  progress: number;
  face: string;
  track: string;
  color: string;
  /** The app draws a quiet full ring when a counter has no target. */
  emptyRing?: string;
}> = ({size, stroke, progress, face, track, color, emptyRing}) => {
  const r = (size - stroke) / 2;
  const circumference = 2 * Math.PI * r;
  const p = Math.max(0, Math.min(1, progress));
  return (
    <svg width={size} height={size} style={{position: 'absolute', top: 0, left: 0}}>
      <circle cx={size / 2} cy={size / 2} r={r} fill={face} />
      <circle
        cx={size / 2}
        cy={size / 2}
        r={r}
        fill="none"
        stroke={p > 0 || !emptyRing ? track : emptyRing}
        strokeWidth={stroke}
      />
      {p > 0 ? (
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          fill="none"
          stroke={color}
          strokeWidth={stroke}
          strokeLinecap="round"
          strokeDasharray={`${circumference * p} ${circumference}`}
          transform={`rotate(-90 ${size / 2} ${size / 2})`}
        />
      ) : null}
    </svg>
  );
};

export type DialState = 'counting' | 'reverse' | 'locked' | 'targetReached';

/**
 * The counter dial (FR-3.3). The whole circle is the button — that is the point of the
 * app, so the video presses the circle, never a small control.
 */
export const Dial: React.FC<{
  count: number;
  state: DialState;
  pillText: string;
  hintText: string;
  ofText?: string;
  progress: number;
  /** 1 at rest, 0.97 while pressed — the design's motion rule, 90ms, no bounce. */
  pressScale?: number;
  /** 0..1 ripples spreading from the last taps. */
  ripples?: number[];
  size?: number;
  stroke?: number;
  hasTarget?: boolean;
}> = ({
  count,
  state,
  pillText,
  hintText,
  ofText,
  progress,
  pressScale = 1,
  ripples = [],
  size = 310,
  stroke = 18,
  hasTarget = true,
}) => {
  const c = useTheme();
  const ringColor =
    state === 'targetReached' ? c.success : state === 'locked' ? c.lineStrong : c.accent;
  const face = state === 'locked' ? c.surfaceSunken : c.surface;
  const countColor =
    state === 'locked' ? c.inkMuted : state === 'targetReached' ? c.accentStrong : c.ink;
  const hint = state === 'counting' || state === 'reverse' ? c.accentStrong : c.inkMuted;
  const tone: PillTone =
    state === 'locked' ? 'muted' : state === 'targetReached' ? 'success' : 'accent';
  const pillIcon =
    state === 'locked' ? (
      <Icon.Lock size={14} color={c.ink} />
    ) : state === 'targetReached' ? (
      <Icon.Check size={14} color={c.onSuccess} />
    ) : state === 'reverse' ? (
      <Icon.Reverse size={14} color={c.onAccentSoft} />
    ) : undefined;

  return (
    <div
      style={{
        width: size,
        height: size,
        position: 'relative',
        transform: `scale(${pressScale})`,
      }}
    >
      {/* The dial's ripple is unbounded in the app, so it spreads past the ring. */}
      {ripples.map((t, i) => (
        <div
          key={i}
          style={{
            position: 'absolute',
            left: '50%',
            top: '50%',
            width: size,
            height: size,
            marginLeft: -size / 2,
            marginTop: -size / 2,
            borderRadius: radius.pill,
            background: ringColor,
            opacity: 0.16 * (1 - t),
            transform: `scale(${0.45 + t * 0.75})`,
          }}
        />
      ))}
      <Ring
        size={size}
        stroke={stroke}
        progress={progress}
        face={face}
        track={c.surfaceSunken}
        color={ringColor}
        emptyRing={hasTarget ? undefined : c.accentSoft}
      />
      <div
        style={{
          position: 'absolute',
          inset: 0,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 2,
          padding: `0 ${space.s6}px`,
        }}
      >
        <StatusPill text={pillText} tone={tone} icon={pillIcon} />
        <div style={{...type.countXl, color: countColor}}>{count}</div>
        {ofText ? <div style={{...type.body, color: c.inkMuted}}>{ofText}</div> : null}
        <div
          style={{
            ...type.title,
            fontFamily: DISPLAY,
            color: hint,
            textAlign: 'center',
            paddingTop: space.s2,
          }}
        >
          {hintText}
        </div>
      </div>
    </div>
  );
};

/** `DaysRing` — the quit screen's ring: same geometry, no tap target (FR-4.2). */
export const DaysRing: React.FC<{
  progress: number;
  size?: number;
  stroke?: number;
  ringColor?: string;
  children: React.ReactNode;
}> = ({progress, size = 270, stroke = 16, ringColor, children}) => {
  const c = useTheme();
  return (
    <div style={{width: size, height: size, position: 'relative'}}>
      <Ring
        size={size}
        stroke={stroke}
        progress={progress}
        face={c.surface}
        track={c.surfaceSunken}
        color={ringColor ?? c.accent}
      />
      <div
        style={{
          position: 'absolute',
          inset: 0,
          display: 'grid',
          placeItems: 'center',
          padding: `0 ${space.s6}px`,
        }}
      >
        {children}
      </div>
    </div>
  );
};

// --- StatStrip.kt ----------------------------------------------------------------------

/** Exactly three cells in one `surface` card — the design is specific that it is three. */
export const StatStrip: React.FC<{
  stats: {value: string; label: string}[];
  valueSize?: number;
}> = ({stats, valueSize = 22}) => {
  const c = useTheme();
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        background: c.surface,
        border: `1px solid ${c.line}`,
        borderRadius: radius.lg,
        padding: `${space.s3}px ${space.s1}px`,
      }}
    >
      {stats.map((s, i) => (
        <React.Fragment key={s.label}>
          {i > 0 ? <div style={{width: 1, height: 40, background: c.line}} /> : null}
          <div
            style={{
              flex: 1,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: 2,
              padding: `0 ${space.s1}px`,
            }}
          >
            <div
              style={{
                ...type.statValue,
                fontSize: valueSize,
                lineHeight: `${valueSize + 6}px`,
                color: c.ink,
                textAlign: 'center',
              }}
            >
              {s.value}
            </div>
            <div style={{...type.caption, color: c.inkMuted, textAlign: 'center'}}>
              {s.label}
            </div>
          </div>
        </React.Fragment>
      ))}
    </div>
  );
};

// --- FocusCard.kt ----------------------------------------------------------------------

/**
 * The one quit counter the user chose to focus on, first on Home (FR-1.2). Unlike every
 * other card this one *is* repainted in the counter's colour, with `on-accent` text.
 */
export const FocusCard: React.FC<{
  name: string;
  color: ThemeColor;
  dark: boolean;
  daysFree: number;
  milestoneProgress: number;
  nextBadgeText: string;
  moneySaved: string;
  urgesToday: string;
  reason?: string;
  /** 1 at rest; the scene presses the single action. */
  buttonScale?: number;
}> = ({
  name,
  color,
  dark,
  daysFree,
  milestoneProgress,
  nextBadgeText,
  moneySaved,
  urgesToday,
  reason,
  buttonScale = 1,
}) => (
  <CounterTheme color={color} dark={dark}>
    {(own) => (
      <div style={{position: 'relative'}}>
        <div
          style={{
            borderRadius: radius.xl,
            background: own.accent,
            padding: space.s4,
            display: 'flex',
            flexDirection: 'column',
            gap: space.s3,
          }}
        >
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 6,
              paddingRight: space.s8,
            }}
          >
            <Icon.Focus size={18} color={own.onAccent} />
            <span style={{...type.label, color: own.onAccent}}>{name}</span>
          </div>

          <div style={{display: 'flex', alignItems: 'baseline', gap: 10}}>
            {/* 72sp made the card taller than it needed to be; the app uses 56sp here. */}
            <span
              style={{...type.countLg, fontSize: 56, lineHeight: '58px', color: own.onAccent}}
            >
              {daysFree}
            </span>
            <span style={{...type.body, fontWeight: 600, color: own.onAccent}}>
              days free
            </span>
          </div>

          <div style={{display: 'flex', flexDirection: 'column', gap: 6}}>
            <ProgressBar
              progress={milestoneProgress}
              height={8}
              trackColor={own.accentStrong}
              fillColor={own.onAccent}
            />
            <span style={{...type.label, color: own.onAccent}}>{nextBadgeText}</span>
          </div>

          <div style={{display: 'flex', gap: space.s2}}>
            {[
              {v: moneySaved, l: 'money saved'},
              {v: urgesToday, l: 'times you said no today'},
            ].map((s) => (
              <div
                key={s.l}
                style={{
                  flex: 1,
                  border: `1px solid ${own.accentStrong}`,
                  borderRadius: radius.md,
                  padding: `8px ${space.s3}px`,
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 1,
                }}
              >
                <span style={{...type.statValue, color: own.onAccent}}>{s.v}</span>
                <span style={{...type.caption, color: own.onAccent}}>{s.l}</span>
              </div>
            ))}
          </div>

          {/* The single action. */}
          <div
            style={{
              minHeight: 52,
              borderRadius: radius.md,
              background: own.onAccent,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: space.s2,
              transform: `scale(${buttonScale})`,
            }}
          >
            <Icon.Plus size={20} color={own.accentStrong} />
            <span style={{...type.title, color: own.accentStrong}}>I said no</span>
          </div>

          {reason ? (
            <span
              style={{
                ...type.label,
                fontStyle: 'italic',
                fontWeight: 400,
                color: own.onAccent,
              }}
            >
              {`“${reason}”`}
            </span>
          ) : null}
        </div>
        <div style={{position: 'absolute', top: 12, right: 10}}>
          <Icon.ChevronRight size={24} color={own.onAccent} />
        </div>
      </div>
    )}
  </CounterTheme>
);

// --- QuitCard.kt / CounterCard.kt ------------------------------------------------------

/** One quit counter in Home's vertical "Quit tracker" list (FR-1.8). */
export const QuitCard: React.FC<{
  name: string;
  color: ThemeColor;
  dark: boolean;
  daysFree: number;
  sideText: string;
  nextBadgeText: string;
  milestoneProgress: number;
  pinned?: boolean;
}> = ({name, color, dark, daysFree, sideText, nextBadgeText, milestoneProgress, pinned}) => {
  const app = useTheme();
  return (
    <CounterTheme color={color} dark={dark}>
      {(own) => (
        <div
          style={{
            background: app.surface,
            border: `1px solid ${pinned ? app.lineStrong : app.line}`,
            borderRadius: radius.lg,
            padding: space.s4,
            display: 'flex',
            flexDirection: 'column',
            gap: 10,
          }}
        >
          <div style={{display: 'flex', alignItems: 'center'}}>
            <div
              style={{
                width: 10,
                height: 10,
                borderRadius: radius.pill,
                background: own.accent,
              }}
            />
            <span
              style={{...type.title, color: app.ink, flex: 1, paddingLeft: space.s2}}
            >
              {name}
            </span>
            <Icon.Pin
              size={20}
              color={pinned ? own.accentStrong : app.inkMuted}
              filled={pinned}
            />
          </div>

          <div style={{display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between'}}>
            <div style={{display: 'flex', alignItems: 'baseline', gap: 6}}>
              <span
                style={{
                  ...type.stat,
                  fontSize: 36,
                  lineHeight: '40px',
                  color: own.accentStrong,
                }}
              >
                {daysFree}
              </span>
              <span style={{...type.label, color: app.inkMuted}}>days free</span>
            </div>
            <span style={{...type.label, color: app.ink, textAlign: 'right'}}>
              {sideText}
            </span>
          </div>

          <ProgressBar progress={milestoneProgress} fillColor={own.accent} />

          <div style={{display: 'flex', alignItems: 'center', gap: space.s2}}>
            <span style={{...type.caption, color: app.inkMuted, flex: 1}}>
              {nextBadgeText}
            </span>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 6,
                background: own.accentSoft,
                borderRadius: radius.pill,
                padding: `8px ${space.s3}px`,
              }}
            >
              <Icon.Focus size={16} color={own.onAccentSoft} />
              <span style={{...type.caption, fontWeight: 600, color: own.onAccentSoft}}>
                Make focus
              </span>
            </div>
          </div>
        </div>
      )}
    </CounterTheme>
  );
};

/** One normal counter in Home's horizontal "Counters" row (FR-1.6). */
export const CounterCard: React.FC<{
  name: string;
  color: ThemeColor;
  dark: boolean;
  count: number;
  ofText: string;
  metaText: string;
  progress: number;
  width?: number;
  plusScale?: number;
  done?: boolean;
}> = ({
  name,
  color,
  dark,
  count,
  ofText,
  metaText,
  progress,
  width = 168,
  plusScale = 1,
  done = false,
}) => {
  const app = useTheme();
  return (
    <CounterTheme color={color} dark={dark}>
      {(own) => (
        <div
          style={{
            width,
            minHeight: 170,
            background: app.surface,
            border: `1px solid ${app.line}`,
            borderRadius: radius.lg,
            padding: 14,
            display: 'flex',
            flexDirection: 'column',
            gap: 10,
            boxSizing: 'border-box',
            flexShrink: 0,
          }}
        >
          <span style={{...type.body, fontWeight: 600, color: app.ink, paddingRight: 30}}>
            {name}
          </span>
          <div style={{display: 'flex', alignItems: 'baseline', gap: space.s1}}>
            <span style={{...type.stat, color: own.accentStrong}}>{count}</span>
            <span style={{...type.label, color: app.inkMuted}}>{ofText}</span>
          </div>
          <ProgressBar progress={progress} fillColor={own.accent} />
          <div style={{flex: 1}} />
          <div style={{display: 'flex', alignItems: 'center', gap: space.s2}}>
            <span style={{...type.caption, color: app.inkMuted, flex: 1}}>{metaText}</span>
            <div
              style={{
                width: 44,
                height: 44,
                borderRadius: radius.pill,
                background: done ? app.success : own.accent,
                display: 'grid',
                placeItems: 'center',
                transform: `scale(${plusScale})`,
              }}
            >
              {done ? (
                <Icon.Check size={22} color={app.onSuccess} />
              ) : (
                <Icon.Plus size={22} color={own.onAccent} />
              )}
            </div>
          </div>
        </div>
      )}
    </CounterTheme>
  );
};

// --- MilestoneBadge.kt -----------------------------------------------------------------

export type BadgeState = 'earned' | 'next' | 'locked';

/** One 56dp milestone badge with its label beneath (FR-4.6). */
export const MilestoneBadge: React.FC<{
  days: number;
  label: string;
  state: BadgeState;
  scale?: number;
}> = ({days, label, state, scale = 1}) => {
  const c = useTheme();
  return (
    <div
      style={{
        width: 60,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 6,
        flexShrink: 0,
        transform: `scale(${scale})`,
      }}
    >
      <div
        style={{
          width: 56,
          height: 56,
          borderRadius: radius.pill,
          background:
            state === 'earned' ? c.accent : state === 'next' ? c.surface : c.surfaceSunken,
          border: state === 'next' ? `2px dashed ${c.accentStrong}` : undefined,
          boxSizing: 'border-box',
          display: 'grid',
          placeItems: 'center',
        }}
      >
        {state === 'earned' ? (
          <Icon.Check size={22} color={c.onAccent} />
        ) : (
          <span
            style={{
              ...type.label,
              fontFamily: DISPLAY,
              color: state === 'next' ? c.accentStrong : c.inkMuted,
            }}
          >
            {days}
          </span>
        )}
      </div>
      <span
        style={{
          ...type.caption,
          fontWeight: 600,
          color: state === 'locked' ? c.inkMuted : c.ink,
          textAlign: 'center',
        }}
      >
        {label}
      </span>
    </div>
  );
};

// --- Button.kt / ControlBar.kt ---------------------------------------------------------

export const PrimaryButton: React.FC<{
  label: string;
  icon?: React.ReactNode;
  scale?: number;
}> = ({label, icon, scale = 1}) => {
  const c = useTheme();
  return (
    <div
      style={{
        height: space.s12,
        borderRadius: radius.md,
        background: c.accent,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: space.s2,
        transform: `scale(${scale})`,
      }}
    >
      {icon}
      <span style={{...type.label, color: c.onAccent}}>{label}</span>
    </div>
  );
};

/** The quiet variant: a bordered button that carries a word, never colour alone. */
export const GhostButton: React.FC<{
  label: string;
  icon?: React.ReactNode;
  danger?: boolean;
}> = ({label, icon, danger}) => {
  const c = useTheme();
  return (
    <div
      style={{
        height: space.s12,
        borderRadius: radius.md,
        background: c.surface,
        border: `1px solid ${danger ? c.danger : c.lineStrong}`,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: space.s2,
      }}
    >
      {icon}
      <span style={{...type.label, color: danger ? c.danger : c.ink}}>{label}</span>
    </div>
  );
};

/** `ControlBar` — lock, count down, reset, in one `surface` bar. */
export const ControlBar: React.FC = () => {
  const c = useTheme();
  const items = [
    {icon: <Icon.Lock size={20} color={c.ink} />, label: 'Lock'},
    {icon: <Icon.Reverse size={20} color={c.ink} />, label: 'Count down'},
    {icon: <Icon.Reset size={20} color={c.danger} />, label: 'Reset'},
  ];
  return (
    <div
      style={{
        display: 'flex',
        background: c.surface,
        border: `1px solid ${c.line}`,
        borderRadius: radius.pill,
        padding: `6px ${space.s2}px`,
      }}
    >
      {items.map((it) => (
        <div
          key={it.label}
          style={{
            flex: 1,
            height: 40,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 6,
          }}
        >
          {it.icon}
          <span
            style={{...type.label, color: it.label === 'Reset' ? c.danger : c.ink}}
          >
            {it.label}
          </span>
        </div>
      ))}
    </div>
  );
};

/** A section header on Home: a title, with a quiet count on the right. */
export const SectionHeader: React.FC<{title: string; trailing?: string}> = ({
  title,
  trailing,
}) => {
  const c = useTheme();
  return (
    <div
      style={{display: 'flex', alignItems: 'baseline', justifyContent: 'space-between'}}
    >
      <span style={{...type.title, color: c.ink}}>{title}</span>
      {trailing ? <span style={{...type.caption, color: c.inkMuted}}>{trailing}</span> : null}
    </div>
  );
};
