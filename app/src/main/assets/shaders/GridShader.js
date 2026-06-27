// GridShader.js - Custom vertex & fragment shader templates for neon grid grounds
const GridShader = {
    vertexShader: `
        varying vec3 vWorldPosition;
        void main() {
            vec4 worldPosition = modelMatrix * vec4(position, 1.0);
            vWorldPosition = worldPosition.xyz;
            gl_Position = projectionMatrix * viewMatrix * worldPosition;
        }
    `,
    fragmentShader: `
        varying vec3 vWorldPosition;
        void main() {
            float coordX = vWorldPosition.x;
            float coordZ = vWorldPosition.z;
            float gridX = abs(fract(coordX / 20.0 - 0.5) - 0.5) / fwidth(coordX / 20.0);
            float gridZ = abs(fract(coordZ / 20.0 - 0.5) - 0.5) / fwidth(coordZ / 20.0);
            float line = min(gridX, gridZ);
            float color = 1.0 - min(line, 1.0);
            gl_FragColor = vec4(0.0, 0.94, 1.0, color * 0.4);
        }
    `
};
