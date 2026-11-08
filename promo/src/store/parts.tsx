/**
 * The app parts the store screenshots need, mirrored at 1dp = 1px.
 *
 * `components/ui.tsx` already mirrors what the *film* shows. A screenshot is held still
 * and looked at, so it needs the parts the film never had to draw — the top bars, the
 * quote slider, the settings rows — and it needs the two the film simplified (the Home
 * mark and the control bar) drawn exactly as Compose draws them.
 *
 * The Compose file is named above each part, and every colour is a token. Nothing here
 * is invented: if a value is not in the app, it is not in a screenshot.
 */
import React from 'react';
import {radius, space, type} from '../theme';
import * as Icon from '../components/Icon';
import {useTheme} from '../components/ui';

// --- TopBar.kt -------------------------------------------------------------------------

/**
 * `AppMark` — a 40dp `radius-md` square in `accent` carrying the launcher glyph in
 * `onAccent`, at 60% of the square.
 */
export const AppMark: React.FC<{size?: number}> = ({size = 40}) => {
  const c = useTheme();
  return (
    <div
      style={{
        width: size,
        height: size,
        borderRadius: radius.md,
        background: c.accent,
        display: 'grid',
        placeItems: 'center',
        flexShrink: 0,
      }}
    >
      <Icon.AppMark size={size * 0.6} color={c.onAccent} />
    </div>
  );
};

/** A 48dp `Framed` icon button: `surface` fill, `line` border. */
export const FramedIconButton: React.FC<{children: React.ReactNode}> = ({children}) => {
  const c = useTheme();
  return (
    <div
      style={{
        width: 48,
        height: 48,
        borderRadius: radius.pill,
        background: c.surface,
        border: `1px solid ${c.line}`,
        boxSizing: 'border-box',
        display: 'grid',
        placeItems: 'center',
        flexShrink: 0,
      }}
    >
      {children}
    </div>
  );
};

const Divider: React.FC = () => {
  const c = useTheme();
  return <div style={{height: 1, background: c.line, flexShrink: 0}} />;
};

/** `HomeTopBar` — the mark, the wordmark, settings, and the 1dp line under them. */
export const HomeTopBar: React.FC = () => {
  const c = useTheme();
  return (
    <div style={{flexShrink: 0}}>
      <div
        style={{
          minHeight: space.s16,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: `0 ${space.s4}px`,
        }}
      >
        <div style={{display: 'flex', alignItems: 'center', gap: 10}}>
          <AppMark />
          <span style={{...type.titleLg, color: c.ink}}>HabitTap</span>
        </div>
        <FramedIconButton>
          <Icon.Settings size={24} color={c.ink} />
        </FramedIconButton>
      </div>
      <Divider />
    </div>
  );
};

/** `TopBar` — back, a centred title with an optional subtitle, at most one action. */
export const TopBar: React.FC<{
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
}> = ({title, subtitle, action}) => {
  const c = useTheme();
  return (
    <div style={{flexShrink: 0}}>
      <div
        style={{
          minHeight: space.s16,
          display: 'flex',
          alignItems: 'center',
          padding: `0 ${space.s2}px`,
        }}
      >
        <FramedIconButton>
          <Icon.ChevronLeft size={24} color={c.ink} />
        </FramedIconButton>
        <div
          style={{
            flex: 1,
            padding: `0 ${space.s2}px`,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: 2,
          }}
        >
          <span style={{...type.screenTitle, color: c.ink, textAlign: 'center'}}>
            {title}
          </span>
          {subtitle ? (
            <span style={{...type.caption, color: c.inkMuted, textAlign: 'center'}}>
              {subtitle}
            </span>
          ) : null}
        </div>
        {/* With no action, an empty 48dp slot keeps the title centred. */}
        {action ?? <div style={{width: 48, height: 48, flexShrink: 0}} />}
      </div>
      <Divider />
    </div>
  );
};

// --- QuoteSlider.kt ----------------------------------------------------------------------

/**
 * The motivation panel at the top of Home. Its text area is a fixed three lines tall, so
 * the rest of Home does not move as the lines rotate.
 */
