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

package com.google.android.filament.utils

import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Encapsulates a triangle mesh produced from [generateOctasphere].
 *
 * Positions are `Float3`, tangents are `Float4` quaternions, and triangles are `UShort3`.
 * This uses [Buffer] rather than Array<> to simplify pushing the data into Filament.
 */
data class OctasphereMesh(
    val numVertices: Int,
    val numIndices: Int,
    val positions: Buffer,
    val tangents: Buffer,
    val indices: Buffer
)

private const val kFloatSize = 4

/**
 * The requested number of subdivisions is automatically clamped to this arbitrary value as
 * a protective measure, since the triangle count can grow exponentially. Currently this value
 * is set to **5**.
 */
const val kOctasphereMaxSubdivisions = 5

/** Size in bytes of the position attribute in each vertex. */
const val kOctaspherePositionSize = 3 * kFloatSize

/** Size in bytes of the tangent attribute in each vertex. */
const val kOctasphereTangentSize = 4 * kFloatSize

/** Size in bytes of each triangle index. */
const val kOctasphereIndexSize = 2

/**
 * Consumes a simple shape description and produces a triangle mesh.
 *
 * - For a **sphere**, set `radius` to a non-zero value and use (0,0,0) for `dimensions`.
 * - For a **capsule**, use a non-zero radius and set two dimension components to zero.
 * - For a **cuboid**, set the radius to zero and use non-zero dimensions.
 * - For a **rounded  cuboid**, use a non-zero corner radius and non-zero dimensions.
 *
 * ### Implementation note
 *
 * This function does not actually perform iterative subdivision. Instead, it tessellates a series
 * of geodesic lines over a patch encompassing one-eighth of the unit sphere, then clones the
 * result 7 times. This is equivalent to iterative subdivision of an octahedron. If a cuboid or
 * capsule shape is requested, the 8 patches are translated away from each other.
 *
 * @param dimensions The width / height / depth of the rounded cuboid. Each of these dimensions
 * is automatically clamped so that they can be no smaller than twice the [radius] value.
 * @param radius The corner radius of the rounded cuboid.
 * @param subdivisions Setting this to 0 produces an octahedron, use a higher value for a smoother
 * surface. Often a value of 2 or 3 is sufficient. This is automatically clamped to be no larger
 * than [kOctasphereMaxSubdivisions].
 */
fun generateOctasphere(dimensions: Float3, radius: Float, subdivisions: Int): OctasphereMesh {
    val actualSubdivisions = clamp(subdivisions, 0, kOctasphereMaxSubdivisions)
    val verticesPerLine = 1.shl(actualSubdivisions) + 1
    val verticesPerPatch = verticesPerLine * (verticesPerLine + 1) / 2
    val trianglesPerPatch = (verticesPerLine - 2) * (verticesPerLine - 1) + verticesPerLine - 1
    val totalVertices = verticesPerPatch * 8
    val numConnectionQuads = (4 + 4 + 4) * (verticesPerLine - 1) + 6
    val totalIndices = (trianglesPerPatch * 8 + numConnectionQuads * 2) * 3

    val positions = ByteBuffer.allocate(totalVertices * kOctaspherePositionSize).order(ByteOrder.nativeOrder())
    val tangents = ByteBuffer.allocate(totalVertices * kOctasphereTangentSize).order(ByteOrder.nativeOrder())
    val triangles = ByteBuffer.allocate(totalIndices * kOctasphereIndexSize).order(ByteOrder.nativeOrder())

    generateUnitSphere(positions, actualSubdivisions)
    applyScale(positions, radius)
    translateCorners(positions, dimensions, radius, actualSubdivisions)
    populateTriangles(triangles, actualSubdivisions)

    assert(!positions.hasRemaining())
    assert(!tangents.hasRemaining())
    assert(!triangles.hasRemaining())

    return OctasphereMesh(totalVertices, totalIndices, positions, tangents, triangles)
}

private fun generateUnitSphere(positions: ByteBuffer, subdivisions: Int) {
    val divisions = clamp(subdivisions, 0, kOctasphereMaxSubdivisions)
    val verticesPerLine = 1.shl(divisions) + 1
    for (i in 0 until verticesPerLine) {
        val theta = FPI * 0.5f * i / (verticesPerLine - 1)
        val pointA = Float3(0f, sin(theta), cos(theta))
        val pointB = Float3(cos(theta), sin(theta), 0f)
        val numSegments = verticesPerLine - 1 - i
        writeGeodesic(positions, pointA, pointB, numSegments)
    }
}

private fun applyScale(positions: ByteBuffer, scale: Float) {
    if (scale == 1f) {
        return
    }
    // TODO
}

private fun translateCorners(positions: ByteBuffer, dimensions: Float3, radius: Float, subdivisions: Int) {
    val r2 = radius * 2
    val width = max(dimensions.x, r2)
    val height = max(dimensions.y, r2)
    val depth = max(dimensions.z, r2)
    val tx = (width - r2) / 2
    val ty = (height - r2) / 2
    val tz = (depth - r2) / 2
    // TODO
}

private fun populateTriangles(triangles: ByteBuffer, subdivisions: Int) {
    val verticesPerLine = 1.shl(subdivisions) + 1
    var j0 = 0
    for (colIndex in 0 until verticesPerLine - 1) {
        val colHeight = verticesPerLine - 1 - colIndex
        val j1 = j0 + 1
        val j2 = j0 + colHeight + 1
        val j3 = j0 + colHeight + 2
        for (row in 0 until colHeight - 1) {
            triangles.putTriangle(j0 + row, j1 + row, j2 + row)
            triangles.putTriangle(j2 + row, j1 + row, j3 + row)
        }
        val row = colHeight - 1
        triangles.putTriangle(j0 + row, j1 + row, j2 + row)
        j0 = j2
    }
}

/** Generates a point sequence along the surface of a unit sphere from pointA to pointB. */
private fun writeGeodesic(positions: ByteBuffer, pointA: Float3, pointB: Float3, numSegments: Int) {
    positions.put(pointA)
    if (numSegments == 0) {
        return
    }
    val angleBetweenEndpoints = acos(dot(pointA, pointB))
    val delta = angleBetweenEndpoints / numSegments.toFloat()
    val axis = cross(pointA, pointB)
    for (i in 1 until numSegments) {
        val q = quaternionFromRotation(axis, delta * i)
        val v = quaternionRotateVector(q, pointA)
        positions.put(v)
    }
    positions.put(pointB)
}

private fun ByteBuffer.put(v: Float3): ByteBuffer {
    putFloat(v.x)
    putFloat(v.y)
    putFloat(v.z)
    return this
}

fun ByteBuffer.putTriangle(a: Int, b: Int, c: Int): ByteBuffer {
    assert(a <= Short.MAX_VALUE)
    assert(b <= Short.MAX_VALUE)
    assert(c <= Short.MAX_VALUE)
    putShort(a.toShort())
    putShort(b.toShort())
    putShort(c.toShort())
    return this
}

private fun quaternionFromRotation(axis: Float3, radians: Float): Float4 {
    return Float4(axis * sin(0.5f * radians), cos(0.5f * radians))
}

private fun quaternionRotateVector(quaternion: Float4, src: Float3): Float3 {
    val t = cross(quaternion.xyz, src) * 2.0f
    val p = cross(quaternion.xyz, t)
    return t * quaternion.w + src + p
}