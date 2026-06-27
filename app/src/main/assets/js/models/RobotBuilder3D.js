// RobotBuilder3D.js - Procedural Three.js mesh builder for customization bay
class RobotBuilder3D {
    static buildRobot(legs, leftArm, rightArm, utility, colorHex) {
        const group = new THREE.Group();
        const mainColor = new THREE.Color(colorHex || 0x00f0ff);

        // Core Torso
        const torsoGeom = new THREE.BoxGeometry(20, 20, 20);
        const torsoMat = new THREE.MeshPhongMaterial({ color: mainColor, flatShading: true });
        const torso = new THREE.Mesh(torsoGeom, torsoMat);
        group.add(torso);

        // Visor glass
        const visorGeom = new THREE.BoxGeometry(12, 5, 2);
        const visorMat = new THREE.MeshBasicMaterial({ color: 0xff0055 });
        const visor = new THREE.Mesh(visorGeom, visorMat);
        visor.position.set(0, 4, 10.5);
        group.add(visor);

        // Legs configuration
        if (legs === "Treads" || legs === "Tracks") {
            const treadGeom = new THREE.BoxGeometry(6, 10, 24);
            const treadMat = new THREE.MeshPhongMaterial({ color: 0x334155 });
            const leftTread = new THREE.Mesh(treadGeom, treadMat);
            leftTread.position.set(-14, -10, 0);
            const rightTread = leftTread.clone();
            rightTread.position.x = 14;
            group.add(leftTread);
            group.add(rightTread);
        } else {
            // Standard biped
            const legGeom = new THREE.CylinderGeometry(2, 2, 14);
            const legMat = new THREE.MeshPhongMaterial({ color: 0x475569 });
            const leftLeg = new THREE.Mesh(legGeom, legMat);
            leftLeg.position.set(-8, -14, 0);
            const rightLeg = leftLeg.clone();
            rightLeg.position.x = 8;
            group.add(leftLeg);
            group.add(rightLeg);
        }

        // Left Arm
        const armGeom = new THREE.BoxGeometry(4, 16, 4);
        const armMat = new THREE.MeshPhongMaterial({ color: 0x64748b });
        const leftArmMesh = new THREE.Mesh(armGeom, armMat);
        leftArmMesh.position.set(-13, 0, 0);
        group.add(leftArmMesh);

        // Right Arm
        const rightArmMesh = leftArmMesh.clone();
        rightArmMesh.position.x = 13;
        group.add(rightArmMesh);

        return group;
    }
}