export const QuoteSlider: React.FC<{quote: string; index: number; count: number}> = ({
  quote,
  index,
  count,
}) => {
  const c = useTheme();
  return (
    <div
      style={{
        background: c.accentSoft,
        borderRadius: radius.lg,
        padding: space.s4,
        display: 'flex',
        gap: space.s2,
      }}
    >
      <span
        style={{
          ...type.title,
          fontSize: 40,
          lineHeight: '40px',
          color: c.onAccentSoft,
          opacity: 0.4,
        }}
      >
        {'“'}
      </span>
      <div style={{flex: 1, display: 'flex', flexDirection: 'column', gap: space.s3}}>
        <div style={{height: 66, display: 'flex', alignItems: 'center'}}>
          <span
            style={{
              ...type.body,
              fontSize: 16,
              lineHeight: '22px',
              fontWeight: 500,
              color: c.onAccentSoft,
            }}
          >
            {quote}
          </span>
        </div>
        <div
          style={{display: 'flex', alignItems: 'center', justifyContent: 'space-between'}}
        >
          {/* The current dot is a short bar, not a bigger circle — shape survives
              greyscale, size alone does not. */}
          <div style={{display: 'flex', alignItems: 'center', gap: 5}}>
            {Array.from({length: count}).map((_, i) => (
              <div
                key={i}
                style={{
                  width: i === index ? 16 : 6,
                  height: 6,
                  borderRadius: radius.pill,
                  background: c.onAccentSoft,
                  opacity: i === index ? 1 : 0.3,
                }}
              />
            ))}
          </div>
          <div style={{display: 'flex', alignItems: 'center', gap: 2}}>
            <span style={{...type.label, color: c.onAccentSoft}}>Start a quit counter</span>
            <Icon.ChevronRight size={18} color={c.onAccentSoft} />
          </div>
        </div>
      </div>
    </div>
  );
};

// --- CounterCard.kt: the dashed card that ends the row -----------------------------------

export const NewCounterCard: React.FC<{width?: number; minHeight?: number}> = ({
  width = 164,
  minHeight = 190,
}) => {
  const c = useTheme();
  return (
    <div
      style={{
        width,
        minHeight,
        borderRadius: radius.lg,
        background: c.accentSoft,
        border: `2px dashed ${c.lineStrong}`,
        boxSizing: 'border-box',
        padding: 14,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: space.s3,
        flexShrink: 0,
      }}
    >
      <div
        style={{
          width: 48,
          height: 48,
          borderRadius: radius.pill,
          background: c.accent,
          display: 'grid',
          placeItems: 'center',
        }}
      >
        <Icon.Plus size={24} color={c.onAccent} />
      </div>
      <span
        style={{...type.label, color: c.onAccentSoft, textAlign: 'center'}}
      >
        New counter
      </span>
    </div>
  );
};

// --- Button.kt ---------------------------------------------------------------------------

export type ButtonVariant =
  | 'primary'
  | 'secondary'
  | 'secondaryActive'
  | 'dangerOutline'
  | 'fab';

/** A 52dp button with a short verb label and an optional leading icon. */
export const Button: React.FC<{
  label: string;
  variant?: ButtonVariant;
  icon?: (color: string, size: number) => React.ReactNode;
  minHeight?: number;
  style?: React.CSSProperties;
}> = ({label, variant = 'primary', icon, minHeight = 52, style}) => {
  const c = useTheme();
  const container: Record<ButtonVariant, string> = {
    primary: c.accent,
    secondary: c.surface,
    secondaryActive: c.accentSoft,
    dangerOutline: c.surface,
    fab: c.ink,
  };
  const content: Record<ButtonVariant, string> = {
    primary: c.onAccent,
    secondary: c.ink,
    secondaryActive: c.onAccentSoft,
    dangerOutline: c.danger,
    fab: c.bg,
  };
  const border: Partial<Record<ButtonVariant, string>> = {
    secondary: c.lineStrong,
    dangerOutline: c.lineStrong,
    secondaryActive: c.accentStrong,
  };
  const fg = content[variant];
  return (
    <div
      style={{
        minHeight,
        borderRadius: variant === 'fab' ? radius.pill : radius.md,
        background: container[variant],
        border: border[variant] ? `1px solid ${border[variant]}` : undefined,
        boxSizing: 'border-box',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: space.s2,
        padding: `${space.s2}px ${space.s6}px`,
        boxShadow: variant === 'fab' ? '0 6px 16px rgba(0,0,0,0.22)' : undefined,
        ...style,
      }}
    >
      {icon?.(fg, 20)}
      <span
        style={{...type.label, fontSize: 15, lineHeight: '20px', color: fg, textAlign: 'center'}}
      >
        {label}
      </span>
    </div>
  );
};

