/**
 * DebrisExplosion3D.js
 * Dedicated Three.js procedural debris and circuit particle engine.
 * Spawns physically simulated exploding chunks of metal plate, circuitry, and sparks.
 */

class DebrisExplosion3D {
    constructor(scene) {
        this.scene = scene;
        this.activeDebris = [];
        this.materials = {
            metal: new THREE.MeshStandardMaterial({
                color: 0x8A95A5,
                roughness: 0.3,
                metalness: 0.9,
                side: THREE.DoubleSide
            }),
            goldPlated: new THREE.MeshStandardMaterial({
                color: 0xFFD700,
                roughness: 0.15,
                metalness: 0.85,
                side: THREE.DoubleSide
            }),
            circuit: new THREE.MeshStandardMaterial({
                color: 0x107C41,
                roughness: 0.6,
                metalness: 0.2,
                side: THREE.DoubleSide
            }),
            spark: new THREE.MeshBasicMaterial({
                color: 0x00FFFF,
                transparent: true,
                blending: THREE.AdditiveBlending
            })
        };
    }

    /**
     * Spawns a physical explosion at specified coordinate
     */
    explode(x, y, z, count = 25) {
        // 1. Metal plates debris (rectangles)
        const plateGeo = new THREE.BoxGeometry(0.35, 0.25, 0.05);
        for (let i = 0; i < count * 0.4; i++) {
            const mesh = new THREE.Mesh(plateGeo, Math.random() > 0.4 ? this.materials.metal : this.materials.goldPlated);
            mesh.position.set(x, y, z);
            
            // Random orientation
            mesh.rotation.set(Math.random() * Math.PI, Math.random() * Math.PI, Math.random() * Math.PI);
            this.scene.add(mesh);

            this.activeDebris.push({
                mesh: mesh,
                type: 'plate',
                velocity: new THREE.Vector3(
                    (Math.random() - 0.5) * 6,
                    (Math.random() * 4) + 2,
                    (Math.random() - 0.5) * 6
                ),
                rotVelocity: new THREE.Vector3(
                    (Math.random() - 0.5) * 8,
                    (Math.random() * 8),
                    (Math.random() - 0.5) * 8
                ),
                life: 1.0 + Math.random() * 0.5,
                decay: 0.5 + Math.random() * 0.5
            });
        }

        // 2. Circuit board chunks (squares/triangles)
        const circuitGeo = new THREE.BoxGeometry(0.2, 0.2, 0.04);
        for (let i = 0; i < count * 0.3; i++) {
            const mesh = new THREE.Mesh(circuitGeo, this.materials.circuit);
            mesh.position.set(x, y + 0.2, z);
            mesh.rotation.set(Math.random() * Math.PI, Math.random() * Math.PI, Math.random() * Math.PI);
            this.scene.add(mesh);

            this.activeDebris.push({
                mesh: mesh,
                type: 'circuit',
                velocity: new THREE.Vector3(
                    (Math.random() - 0.5) * 5,
                    (Math.random() * 3) + 3,
                    (Math.random() - 0.5) * 5
                ),
                rotVelocity: new THREE.Vector3(
                    (Math.random() - 0.5) * 12,
                    (Math.random() * 12),
                    (Math.random() - 0.5) * 12
                ),
                life: 0.8 + Math.random() * 0.4,
                decay: 0.7 + Math.random() * 0.4
            });
        }

        // 3. Hot wiring sparks (thin glowing cylinders)
        const sparkGeo = new THREE.CylinderGeometry(0.02, 0.02, 0.4, 4);
        for (let i = 0; i < count * 0.3; i++) {
            const mat = this.materials.spark.clone();
            mat.color.setHex(Math.random() > 0.5 ? 0x00FFFF : 0xFF00FF);
            const mesh = new THREE.Mesh(sparkGeo, mat);
            mesh.position.set(x, y + 0.1, z);
            mesh.rotation.set(Math.random() * Math.PI, Math.random() * Math.PI, Math.random() * Math.PI);
            this.scene.add(mesh);

            this.activeDebris.push({
                mesh: mesh,
                type: 'spark',
                velocity: new THREE.Vector3(
                    (Math.random() - 0.5) * 8,
                    (Math.random() * 6) + 1,
                    (Math.random() - 0.5) * 8
                ),
                rotVelocity: new THREE.Vector3(
                    (Math.random() - 0.5) * 15,
                    (Math.random() * 15),
                    (Math.random() - 0.5) * 15
                ),
                life: 0.5 + Math.random() * 0.3,
                decay: 1.2 + Math.random() * 0.6
            });
        }
    }

    /**
     * Updates physics, gravity, rotation and decay for debris pieces
     */
    update(delta) {
        const gravity = -9.8;
        
        for (let i = this.activeDebris.length - 1; i >= 0; i--) {
            const d = this.activeDebris[i];
            
            // Apply physics forces
            d.velocity.y += gravity * delta;
            d.mesh.position.addScaledVector(d.velocity, delta);
            
            // Apply spin
            d.mesh.rotation.x += d.rotVelocity.x * delta;
            d.mesh.rotation.y += d.rotVelocity.y * delta;
            d.mesh.rotation.z += d.rotVelocity.z * delta;
            
            // Decelerate life
            d.life -= d.decay * delta;
            
            // Ground collision bounce
            if (d.mesh.position.y < 0.1) {
                d.mesh.position.y = 0.1;
                d.velocity.y = -d.velocity.y * 0.4; // bouncy
                d.velocity.x *= 0.7;
                d.velocity.z *= 0.7;
                d.rotVelocity.multiplyScalar(0.5);
            }

            // Shrink or fade
            if (d.type === 'spark') {
                d.mesh.material.opacity = Math.max(0, d.life);
            } else {
                const scale = Math.max(0, d.life);
                d.mesh.scale.set(scale, scale, scale);
            }

            // Clean up decayed pieces
            if (d.life <= 0) {
                this.scene.remove(d.mesh);
                if (d.type === 'spark') {
                    d.mesh.material.dispose();
                }
                this.activeDebris.splice(i, 1);
            }
        }
    }

    /**
     * Clear all current debris
     */
    clear() {
        this.activeDebris.forEach(d => {
            this.scene.remove(d.mesh);
        });
        this.activeDebris = [];
    }
}
