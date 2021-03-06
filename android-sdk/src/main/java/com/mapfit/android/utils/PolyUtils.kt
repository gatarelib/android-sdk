@file:JvmName("PolyUtils")

package com.mapfit.android.utils

import com.mapfit.android.geometry.LatLng

/**
 * Decodes an encoded path string into a sequence of [LatLng]s.
 *
 * @param encodedPath
 */
fun decodePolyline(encodedPath: String): List<LatLng> {
    val len = encodedPath.length
    val decodedPath = ArrayList<LatLng>()
    var index = 0
    var lat = 0
    var lng = 0

    while (index < len) {
        var result = 1
        var shift = 0
        var b: Int
        do {
            b = encodedPath[index++].toInt() - 63 - 1
            result += b shl shift
            shift += 5
        } while (b >= 0x1f)
        lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1

        result = 1
        shift = 0
        do {
            b = encodedPath[index++].toInt() - 63 - 1
            result += b shl shift
            shift += 5
        } while (b >= 0x1f)
        lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1

        decodedPath.add(LatLng(lat * 1e-6, lng * 1e-6))
    }

    return decodedPath
}
