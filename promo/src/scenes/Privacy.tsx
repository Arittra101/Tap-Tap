/** The claim the data-safety form makes: nothing leaves the device (NFR-1, FR-8.7). */
import React from 'react';
import {AbsoluteFill, spring, useCurrentFrame, useVideoConfig} from 'remotion';
import {LayoutContext, Stage, useSceneFade} from '../Stage';
import {sceneFrames} from '../timing';
import {Voice} from '../components/Voice';
import {colors, DISPLAY, radius, TEXT} from '../theme';
import * as Icon from '../components/Icon';

export const PRIVACY_FRAMES = sceneFrames('Privacy');

const items = [
  {icon: Icon.Shield, title: 'No account', body: 'Nothing to sign up for'},
  {icon: Icon.Globe, title: 'No internet', body: 'Works fully offline'},
  {icon: Icon.Quit, title: 'No ads', body: 'Nothing is sold, ever'},
];

export const Privacy: React.FC = () => {
  const frame = useCurrentFrame();
  const {fps} = useVideoConfig();
  const layout = React.useContext(LayoutContext);
  const fade = useSceneFade(PRIVACY_FRAMES);
  const c = colors('blue', true);
  const vertical = layout === 'vertical';

  return (
    <AbsoluteFill style={{opacity: fade}}>
      <Voice scene="Privacy" />
      <Stage
        accent={c.accent}
        phone={false}
        headline={<>Your data never<br />leaves your phone</>}
        sub="No account, no ads, no internet. HabitTap keeps everything on the device."
      >
        <div
          style={{
            display: 'flex',
            // Portrait reads a column; landscape has the width for three cards.
            flexDirection: vertical ? 'column' : 'row',
            gap: vertical ? 22 : 28,
            marginTop: 28,
          }}
        >
          {items.map((item, i) => {
            const s = spring({frame: frame - 22 - i * 7, fps, config: {damping: 14}});
            const Glyph = item.icon;
            return (
              <div
                key={item.title}
                style={{
                  width: vertical ? 820 : 300,
                  padding: vertical ? '30px 40px' : 36,
                  borderRadius: radius.xl + 12,
                  background: '#1A1816',
                  border: '1px solid #2E2A26',
                  display: 'flex',
                  flexDirection: vertical ? 'row' : 'column',
                  alignItems: vertical ? 'center' : 'flex-start',
                  gap: vertical ? 32 : 16,
                  textAlign: 'left',
                  opacity: s,
                  transform: `translateY(${(1 - s) * 24}px)`,
                  boxSizing: 'border-box',
                }}
              >
                <div
                  style={{
                    width: 86,
                    height: 86,
                    flexShrink: 0,
                    borderRadius: radius.pill,
                    background: c.accentSoft,
                    display: 'grid',
                    placeItems: 'center',
                  }}
                >
                  <Glyph size={44} color={c.accent} strokeWidth={1.8} />
                </div>
                <div style={{display: 'flex', flexDirection: 'column', gap: 6}}>
                  <span
                    style={{
                      fontFamily: DISPLAY,
                      fontSize: 38,
                      fontWeight: 600,
                      color: '#F7F5F2',
                    }}
                  >
                    {item.title}
                  </span>
                  <span style={{fontFamily: TEXT, fontSize: 24, color: '#A9A29A'}}>
                    {item.body}
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </Stage>
    </AbsoluteFill>
  );
};