// --- ControlBar.kt -----------------------------------------------------------------------

/** Lock, Count down and Reset in one `surface` bar: icon over label, 60dp tall. */
export const ControlBar: React.FC<{locked?: boolean; reversed?: boolean}> = ({
  locked = false,
  reversed = false,
}) => {
  const c = useTheme();
  const cells = [
    {
      key: 'lock',
      // The label is the *action*, so an unlocked counter says "Lock".
      label: locked ? 'Unlock' : 'Lock',
      on: locked,
      glyph: (color: string) =>
        locked ? <Icon.Lock size={22} color={color} /> : <Icon.Unlock size={22} color={color} />,
    },
    {
      key: 'reverse',
      label: reversed ? 'Count up' : 'Count down',
      on: reversed,
      glyph: (color: string) => <Icon.Reverse size={22} color={color} />,
    },
    {key: 'reset', label: 'Reset', on: false, danger: true, glyph: (color: string) => <Icon.Reset size={22} color={color} />},
  ];
  return (
    <div
      style={{
        display: 'flex',
        gap: 4,
        background: c.surface,
        border: `1px solid ${c.line}`,
        borderRadius: radius.lg,
        padding: 6,
        boxSizing: 'border-box',
      }}
    >
      {cells.map((cell) => {
        const fg = cell.danger ? c.danger : cell.on ? c.onAccentSoft : c.ink;
        return (
          <div
            key={cell.key}
            style={{
              flex: 1,
              minHeight: 60,
              borderRadius: radius.md,
              background: cell.on ? c.accentSoft : 'transparent',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              gap: space.s1,
              padding: `${space.s2}px ${space.s1}px`,
              boxSizing: 'border-box',
            }}
          >
            {cell.glyph(fg)}
            <span style={{...type.caption, fontWeight: 600, color: fg, textAlign: 'center'}}>
              {cell.label}
            </span>
          </div>
        );
      })}
    </div>
  );
};

// --- SettingRow.kt -----------------------------------------------------------------------

export const SettingGroup: React.FC<{heading?: string; children: React.ReactNode}> = ({
  heading,
  children,
}) => {
  const c = useTheme();
  return (
    <div style={{display: 'flex', flexDirection: 'column', gap: 10}}>
      {heading ? (
        <span style={{...type.label, color: c.inkMuted, padding: `0 ${space.s1}px`}}>
          {heading}
        </span>
      ) : null}
      <div
        style={{
          borderRadius: radius.lg,
          background: c.surface,
          border: `1px solid ${c.line}`,
          boxSizing: 'border-box',
          overflow: 'hidden',
        }}
      >
        {children}
      </div>
    </div>
  );
};

export const SettingDivider: React.FC = () => <Divider />;

/** The switch: off keeps a 2dp `line-strong` outline, so the state is never fill alone. */
export const Switch: React.FC<{checked: boolean}> = ({checked}) => {
  const c = useTheme();
  return (
    <div
      style={{
        width: 52,
        height: 32,
        borderRadius: radius.pill,
        background: checked ? c.accent : c.surfaceSunken,
        border: `2px solid ${checked ? c.accent : c.lineStrong}`,
        boxSizing: 'border-box',
        position: 'relative',
        flexShrink: 0,
      }}
    >
      <div
        style={{
          position: 'absolute',
          top: '50%',
          left: checked ? 24 : 4,
          marginTop: -10,
          width: 20,
          height: 20,
          borderRadius: radius.pill,
          background: checked ? c.onAccent : c.lineStrong,
        }}
      />
    </div>
  );
};

