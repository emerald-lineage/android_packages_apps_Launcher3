/*
 * Copyright (C) 2024-2026 Lunaris AOSP
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
 * Stacked ("iOS") overview card transform: cards on one side of the centred task slide under it,
 * shrink, sink and dim with distance.
 *
 * Ported from Lunaris AOSP packages_apps_Launcher3 (branch 16.2, RecentsStyle at 8cba80f2);
 * emerald keeps only the "ios" style, always on.
 */
final class RecentsStyle {
    private static final float MIN_SCALE = 0.85f;

    float scale = 1f;
    float translation;
    float elevation;
    float dim;

    void update(float distance, float taskSize, float pageSpacing, float progress,
            boolean recentsRtl) {
        scale = 1f;
        translation = 0f;
        elevation = 0f;
        dim = 0f;
        if (taskSize <= 0f || progress <= 0f) {
            return;
        }
        progress = Math.min(1f, progress);
        float dist = recentsRtl ? distance : -distance;
        float absDist = Math.abs(dist);
        float scaleProgress = Math.min(1f, absDist / Math.max(1f, taskSize + pageSpacing));
        float targetScale = 1f - (1f - MIN_SCALE) * scaleProgress;

        if (dist < 0f) {
            float excess = Math.max(0f, absDist - taskSize * 0.1f);
            float squish = Math.min(excess, (float) (Math.log10(1f + excess) * 45f));
            translation = excess - squish;
            elevation = -(excess / taskSize) * 24f;
            float depth = Math.min(1f, excess / (taskSize * 1.5f));
            dim = 0.3f * depth;
            targetScale *= 1f - depth * 0.15f;
        }

        scale = 1f + (targetScale - 1f) * progress;
        translation *= (recentsRtl ? 1f : -1f) * progress;
        elevation *= progress;
        dim *= progress;
    }
}
