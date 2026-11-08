/** One scene's line of narration, placed after the scene has settled. */
import React from 'react';
import {Audio, Sequence, staticFile} from 'remotion';
import {scene, SceneName} from '../timing';

export const Voice: React.FC<{scene: SceneName}> = ({scene: name}) => {
  const s = scene(name);
  return (
    <Sequence from={s.voStart} name={`Voice: ${name}`}>
      <Audio src={staticFile(s.file)} volume={1} />
    </Sequence>
  );
};

/** A one-shot interface sound at a given frame of the scene. */
export const Sfx: React.FC<{file: string; at: number; volume?: number}> = ({
  file,
  at,
  volume = 0.5,
}) => (
  <Sequence from={at} durationInFrames={45} name={`Sfx: ${file}`}>
    <Audio src={staticFile(`sfx/${file}.wav`)} volume={volume} />
  </Sequence>
);