/** A 64dp settings row: an icon, a label over its help line, and something trailing. */
export const SettingRow: React.FC<{
  label: string;
  help?: string;
  icon?: (color: string, size: number) => React.ReactNode;
  trailing?: React.ReactNode;
}> = ({label, help, icon, trailing}) => {
  const c = useTheme();
  return (
    <div
      style={{
        minHeight: 64,
        display: 'flex',
        alignItems: 'center',
        gap: space.s3,
        padding: `${space.s2}px ${space.s4}px`,
        boxSizing: 'border-box',
      }}
    >
      {icon?.(c.ink, 22)}
      <div style={{flex: 1, display: 'flex', flexDirection: 'column', gap: 2}}>
        <span style={{...type.body, color: c.ink}}>{label}</span>
        {help ? <span style={{...type.caption, color: c.inkMuted}}>{help}</span> : null}
      </div>
      {trailing}
    </div>
  );
};

// --- SegmentedControl.kt -----------------------------------------------------------------

export const SegmentedControl: React.FC<{segments: string[]; selected: string}> = ({
  segments,
  selected,
}) => {
  const c = useTheme();
  return (
    <div
      style={{
        display: 'flex',
        gap: 4,
        background: c.surfaceSunken,
        borderRadius: radius.pill,
        padding: 4,
      }}
    >
      {segments.map((label) => {
        const on = label === selected;
        return (
          <div
            key={label}
            style={{
              flex: 1,
              minHeight: 40,
              borderRadius: radius.pill,
              background: on ? c.surface : 'transparent',
              border: on ? `1px solid ${c.lineStrong}` : '1px solid transparent',
              boxSizing: 'border-box',
              display: 'grid',
              placeItems: 'center',
            }}
          >
            <span style={{...type.label, color: on ? c.ink : c.inkMuted}}>{label}</span>
          </div>
        );
      })}
    </div>
  );
};

// --- ColorSwatch.kt ----------------------------------------------------------------------

/** A 52dp ring with the colour inside; the name is always shown beneath it. */
export const ColorSwatch: React.FC<{name: string; fill: string; selected: boolean}> = ({
  name,
  fill,
  selected,
}) => {
  const c = useTheme();
  return (
    <div
      style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 6,
        padding: `${space.s1}px 0`,
      }}
    >
      <div
        style={{
          width: 52,
          height: 52,
          borderRadius: radius.pill,
          border: `2px solid ${selected ? c.ink : 'transparent'}`,
          boxSizing: 'border-box',
          padding: 3,
        }}
      >
        <div
          style={{
            width: '100%',
            height: '100%',
            borderRadius: radius.pill,
            background: fill,
            display: 'grid',
            placeItems: 'center',
          }}
        >
          {selected ? <Icon.Check size={20} color="#FFFFFF" /> : null}
        </div>
      </div>
      <span style={{...type.caption, color: c.ink, textAlign: 'center'}}>{name}</span>
    </div>
  );
};

export const RadioDot: React.FC<{selected: boolean}> = ({selected}) => {
  const c = useTheme();
  return (
    <div
      style={{
        width: 22,
        height: 22,
        borderRadius: radius.pill,
        border: `2px solid ${selected ? c.accentStrong : c.lineStrong}`,
        boxSizing: 'border-box',
        display: 'grid',
        placeItems: 'center',
        flexShrink: 0,
      }}
    >
      {selected ? (
        <div
          style={{
            width: 10,
            height: 10,
            borderRadius: radius.pill,
            background: c.accentStrong,
          }}
        />
      ) : null}
    </div>
  );
};

// --- WelcomeParts.kt -----------------------------------------------------------------------

