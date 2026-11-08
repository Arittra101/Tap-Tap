/**
 * The icons the video needs, drawn as strokes to match the app's Material Symbols
 * Rounded at weight 400 (see `TapTapIcons.kt`). Named for meaning, not for the glyph,
 * exactly as the app names them.
 */
import React from 'react';

type Props = {
  size?: number;
  color?: string;
  strokeWidth?: number;
};

const Svg: React.FC<Props & {children: React.ReactNode; fill?: boolean}> = ({
  size = 24,
  color = 'currentColor',
  strokeWidth = 2,
  fill = false,
  children,
}) => (
  <svg
    width={size}
    height={size}
    viewBox="0 0 24 24"
    fill={fill ? color : 'none'}
    stroke={fill ? 'none' : color}
    strokeWidth={strokeWidth}
    strokeLinecap="round"
    strokeLinejoin="round"
    style={{display: 'block', flexShrink: 0}}
  >
    {children}
  </svg>
);

export const Plus: React.FC<Props> = (p) => (
  <Svg {...p} strokeWidth={p.strokeWidth ?? 2.4}>
    <path d="M12 5v14M5 12h14" />
  </Svg>
);

export const Check: React.FC<Props> = (p) => (
  <Svg {...p} strokeWidth={p.strokeWidth ?? 2.6}>
    <path d="M4.5 12.5l5 5 10-10" />
  </Svg>
);

export const Lock: React.FC<Props> = (p) => (
  <Svg {...p}>
    <rect x="4.5" y="10.5" width="15" height="9.5" rx="2.5" />
    <path d="M8 10.5V8a4 4 0 0 1 8 0v2.5" />
  </Svg>
);

export const Reverse: React.FC<Props> = (p) => (
  <Svg {...p}>
    <path d="M7 4v16M7 4L4 7.5M7 4l3 3.5" />
    <path d="M17 20V4M17 20l-3-3.5M17 20l3-3.5" />
  </Svg>
);

export const Reset: React.FC<Props> = (p) => (
  <Svg {...p}>
    <path d="M19.5 12a7.5 7.5 0 1 1-2.6-5.7" />
    <path d="M19.8 4.2v3.9h-3.9" />
  </Svg>
);

/** "Your focus" / "Make focus" — the design draws two concentric circles. */
export const Focus: React.FC<Props> = (p) => (
  <Svg {...p}>
    <circle cx="12" cy="12" r="8.5" />
    <circle cx="12" cy="12" r="3.2" fill={p.color ?? 'currentColor'} stroke="none" />
  </Svg>
);

/** Settings — two sliders. */
export const Settings: React.FC<Props> = (p) => (
  <Svg {...p}>
    <path d="M4 8h10M18 8h2M4 16h4M12 16h8" />
    <circle cx="16" cy="8" r="2.2" />
    <circle cx="10" cy="16" r="2.2" />
  </Svg>
);

export const ChevronRight: React.FC<Props> = (p) => (
  <Svg {...p} strokeWidth={p.strokeWidth ?? 2.2}>
    <path d="M9.5 5.5l7 6.5-7 6.5" />
  </Svg>
);

export const Pin: React.FC<Props & {filled?: boolean}> = ({filled, ...p}) => (
  <Svg {...p} fill={filled}>
    <path d="M12 3.5h5.5l-1.5 2.5v4.5l2.5 3.5H6l2.5-3.5V6L7 3.5H12zM12 14v6.5" />
  </Svg>
);

/** The quit counter type: a circle with a slash. */
export const Quit: React.FC<Props> = (p) => (
  <Svg {...p}>
    <circle cx="12" cy="12" r="8.5" />
    <path d="M6 18L18 6" />
  </Svg>
);

export const Shield: React.FC<Props> = (p) => (
  <Svg {...p}>
    <path d="M12 3l7.5 3v5.5c0 4.6-3.1 8-7.5 9.5-4.4-1.5-7.5-4.9-7.5-9.5V6L12 3z" />
    <path d="M8.8 12.2l2.3 2.3 4.1-4.3" />
  </Svg>
);

export const Bell: React.FC<Props> = (p) => (
  <Svg {...p}>
    <path d="M6.5 16.5v-5a5.5 5.5 0 0 1 11 0v5l1.5 2H5l1.5-2z" />
    <path d="M10 19.5a2.2 2.2 0 0 0 4 0" />
  </Svg>
);

export const Globe: React.FC<Props> = (p) => (
  <Svg {...p}>
    <circle cx="12" cy="12" r="8.5" />
    <path d="M3.5 12h17M12 3.5c2.3 2.4 3.4 5.3 3.4 8.5S14.3 18.1 12 20.5c-2.3-2.4-3.4-5.3-3.4-8.5S9.7 5.9 12 3.5z" />
  </Svg>
);

/**
 * The app mark — the heart with the plus, as an even-odd hole. The path is the app's own
 * `drawable/ic_app_mark.xml`, whose glyph sits at 12..60 in a 72 viewport.
 */
export const AppMark: React.FC<{size?: number; color?: string}> = ({
  size = 48,
  color = 'currentColor',
}) => (
  <svg
    width={size}
    height={size}
    viewBox="12 12 48 48"
    style={{display: 'block', flexShrink: 0}}
  >
    <path
      fill={color}
      fillRule="evenodd"
      d="M36,55.89C20.01,45.36 13.38,37.17 13.38,28.98C13.38,21.57 18.84,16.11 25.47,16.11C30.15,16.11 33.66,18.606 36,22.74C38.34,18.606 41.85,16.11 46.53,16.11C53.16,16.11 58.62,21.57 58.62,28.98C58.62,37.17 51.99,45.36 36,55.89ZM33.465,31.125L33.465,27.42A2.535,2.535 0 0 1 38.535,27.42L38.535,31.125L42.24,31.125A2.535,2.535 0 0 1 42.24,36.195L38.535,36.195L38.535,39.9A2.535,2.535 0 0 1 33.465,39.9L33.465,36.195L29.76,36.195A2.535,2.535 0 0 1 29.76,31.125Z"
    />
  </svg>
);
