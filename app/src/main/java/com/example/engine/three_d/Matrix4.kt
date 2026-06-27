package com.example.engine.three_d

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class Matrix4 {
    val data = FloatArray(16)

    init {
        setIdentity()
    }

    fun setIdentity() {
        for (i in 0..15) {
            data[i] = if (i % 5 == 0) 1.0f else 0.0f
        }
    }

    fun multiply(other: Matrix4): Matrix4 {
        val result = Matrix4()
        for (row in 0..3) {
            for (col in 0..3) {
                var sum = 0.0f
                for (i in 0..3) {
                    sum += this.data[row * 4 + i] * other.data[i * 4 + col]
                }
                result.data[row * 4 + col] = sum
            }
        }
        return result
    }

    fun transform(p: Point3D): Point3D {
        val w = data[12] * p.x + data[13] * p.y + data[14] * p.z + data[15]
        val rw = if (w == 0f) 1.0f else w
        return Point3D(
            x = (data[0] * p.x + data[1] * p.y + data[2] * p.z + data[3]) / rw,
            y = (data[4] * p.x + data[5] * p.y + data[6] * p.z + data[7]) / rw,
            z = (data[8] * p.x + data[9] * p.y + data[10] * p.z + data[11]) / rw
        )
    }

    companion object {
        fun makeTranslation(tx: Float, ty: Float, tz: Float): Matrix4 {
            val m = Matrix4()
            m.data[3] = tx
            m.data[7] = ty
            m.data[11] = tz
            return m
        }

        fun makeScale(sx: Float, sy: Float, sz: Float): Matrix4 {
            val m = Matrix4()
            m.data[0] = sx
            m.data[5] = sy
            m.data[10] = sz
            return m
        }

        fun makeRotationX(angleRad: Float): Matrix4 {
            val m = Matrix4()
            val c = cos(angleRad)
            val s = sin(angleRad)
            m.data[5] = c
            m.data[6] = -s
            m.data[9] = s
            m.data[10] = c
            return m
        }

        fun makeRotationY(angleRad: Float): Matrix4 {
            val m = Matrix4()
            val c = cos(angleRad)
            val s = sin(angleRad)
            m.data[0] = c
            m.data[2] = s
            m.data[8] = -s
            m.data[10] = c
            return m
        }

        fun makeRotationZ(angleRad: Float): Matrix4 {
            val m = Matrix4()
            val c = cos(angleRad)
            val s = sin(angleRad)
            m.data[0] = c
            m.data[1] = -s
            m.data[4] = s
            m.data[5] = c
            return m
        }

        fun makePerspective(fovDegrees: Float, aspect: Float, near: Float, far: Float): Matrix4 {
            val m = Matrix4()
            val f = 1.0f / tan(Math.toRadians(fovDegrees.toDouble() / 2.0)).toFloat()
            val rangeReciprocal = 1.0f / (near - far)

            m.data[0] = f / aspect
            m.data[5] = f
            m.data[10] = (far + near) * rangeReciprocal
            m.data[11] = 2.0f * far * near * rangeReciprocal
            m.data[14] = -1.0f
            m.data[15] = 0.0f
            return m
        }
    }
}
