/**
 * HabitTap's design tokens, copied from the app's own
 * `app/src/main/java/com/habitTap/ui/theme/` — Color.kt, Type.kt, Shape.kt, Spacing.kt.
 *
 * Copied, never invented: the promo has to be the same product the user installs, so
 * every value here has a counterpart in the app. When a token changes there, change it
 * here too — this file is the one place the video keeps colours.
 */

import type React from 'react';

export type ThemeColor = 'pink' | 'teal' | 'saffron' | 'blue';

export interface Colors {
  bg: string;
  surface: string;
  surfaceRaised: string;
  surfaceSunken: string;
  ink: string;
  inkMuted: string;
  line: string;
  lineStrong: string;
  accent: string;
  onAccent: string;
  accentStrong: string;
  accentSoft: string;
  onAccentSoft: string;
  danger: string;
  onDanger: string;
  success: string;
  onSuccess: string;
  dim: string;
}

// --- Neutrals: same in every colour theme, different per mode. -------------------------

const lightNeutrals = {
  bg: '#F7F5F2',
  surface: '#FFFFFF',
  surfaceRaised: '#FFFFFF',
  surfaceSunken: '#EEEBE6',
  ink: '#1C1A18',
  inkMuted: '#5C5751',
  line: '#D9D4CC',
  lineStrong: '#8A847C',
  danger: '#B3261E',
  onDanger: '#FFFFFF',
  success: '#2E6B34',
  onSuccess: '#FFFFFF',
  dim: 'rgba(28, 26, 24, 0.45)',
};

const darkNeutrals = {
  bg: '#121110',
  surface: '#1C1B19',
  surfaceRaised: '#272522',
  surfaceSunken: '#0B0A09',
  ink: '#F2EFEA',
  inkMuted: '#AAA39A',
  line: '#38352F',
  lineStrong: '#7C766E',
  danger: '#FFB4AB',
  onDanger: '#5A0B05',
  success: '#93D39A',
  onSuccess: '#0B3811',
  dim: 'rgba(0, 0, 0, 0.60)',
};

// --- Accents: the five tokens that change with the colour theme. -----------------------

interface AccentSet {
  accent: string;
  onAccent: string;
  accentStrong: string;
  accentSoft: string;
  onAccentSoft: string;
}

const accents: Record<ThemeColor, {light: AccentSet; dark: AccentSet}> = {
  pink: {
    light: {
      accent: '#C2185B',
      onAccent: '#FFFFFF',
      accentStrong: '#96124A',
      accentSoft: '#FCE4EC',
      onAccentSoft: '#7F0F3E',
    },
    dark: {
      accent: '#F48FB1',
      onAccent: '#3D0A1E',
      accentStrong: '#F8BBD0',
      accentSoft: '#3B1A27',
      onAccentSoft: '#F8BBD0',
    },
  },
  teal: {
    light: {
      accent: '#00796B',
      onAccent: '#FFFFFF',
      accentStrong: '#005A4F',
      accentSoft: '#DDF2EE',
      onAccentSoft: '#00463E',
    },
    dark: {
      accent: '#4FD1BF',
      onAccent: '#00302A',
      accentStrong: '#91E7DA',
      accentSoft: '#12302C',
      onAccentSoft: '#91E7DA',
    },
  },
  saffron: {
    light: {
      accent: '#B45309',
      onAccent: '#FFFFFF',
      accentStrong: '#8A3F06',
      accentSoft: '#FDEEDC',
      onAccentSoft: '#743405',
    },
    dark: {
      accent: '#F6B25B',
      onAccent: '#3A2000',
      accentStrong: '#FAD29C',
      accentSoft: '#352619',
      onAccentSoft: '#FAD29C',
    },
  },
  blue: {
    light: {
      accent: '#1F5FBF',
      onAccent: '#FFFFFF',
      accentStrong: '#174A96',
      accentSoft: '#E3ECFA',
      onAccentSoft: '#123D7C',
    },
    dark: {
      accent: '#8AB4F8',
      onAccent: '#0A2A5C',
      accentStrong: '#B9D2FB',
      accentSoft: '#1B2638',
      onAccentSoft: '#B9D2FB',
    },
  },
};

/** The palette for one of the app's eight themes (one colour x one mode). */
export const colors = (color: ThemeColor, dark: boolean): Colors => ({
  ...(dark ? darkNeutrals : lightNeutrals),
  ...(dark ? accents[color].dark : accents[color].light),
});

/** The four swatches, as the Settings colour picker shows them. */
export const swatch = (color: ThemeColor, dark: boolean): string =>
  dark ? accents[color].dark.accent : accents[color].light.accent;

export const themeColors: ThemeColor[] = ['pink', 'teal', 'saffron', 'blue'];

// --- Spacing, radii, type: Spacing.kt, Shape.kt, Type.kt. ------------------------------

export const space = {
  s1: 4,
  s2: 8,
  s3: 12,
  s4: 16,
  s6: 24,
  s8: 32,
  s12: 48,
  s16: 64,
} as const;

export const radius = {
  sm: 8,
  md: 14,
  lg: 24,
  xl: 28,
  pill: 999,
} as const;

/** Outfit for numbers and titles, Noto Sans for text — FR-7.5. */
export const DISPLAY = 'Outfit';
export const TEXT = 'NotoSansVF';

type Style = React.CSSProperties;

/** The nine named styles from `TapTapType`, as CSS. */
export const type: Record<string, Style> = {
  countXl: {
    fontFamily: DISPLAY,
    fontSize: 104,
    lineHeight: '108px',
    fontWeight: 600,
    letterSpacing: '-0.029em',
    fontFeatureSettings: '"tnum"',
  },
  countLg: {
    fontFamily: DISPLAY,
    fontSize: 72,
    lineHeight: '72px',
    fontWeight: 600,
    letterSpacing: '-0.028em',
    fontFeatureSettings: '"tnum"',
  },
  stat: {
    fontFamily: DISPLAY,
    fontSize: 34,
    lineHeight: '38px',
    fontWeight: 600,
    fontFeatureSettings: '"tnum"',
  },
  titleLg: {fontFamily: DISPLAY, fontSize: 22, lineHeight: '28px', fontWeight: 600},
  screenTitle: {fontFamily: DISPLAY, fontSize: 20, lineHeight: '26px', fontWeight: 600},
  title: {fontFamily: TEXT, fontSize: 17, lineHeight: '24px', fontWeight: 600},
  body: {fontFamily: TEXT, fontSize: 15, lineHeight: '22px', fontWeight: 400},
  label: {
    fontFamily: TEXT,
    fontSize: 13,
    lineHeight: '18px',
    fontWeight: 600,
    letterSpacing: '0.015em',
  },
  caption: {fontFamily: TEXT, fontSize: 12, lineHeight: '16px', fontWeight: 400},
  statValue: {
    fontFamily: DISPLAY,
    fontSize: 20,
    lineHeight: '26px',
    fontWeight: 600,
    fontFeatureSettings: '"tnum"',
  },
};

/** The ten milestone days — `TimeUtils.MILESTONES`. */
export const MILESTONES = [1, 3, 7, 14, 21, 30, 60, 90, 180, 365];
