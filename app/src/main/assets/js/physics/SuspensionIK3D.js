/**
 * SuspensionIK3D.js
 * Implements real-time mechanical suspension and Inverse Kinematics (IK)
 * to compute dynamic spring compression and limb angles as the robot moves.
 */

class SuspensionIK3D {
    constructor() {
        this.shockCompression = 0.0;
        this.bouncePhase = 0.0;
        this.terrainHeightOffset = 0.0;
        this.damping = 0.85;
        this.stiffness = 15.0; // Spring constant
        this.velocity = 0.0;
    }

    /**
     * Calculates the local spring compression based on speed, bumps, and hazard types.
     */
    update(delta, speed, hazardType, progress, currentY) {
        this.bouncePhase += delta * speed * 8.0;

        // Base bumpy road frequency simulation
        let targetCompression = 0.0;

        if (speed > 0.02) {
            // High frequency road noise
            targetCompression = Math.sin(this.bouncePhase) * 0.08 * (speed / 1.5);
            
            // Large hazard stepping bumps
            if (hazardType === 'lava' && progress > 0.3 && progress < 0.7) {
                targetCompression += Math.sin(this.bouncePhase * 1.5) * 0.15;
            } else if (hazardType === 'river') {
                // Wave bobbing frequency
                targetCompression += Math.cos(this.bouncePhase * 0.5) * 0.22;
            } else if (hazardType === 'wall' && progress > 0.45 && progress < 0.55) {
                // Hard bump near barricade
                targetCompression += 0.35;
            }
        }

        // Apply simple Hooke's Law mass-spring-damper physics
        const force = -this.stiffness * (this.shockCompression - targetCompression);
        this.velocity += force * delta;
        this.velocity *= this.damping;
        this.shockCompression += this.velocity * delta;

        // Ensure compression bounds
        this.shockCompression = Math.max(-0.4, Math.min(0.5, this.shockCompression));
        return this.shockCompression;
    }

    /**
     * Computes 2-joint leg Inverse Kinematics angles for standard mechanical legs
     * Returns thigh rotation and knee rotation to keep the foot planted.
     * thighLength: length of upper leg
     * shinLength: length of lower leg
     * targetY: relative distance from hip to ground
     */
    solve2DJointIK(thighLength, shinLength, targetY) {
        // Limit targetY to avoid divide-by-zero or imaginary angles
        const maxReach = (thighLength + shinLength) * 0.99;
        const minReach = Math.abs(thighLength - shinLength) * 1.01;
        const clampedY = Math.max(minReach, Math.min(maxReach, targetY));

        // Using Law of Cosines to solve joint angles
        // clampedY^2 = thigh^2 + shin^2 - 2*thigh*shin*cos(kneeAngle)
        const cosKnee = (thighLength * thighLength + shinLength * shinLength - clampedY * clampedY) / 
                          (2 * thighLength * shinLength);
        const kneeRad = Math.acos(Math.max(-1, Math.min(1, cosKnee)));

        // Solving thigh angle relative to vertical axis
        const cosThigh = (thighLength * thighLength + clampedY * clampedY - shinLength * shinLength) / 
                           (2 * thighLength * clampedY);
        const thighRad = Math.acos(Math.max(-1, Math.min(1, cosThigh)));

        return {
            thighAngle: thighRad, // radians to rotate thigh forward/backward
            kneeAngle: Math.PI - kneeRad    // radians to rotate knee back
        };
    }
}
