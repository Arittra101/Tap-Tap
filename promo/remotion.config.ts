/**
 * Render settings. The CLI reads this file; `renderMedia` does not, so the npm scripts
 * are the supported way to render.
 */
import {Config} from '@remotion/cli/config';

// PNG frames, not JPEG: JPEG frames encode as full-range yuvj420p, which some players
// and the Play Console preview render washed out. PNG keeps the output at yuv420p.
Config.setVideoImageFormat('png');
// The video is flat colour and type, so a high CRF would show banding in the gradients.
Config.setCrf(17);
Config.setPixelFormat('yuv420p');
Config.setCodec('h264');
Config.setOverwriteOutput(true);
// Four tabs render in parallel; raise it on a bigger machine.
Config.setConcurrency(4);
// PNG frames at 1080x1920 are slow to hand over, and every tab fetches the 2 MB text
// face on open; the default 30s is not enough headroom for both at once.
Config.setDelayRenderTimeoutInMilliseconds(120000);
