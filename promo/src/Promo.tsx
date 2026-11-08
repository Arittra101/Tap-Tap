/**
 * The film: seven scenes, in order, shared by both compositions.
 *
 * The layout comes in through context rather than a prop on every scene, so a scene
 * describes what it shows and the stage decides where it goes.
 */
import React from 'react';
import {AbsoluteFill, Audio, Series, staticFile} from 'remotion';
import {Layout, LayoutContext} from './Stage';
import {musicVolume, TOTAL_FRAMES as TIMELINE_FRAMES} from './timing';
import {Intro, INTRO_FRAMES} from './scenes/Intro';
import {Count, COUNT_FRAMES} from './scenes/Count';
import {Quit, QUIT_FRAMES} from './scenes/Quit';
import {Milestones, MILESTONES_FRAMES} from './scenes/Milestones';
import {Themes, THEMES_FRAMES} from './scenes/Themes';
import {Privacy, PRIVACY_FRAMES} from './scenes/Privacy';
import {Outro, OUTRO_FRAMES} from './scenes/Outro';

export const SCENES = [
  {component: Intro, frames: INTRO_FRAMES, name: 'Intro'},
  {component: Count, frames: COUNT_FRAMES, name: 'Count'},
  {component: Quit, frames: QUIT_FRAMES, name: 'Quit'},
  {component: Milestones, frames: MILESTONES_FRAMES, name: 'Milestones'},
  {component: Themes, frames: THEMES_FRAMES, name: 'Themes'},
  {component: Privacy, frames: PRIVACY_FRAMES, name: 'Privacy'},
  {component: Outro, frames: OUTRO_FRAMES, name: 'Outro'},
] as const;

export const TOTAL_FRAMES = SCENES.reduce((sum, s) => sum + s.frames, 0);

if (TOTAL_FRAMES !== TIMELINE_FRAMES) {
  // The scenes and the voice track have drifted apart; re-run audio/build_vo.py.
  console.warn(
    `Scene lengths total ${TOTAL_FRAMES} frames but timeline.json says ${TIMELINE_FRAMES}.`,
  );
}

export const Promo: React.FC<{layout: Layout}> = ({layout}) => (
  <LayoutContext.Provider value={layout}>
    <AbsoluteFill style={{backgroundColor: '#100F0E'}}>
      {/* The score, ducking under each line — see `musicVolume`. */}
      <Audio src={staticFile('music/bed.wav')} volume={musicVolume} />
      <Series>
        {SCENES.map((scene) => {
          const Scene = scene.component;
          return (
            <Series.Sequence
              key={scene.name}
              durationInFrames={scene.frames}
              name={scene.name}
            >
              <Scene />
            </Series.Sequence>
          );
        })}
      </Series>
    </AbsoluteFill>
  </LayoutContext.Provider>
);
