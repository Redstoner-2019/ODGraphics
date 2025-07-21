package me.redstoner2019.threed.model;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Shapes {
    public static Model createSphere(float radius, int sectors, int stacks, Vector3f color) {
        return new Model(createSphereMesh(radius, sectors, stacks), color);
    }

    public static Mesh createSphereMesh(float radius, int sectors, int stacks) {
        List<Float> positions = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        for (int i = 0; i <= stacks; ++i) {
            double stackAngle = Math.PI / 2 - i * Math.PI / stacks; // from pi/2 to -pi/2
            double xy = radius * Math.cos(stackAngle);
            double z = radius * Math.sin(stackAngle);

            for (int j = 0; j <= sectors; ++j) {
                double sectorAngle = j * 2 * Math.PI / sectors;

                float x = (float)(xy * Math.cos(sectorAngle));
                float y = (float)(xy * Math.sin(sectorAngle));
                positions.add(x);
                positions.add(y);
                positions.add((float)z);

                float length = (float)Math.sqrt(x * x + y * y + z * z);
                normals.add(x / length);
                normals.add(y / length);
                normals.add((float)z / length);
            }
        }

        int k1, k2;
        for (int i = 0; i < stacks; ++i) {
            k1 = i * (sectors + 1);
            k2 = k1 + sectors + 1;

            for (int j = 0; j < sectors; ++j, ++k1, ++k2) {
                if (i != 0) {
                    indices.add(k1);
                    indices.add(k2);
                    indices.add(k1 + 1);
                }

                if (i != (stacks - 1)) {
                    indices.add(k1 + 1);
                    indices.add(k2);
                    indices.add(k2 + 1);
                }
            }
        }

        return new Mesh(
                toFloatArray(positions),
                toFloatArray(normals),
                indices.stream().mapToInt(i -> i).toArray()
        );
    }

    public static Model createCylinder(float radius, float height, int segments, Vector3f color) {
        return new Model(createCylinderMesh(radius, height, segments), color);
    }

    public static Mesh createCylinderMesh(float radius, float height, int segments) {
        List<Float> positions = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        float halfHeight = height / 2f;

        for (int i = 0; i <= segments; ++i) {
            float angle = (float)(2 * Math.PI * i / segments);
            float x = (float)Math.cos(angle);
            float z = (float)Math.sin(angle);

            // bottom
            positions.add(x * radius);
            positions.add(-halfHeight);
            positions.add(z * radius);
            normals.add(x);
            normals.add(0f);
            normals.add(z);

            // top
            positions.add(x * radius);
            positions.add(halfHeight);
            positions.add(z * radius);
            normals.add(x);
            normals.add(0f);
            normals.add(z);
        }

        // side indices
        for (int i = 0; i < segments * 2; i += 2) {
            indices.add(i);
            indices.add(i + 1);
            indices.add(i + 3);

            indices.add(i);
            indices.add(i + 3);
            indices.add(i + 2);
        }

        return new Mesh(
                toFloatArray(positions),
                toFloatArray(normals),
                indices.stream().mapToInt(i -> i).toArray()
        );
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) array[i] = list.get(i);
        return array;
    }

    // Creates a flat plane centered at origin, on XZ plane
    public static Model createPlane(float size, Vector3f color) {
        return new Model(createPlaneMesh(size), color);
    }

    public static Mesh createPlaneMesh(float size) {
        float half = size / 2f;

        float[] positions = {
                -half, 0, -half,
                half, 0, -half,
                half, 0,  half,
                -half, 0,  half
        };

        float[] normals = {
                0, 1, 0,
                0, 1, 0,
                0, 1, 0,
                0, 1, 0
        };

        int[] indices = {
                0, 1, 2,
                2, 3, 0
        };

        return new Mesh(positions, normals, indices);
    }

    // Creates a cube centered at origin
    public static Model createCube(Vector3f color) {
        return new Model(createCubeMesh(), color);
    }

    public static Mesh createCubeMesh() {
        float size = 0.5f;

        float[] positions = {
                // Front
                -size, -size,  size,
                size, -size,  size,
                size,  size,  size,
                -size,  size,  size,

                // Back
                -size, -size, -size,
                -size,  size, -size,
                size,  size, -size,
                size, -size, -size,

                // Left
                -size, -size, -size,
                -size, -size,  size,
                -size,  size,  size,
                -size,  size, -size,

                // Right
                size, -size, -size,
                size,  size, -size,
                size,  size,  size,
                size, -size,  size,

                // Top
                -size,  size, -size,
                -size,  size,  size,
                size,  size,  size,
                size,  size, -size,

                // Bottom
                -size, -size, -size,
                size, -size, -size,
                size, -size,  size,
                -size, -size,  size,
        };

        float[] normals = {
                // Front
                0, 0, 1,  0, 0, 1,  0, 0, 1,  0, 0, 1,
                // Back
                0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,
                // Left
                -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
                // Right
                1, 0, 0,  1, 0, 0,  1, 0, 0,  1, 0, 0,
                // Top
                0, 1, 0,  0, 1, 0,  0, 1, 0,  0, 1, 0,
                // Bottom
                0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0
        };

        int[] indices = {
                0, 1, 2, 2, 3, 0,       // Front
                4, 5, 6, 6, 7, 4,       // Back
                8, 9,10,10,11, 8,       // Left
                12,13,14,14,15,12,      // Right
                16,17,18,18,19,16,      // Top
                20,21,22,22,23,20       // Bottom
        };

        return new Mesh(positions, normals, indices);
    }
}
