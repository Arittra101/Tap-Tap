/**
 * The app's own two typefaces, loaded from `public/` — the same variable TTFs that
 * `app/src/main/res/font/` bundles, so the video's numbers are the app's numbers.
 *
 * `delayRender` holds the frame until both faces are ready; without it a render races
 * the font load and some frames come out in a fallback face. Noto Sans is a 2 MB file
 * and several tabs fetch it at once, so the wait is generous and the frame is retried
 * rather than failing the render — a frame in the wrong face would be worse than a slow
 * one, so the handle is only ever cleared once the faces are really in `document.fonts`.
 */
import {continueRender, delayRender, staticFile} from 'remotion';

const handle = delayRender('Loading the HabitTap typefaces', {
  timeoutInMilliseconds: 120000,
  retries: 3,
});

const load = (family: string, file: string) =>
  new FontFace(family, `url(${staticFile(file)}) format('truetype')`, {
    weight: '400 700',
  })
    .load()
    .then((face) => {
      document.fonts.add(face);
    });

Promise.all([
  load('Outfit', 'outfit_variable.ttf'),
  load('NotoSansVF', 'noto_sans_variable.ttf'),
])
  .then(() => continueRender(handle))
  .catch((err) => {
    // Let the frame fail loudly: a silent fallback face would ship a wrong-looking video.
    console.error('Font load failed', err);
  });

export {};
