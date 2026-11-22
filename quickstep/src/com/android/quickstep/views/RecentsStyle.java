/*
 * Copyright (C) 2024-2026 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.quickstep.views;

/**
 * Computes the visual transform (scale / translation / elevation / dim) of a single task card
 * for the custom recents layout styles (Kami, iOS, OxygenOS).
 */
final class RecentsStyle {
    float scale = 1f;
    float translation;
    float elevation;
    float dim;

    /**
     * Horizontal pull of a stacked card at the given distance from the centre, as a magnitude
     * (a view translation, i.e. towards the centre). The only place the stack math lives, so the
     * layout can never drift from what the dismiss reflow expects.
     */
    private static float stackShift(String style, float absDist, float taskSize) {
        if (taskSize <= 0f || absDist <= 0f) {
            return 0f;
        }
        if ("staple".equals(style)) {
            // Kami: iOS-like curve, slightly softer, so the pile never rides over the focused card.
            float excess = excessBeyond(absDist, taskSize);
            float squish = Math.min(excess, (float) (Math.log10(1f + excess) * 60f));
            return excess - squish;
        }
        if ("ios".equals(style)) {
            float excess = excessBeyond(absDist, taskSize);
            float squish = Math.min(excess, (float) (Math.log10(1f + excess) * 45f));
            return excess - squish;
        }
        if ("oxygen".equals(style)) {
            float bend = taskSize * 0.02f;
            float visualDistance = 2f * absDist
                    / ((float) Math.sqrt(1f + absDist / bend) + 1f);
            return absDist - visualDistance;
        }
        return 0f;
    }

    /** Distance beyond the dead zone where a stacked card starts to be pulled. */
    private static float excessBeyond(float absDist, float taskSize) {
        return Math.max(0f, absDist - taskSize * 0.1f);
    }

    /** Whether the given style pulls the card on the side it is given (see {@link #stackShift}). */
    private static boolean pullsSide(String style, float dist) {
        if ("staple".equals(style) || "ios".equals(style) || "oxygen".equals(style)) {
            // Only the incoming page is pulled: pulling the outgoing one as well makes the two
            // cards ride over each other while paging and while dismissing a card.
            return dist < 0f;
        }
        return false;
    }

    void update(String style, float distance, float taskSize, float pageSpacing,
            float progress, boolean recentsRtl) {
        scale = 1f;
        translation = 0f;
        elevation = 0f;
        dim = 0f;
        if (taskSize <= 0f || progress <= 0f || "default".equals(style)) {
            return;
        }
        progress = Math.min(1f, progress);
        float dist = recentsRtl ? distance : -distance;
        float absDist = Math.abs(dist);
        float scaleProgress = Math.min(1f, absDist / Math.max(1f, taskSize + pageSpacing));
        if ("stock".equals(style)) {
            scale = 1f - 0.15f * scaleProgress * progress;
            return;
        }
        float minScale = "oxygen".equals(style) ? 0.92f : 0.85f;
        float targetScale = 1f - (1f - minScale) * scaleProgress;
        float shift = 0f;

        if (!pullsSide(style, dist)) {
            scale = 1f + (targetScale - 1f) * progress;
            return;
        }

        shift = stackShift(style, absDist, taskSize);
        if ("staple".equals(style)) {
            float excess = excessBeyond(absDist, taskSize);
            float depth = Math.min(1f, excess / (taskSize * 1.5f));
            elevation = -(excess / taskSize) * 16f;
            dim = 0.15f * depth;
            targetScale *= 1f - depth * 0.1f;
        } else if ("ios".equals(style)) {
            float excess = excessBeyond(absDist, taskSize);
            elevation = -(excess / taskSize) * 24f;
            float depth = Math.min(1f, excess / (taskSize * 1.5f));
            dim = 0.3f * depth;
            targetScale *= 1f - depth * 0.15f;
        } else if ("oxygen".equals(style)) {
            elevation = -(absDist / taskSize) * 12f;
            targetScale *= Math.max(0.85f, 1f - absDist / (taskSize * 3f) * 0.1f);
        }

        translation = -Math.signum(dist) * shift;
        scale = 1f + (targetScale - 1f) * progress;
        translation *= (recentsRtl ? 1f : -1f) * progress;
        elevation *= progress;
        dim *= progress;
    }
}
