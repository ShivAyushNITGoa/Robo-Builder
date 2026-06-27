// ParticleEmitter3D.js - WebGL-based particle stream controller
class ParticleEmitter3D {
    constructor(scene, colorHex) {
        this.scene = scene;
        this.particles = [];
        this.geometry = new THREE.BufferGeometry();
        this.material = new THREE.PointsMaterial({
            color: new THREE.Color(colorHex || 0x00f0ff),
            size: 2.0,
            transparent: true,
            opacity: 0.8,
            blending: THREE.AdditiveBlending
        });
        this.points = new THREE.Points(this.geometry, this.material);
        this.scene.add(this.points);
    }

    spawnParticle(originX, originY, originZ) {
        this.particles.push({
            x: originX, y: originY, z: originZ,
            vx: (Math.random() - 0.5) * 4,
            vy: (Math.random() - 0.5) * 4,
            vz: (Math.random() - 0.5) * 4 - 5, // drift back
            life: 30 + Math.random() * 20,
            maxLife: 50
        });
    }

    update() {
        const positions = [];
        this.particles = this.particles.filter(p => {
            p.x += p.vx;
            p.y += p.vy;
            p.z += p.vz;
            p.life--;
            if (p.life > 0) {
                positions.push(p.x, p.y, p.z);
                return true;
            }
            return false;
        });

        const vertices = new Float32Array(positions);
        this.geometry.setAttribute('position', new THREE.BufferAttribute(vertices, 3));
        this.geometry.attributes.position.needsUpdate = true;
    }
}
