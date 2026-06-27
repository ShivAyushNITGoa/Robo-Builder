package com.example.engine.three_d

data class BoundingBox3D(
    val minX: Float, val maxX: Float,
    val minY: Float, val maxY: Float,
    val minZ: Float, val maxZ: Float
) {
    fun intersects(other: BoundingBox3D): Boolean {
        return (minX <= other.maxX && maxX >= other.minX) &&
               (minY <= other.maxY && maxY >= other.minY) &&
               (minZ <= other.maxZ && maxZ >= other.minZ)
    }
}

class PhysicsEngine3D {
    var posX = 0f
    var posY = 0f
    var posZ = 0f

    var velX = 0f
    var velY = 0f
    var velZ = 0f

    var accX = 0f
    var accY = 0f
    var accZ = 0f

    var dragCoefficient = 0.02f
    var gravity = 0f

    fun resetForces() {
        accX = 0f
        accY = gravity
        accZ = 0f
    }

    fun applyThrust(tx: Float, ty: Float, tz: Float) {
        accX += tx
        accY += ty
        accZ += tz
    }

    fun update(deltaTime: Float) {
        // Apply vacuum drag
        velX += accX * deltaTime - dragCoefficient * velX
        velY += accY * deltaTime - dragCoefficient * velY
        velZ += accZ * deltaTime - dragCoefficient * velZ

        posX += velX * deltaTime
        posY += velY * deltaTime
        posZ += velZ * deltaTime
    }

    fun getPosition(): Point3D {
        return Point3D(posX, posY, posZ)
    }

    fun setPosition(x: Float, y: Float, z: Float) {
        posX = x
        posY = y
        posZ = z
    }

    fun setVelocity(x: Float, y: Float, z: Float) {
        velX = x
        velY = y
        velZ = z
    }

    companion object {
        fun checkCollision(boxA: BoundingBox3D, boxB: BoundingBox3D): Boolean {
            return boxA.intersects(boxB)
        }

        fun getAABBForObject(center: Point3D, sizeX: Float, sizeY: Float, sizeZ: Float): BoundingBox3D {
            val hx = sizeX / 2f
            val hy = sizeY / 2f
            val hz = sizeZ / 2f
            return BoundingBox3D(
                minX = center.x - hx, maxX = center.x + hx,
                minY = center.y - hy, maxY = center.y + hy,
                minZ = center.z - hz, maxZ = center.z + hz
            )
        }
    }
}