/** The 168dp ring illustration above the first-run welcome. */
export const WelcomeMark: React.FC = () => {
  const c = useTheme();
  const size = 168;
  const stroke = 14;
  const r = size / 2 - stroke / 2;
  const circumference = 2 * Math.PI * r;
  const arm = size * 0.107;
  return (
    <svg width={size} height={size} style={{display: 'block'}}>
      <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke={c.surfaceSunken} strokeWidth={stroke} />
      <circle
        cx={size / 2}
        cy={size / 2}
        r={r}
        fill="none"
        stroke={c.accent}
        strokeWidth={stroke}
        strokeLinecap="round"
        strokeDasharray={`${(circumference * 119) / 360} ${circumference}`}
        transform={`rotate(-90 ${size / 2} ${size / 2})`}
      />
      <circle cx={size / 2} cy={size / 2} r={size * 0.286} fill={c.accentSoft} />
      <path
        d={`M${size / 2} ${size / 2 - arm}V${size / 2 + arm}M${size / 2 - arm} ${size / 2}H${
          size / 2 + arm
        }`}
        stroke={c.accentStrong}
        strokeWidth={7}
        strokeLinecap="round"
      />
    </svg>
  );
};

/** One of the two big choices on first run. */
export const BigChoiceRow: React.FC<{
  title: string;
  subtitle: string;
  accentFill: boolean;
  glyph: (color: string, size: number) => React.ReactNode;
}> = ({title, subtitle, accentFill, glyph}) => {
  const c = useTheme();
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 14,
        background: c.surface,
        border: `1px solid ${c.line}`,
        borderRadius: radius.lg,
        padding: space.s4,
        boxSizing: 'border-box',
      }}
    >
      <div
        style={{
          width: 48,
          height: 48,
          borderRadius: radius.md,
          background: accentFill ? c.accent : c.accentSoft,
          display: 'grid',
          placeItems: 'center',
          flexShrink: 0,
        }}
      >
        {glyph(accentFill ? c.onAccent : c.onAccentSoft, 24)}
      </div>
      <div style={{flex: 1, display: 'flex', flexDirection: 'column', gap: 2}}>
        <span style={{...type.title, color: c.ink}}>{title}</span>
        <span style={{...type.label, fontWeight: 400, color: c.inkMuted}}>{subtitle}</span>
      </div>
      <Icon.ChevronRight size={20} color={c.inkMuted} />
    </div>
  );
};

/** A 48dp pill with a short label — unselected is `surface` with a `line-strong` edge. */
export const Chip: React.FC<{label: string}> = ({label}) => {
  const c = useTheme();
  return (
    <div
      style={{
        minHeight: 48,
        display: 'flex',
        alignItems: 'center',
        background: c.surface,
        border: `1px solid ${c.lineStrong}`,
        borderRadius: radius.pill,
        padding: `10px ${space.s4}px`,
        boxSizing: 'border-box',
      }}
    >
      <span style={{...type.label, color: c.ink}}>{label}</span>
    </div>
  );
};

// --- Shared -------------------------------------------------------------------------------

/** A Home section: its header tight against its own content. */
export const Section: React.FC<{
  title: string;
  trailing: string;
  children: React.ReactNode;
  /** The counters row bleeds to the screen edges, so its header is padded on its own. */
  bleed?: boolean;
}> = ({title, trailing, children, bleed = false}) => {
  const c = useTheme();
  return (
    <div style={{display: 'flex', flexDirection: 'column', gap: 10}}>
      <div
        style={{
          display: 'flex',
          alignItems: 'flex-end',
          justifyContent: 'space-between',
          gap: space.s2,
          padding: `0 ${space.s4}px`,
        }}
      >
        <span style={{...type.title, color: c.ink}}>{title}</span>
        <span style={{...type.caption, color: c.inkMuted, textAlign: 'right'}}>
          {trailing}
        </span>
      </div>
      <div style={bleed ? undefined : {padding: `0 ${space.s4}px`}}>{children}</div>
    </div>
  );
};

/** The quit screen's own "Milestones / 4 of 10 earned" row. */
export const RowHeading: React.FC<{title: string; trailing: string}> = ({
  title,
  trailing,
}) => {
  const c = useTheme();
  return (
    <div
      style={{display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', gap: space.s2}}
    >
      <span style={{...type.title, color: c.ink}}>{title}</span>
      <span style={{...type.caption, color: c.inkMuted}}>{trailing}</span>
    </div>
  );
};
