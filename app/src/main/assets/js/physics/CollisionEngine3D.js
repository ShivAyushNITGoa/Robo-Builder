// CollisionEngine3D.js - Checks 3D intersection bounds of actors
class CollisionEngine3D {
    static checkAABBCollision(box1, box2) {
        return (box1.min.x <= box2.max.x && box1.max.x >= box2.min.x) &&
               (box1.min.y <= box2.max.y && box1.max.y >= box2.min.y) &&
               (box1.min.z <= box2.max.z && box1.max.z >= box2.min.z);
    }

    static checkSphereCollision(p1, r1, p2, r2) {
        const dx = p1.x - p2.x;
        const dy = p1.y - p2.y;
        const dz = p1.z - p2.z;
        const distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return distance < (r1 + r2);
    }
}
