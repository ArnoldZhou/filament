/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.filament;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.nio.Buffer;

/**
 * Helper used to populate <code>TANGENTS</code> buffers.
 */
public class SurfaceOrientation {
    private long mNativeObject;

    private SurfaceOrientation(long nativeSurfaceOrientation) {
        mNativeObject = nativeSurfaceOrientation;
    }

    /**
     * Constructs an immutable surface orientation helper.
     *
     * <p>Clients provide pointers into their own data, which is synchronously consumed during
     * <code>build()</code>. At a minimum, clients must supply a vertex count and normals buffer.
     * They can supply data in any of the following three combinations:</p>
     *
     * <ol>
     * <li>vec3 normals only (not recommended)</li>
     * <li>vec3 normals + vec4 tangents (sign of W determines bitangent orientation)</li>
     * <li>vec3 normals + vec2 uvs + vec3 positions + uint3 indices</li>
     * </ol>
     */
    public static class Builder {
        @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"}) // Keep to finalize native resources
        private final BuilderFinalizer mFinalizer;
        private final long mNativeBuilder;

        public Builder() {
            mNativeBuilder = nCreateBuilder();
            mFinalizer = new BuilderFinalizer(mNativeBuilder);
        }

        @NonNull
        public Builder vertexCount(@IntRange(from = 1) int vertexCount) {
            nBuilderVertexCount(mNativeBuilder, vertexCount);
            return this;
        }

        @NonNull
        public Builder normals(@NonNull Buffer buffer, int stride) {
            nBuilderNormals(mNativeBuilder, buffer, buffer.remaining(), stride);
            return this;
        }

        @NonNull
        public Builder tangents(@NonNull Buffer buffer, int stride) {
            nBuilderTangents(mNativeBuilder, buffer, buffer.remaining(), stride);
            return this;
        }

        @NonNull
        public Builder uvs(@NonNull Buffer buffer, int stride) {
            nBuilderUVs(mNativeBuilder, buffer, buffer.remaining(), stride);
            return this;
        }

        @NonNull
        public Builder positions(@NonNull Buffer buffer, int stride) {
            nBuilderPositions(mNativeBuilder, buffer, buffer.remaining(), stride);
            return this;
        }

        @NonNull
        public Builder triangleCount(int triangleCount) {
            nBuilderTriangleCount(mNativeBuilder, triangleCount);
            return this;
        }

        @NonNull
        public Builder triangles_uint16(@NonNull Buffer buffer) {
            nBuilderTriangles16(mNativeBuilder, buffer, buffer.remaining());
            return this;
        }

        @NonNull
        public Builder triangles_uint32(@NonNull Buffer buffer) {
            nBuilderTriangles32(mNativeBuilder, buffer, buffer.remaining());
            return this;
        }

        @NonNull
        public SurfaceOrientation build() {
            long nativeSurfaceOrientation = nBuilderBuild(mNativeBuilder);
            if (nativeSurfaceOrientation == 0) throw new IllegalStateException("Couldn't create SurfaceOrientation");
            return new SurfaceOrientation(nativeSurfaceOrientation);

        }

        private static class BuilderFinalizer {
            private final long mNativeObject;

            BuilderFinalizer(long nativeObject) { mNativeObject = nativeObject; }

            @Override
            public void finalize() {
                try {
                    super.finalize();
                } catch (Throwable t) { // Ignore
                } finally {
                    nDestroyBuilder(mNativeObject);
                }
            }
        }
    }

    public long getNativeObject() {
        if (mNativeObject == 0) {
            throw new IllegalStateException("Calling method on destroyed VertexBuffer");
        }
        return mNativeObject;
    }

    @IntRange(from = 0)
    public int getVertexCount() {
        return nGetVertexCount(mNativeObject);
    }

    @NonNull
    public Buffer getQuatsAsFloat() {
        return nGetQuatsAsFloat(mNativeObject);
    }

    @NonNull
    public Buffer getQuatsAsHalf() {
        return nGetQuatsAsHalf(mNativeObject);
    }

    @NonNull
    public Buffer getQuatsAsShort() {
        return nGetQuatsAsShort(mNativeObject);
    }

    public void destroy() {
        nDestroy(mNativeObject);
        mNativeObject = 0;
    }

    private static native long nCreateBuilder();
    private static native void nDestroyBuilder(long nativeBuilder);

    private static native void nBuilderVertexCount(long nativeBuilder, int vertexCount);
    private static native void nBuilderNormals(long nativeBuilder, Buffer buffer, int remaining, int stride);
    private static native void nBuilderTangents(long nativeBuilder, Buffer buffer, int remaining, int stride);
    private static native void nBuilderUVs(long nativeBuilder, Buffer buffer, int remaining, int stride);
    private static native void nBuilderPositions(long nativeBuilder, Buffer buffer, int remaining, int stride);
    private static native void nBuilderTriangleCount(long nativeBuilder, int triangleCount);
    private static native void nBuilderTriangles16(long nativeBuilder, Buffer buffer, int remaining);
    private static native void nBuilderTriangles32(long nativeBuilder, Buffer buffer, int remaining);
    private static native long nBuilderBuild(long nativeBuilder);

    private static native int nGetVertexCount(long nativeSurfaceOrientation);
    private static native Buffer nGetQuatsAsFloat(long nativeSurfaceOrientation);
    private static native Buffer nGetQuatsAsHalf(long nativeSurfaceOrientation);
    private static native Buffer nGetQuatsAsShort(long nativeSurfaceOrientation);
    private static native void nDestroy(long nativeSurfaceOrientation);
}
