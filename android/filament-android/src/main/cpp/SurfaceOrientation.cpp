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

#include <jni.h>

#include <geometry/SurfaceOrientation.h>

using namespace filament;
using namespace filament::geometry;

extern "C" JNIEXPORT jlong JNICALL
Java_com_google_android_filament_SurfaceOrientation_nCreateBuilder(JNIEnv*, jclass) {
    return (jlong) new SurfaceOrientation::Builder();
}

extern "C" JNIEXPORT void JNICALL
Java_com_google_android_filament_SurfaceOrientation_nDestroyBuilder(JNIEnv*, jclass,
        jlong nativeBuilder) {
    auto builder = (SurfaceOrientation::Builder *) nativeBuilder;
    delete builder;
}

extern "C" JNIEXPORT void JNICALL
Java_com_google_android_filament_SurfaceOrientation_nBuilderVertexCount(JNIEnv*, jclass,
        jlong nativeBuilder, int vertexCount) {
    auto builder = (SurfaceOrientation::Builder *) nativeBuilder;
    builder->vertexCount(vertexCount);
}

extern "C" JNIEXPORT void JNICALL
Java_com_google_android_filament_SurfaceOrientation_mBuilderNormals(JNIEnv*, jclass,
        jlong nativeBuilder, jobject buffer, int remaining, int stride) {
    auto builder = (SurfaceOrientation::Builder *) nativeBuilder;
}

extern "C" JNIEXPORT void JNICALL
Java_com_google_android_filament_SurfaceOrientation_mBuilderTangents(JNIEnv*, jclass,
        jlong nativeBuilder, jobject buffer, int remaining, int stride) {
    auto builder = (SurfaceOrientation::Builder *) nativeBuilder;

}

extern "C" JNIEXPORT void JNICALL
Java_com_google_android_filament_SurfaceOrientation_mBuilderUVs(JNIEnv*, jclass,
        jlong nativeBuilder, jobject buffer, int remaining, int stride) {
    auto builder = (SurfaceOrientation::Builder *) nativeBuilder;

}

extern "C" JNIEXPORT void JNICALL
Java_com_google_android_filament_SurfaceOrientation_mBuilderPositions(JNIEnv*, jclass,
        jlong nativeBuilder, jobject buffer, int remaining, int stride) {
    auto builder = (SurfaceOrientation::Builder *) nativeBuilder;

}

extern "C" JNIEXPORT void JNICALL
Java_com_google_android_filament_SurfaceOrientation_mBuilderTriangleCount(JNIEnv*, jclass,
        jlong nativeBuilder, int triangleCount) {
    auto builder = (SurfaceOrientation::Builder *) nativeBuilder;

}

extern "C" JNIEXPORT void JNICALL
Java_com_google_android_filament_SurfaceOrientation_mBuilderTriangles16(JNIEnv*, jclass,
        jlong nativeBuilder, jobject buffer, int remaining) {
    auto builder = (SurfaceOrientation::Builder *) nativeBuilder;

}

extern "C" JNIEXPORT void JNICALL
Java_com_google_android_filament_SurfaceOrientation_mBuilderTriangles32(JNIEnv*, jclass,
        jlong nativeBuilder, jobject buffer, int remaining) {
    auto builder = (SurfaceOrientation::Builder *) nativeBuilder;

}

extern "C" JNIEXPORT jlong JNICALL
Java_com_google_android_filament_SurfaceOrientation_nBuilderBuild(JNIEnv*, jclass,
        jlong nativeBuilder) {
    auto builder = (SurfaceOrientation::Builder *) nativeBuilder;
    SurfaceOrientation orientation = builder->build();
    SurfaceOrientation* retval = new SurfaceOrientation(std::move(orientation));
    return (jlong) retval;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_google_android_filament_SurfaceOrientation_nGetVertexCount(JNIEnv*, jclass,
        jlong nativeObject) {
    SurfaceOrientation* helper = (SurfaceOrientation*) nativeObject;
    return helper->getVertexCount();
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_google_android_filament_SurfaceOrientation_nGetQuatsAsFloat(JNIEnv*, jclass,
        jlong nativeObject) {
    SurfaceOrientation* helper = (SurfaceOrientation*) nativeObject;
    // TODO
    return nullptr;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_google_android_filament_SurfaceOrientation_nGetQuatsAsHalf(JNIEnv*, jclass,
        jlong nativeObject) {
    SurfaceOrientation* helper = (SurfaceOrientation*) nativeObject;
    // TODO
    return nullptr;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_google_android_filament_SurfaceOrientation_nGetQuatsAsShort(JNIEnv*, jclass,
        jlong nativeObject) {
    SurfaceOrientation* helper = (SurfaceOrientation*) nativeObject;
    // TODO
    return nullptr;
}

extern "C" JNIEXPORT void JNICALL
Java_com_google_android_filament_SurfaceOrientation_nDestroy(JNIEnv*, jclass,
        jlong nativeSurfaceOrientation) {
    SurfaceOrientation* helper = (SurfaceOrientation*) nativeSurfaceOrientation;
    delete helper;
}
