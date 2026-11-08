/**
 * The compositions.
 *
 * Two shapes of the same film: 9:16 for the Play Store listing and social, 16:9 for
 * YouTube, which is where a Play Store promo video has to be hosted.
 */
import React from 'react';
import {Composition} from 'remotion';
import './fonts';
import {Promo, TOTAL_FRAMES} from './Promo';

export const RemotionRoot: React.FC = () => (
  <>
    <Composition
      id="PromoVertical"
      component={Promo}
      durationInFrames={TOTAL_FRAMES}
      fps={30}
      width={1080}
      height={1920}
      defaultProps={{layout: 'vertical' as const}}
    />
    <Composition
      id="PromoLandscape"
      component={Promo}
      durationInFrames={TOTAL_FRAMES}
      fps={30}
      width={1920}
      height={1080}
      defaultProps={{layout: 'landscape' as const}}
    />
  </>
);
