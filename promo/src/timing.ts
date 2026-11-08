/**
 * The edit, as `audio/build_vo.py` measured it.
 *
 * Scene lengths are not chosen here — they come from how long the voice actually takes
 * to say each line, plus a lead-in and a tail. Re-record the voice and the film re-cuts
 * itself; nothing in `src/` has a hard-coded duration.
 */
import timeline from './timeline.json';

export type SceneName =
  | 'Intro'
  | 'Count'
  | 'Quit'
  | 'Milestones'
  | 'Themes'
  | 'Privacy'
  | 'Outro';

export const FPS = timeline.fps;
export const TOTAL_FRAMES = timeline.totalFrames;
export const VOICE = timeline.voice;

export const scene = (name: SceneName) => {
  const found = timeline.scenes.find((s) => s.name === name);
  if (!found) {
    throw new Error(`No scene ${name} in timeline.json — re-run audio/build_vo.py`);
  }
  return found;
};

export const sceneFrames = (name: SceneName) => scene(name).frames;

/**
 * The music sits back under a line and comes up between them.
 *
 * Ducking is done here rather than baked into the WAV so the balance can be changed
 * without re-synthesising the score.
 */
const UNDER_VOICE = 0.3;
const BETWEEN_LINES = 0.62;
const RAMP = 10;

const voiceWindows = timeline.scenes.map((s) => ({
  from: s.start + s.voStart,
  to: s.start + s.voStart + s.voFrames,
}));

export const musicVolume = (frame: number): number => {
  const distance = Math.min(
    ...voiceWindows.map((w) =>
      frame >= w.from && frame <= w.to ? 0 : Math.min(Math.abs(frame - w.from), Math.abs(frame - w.to)),
    ),
  );
  const open = Math.min(1, distance / RAMP);
  return UNDER_VOICE + (BETWEEN_LINES - UNDER_VOICE) * open;
};
