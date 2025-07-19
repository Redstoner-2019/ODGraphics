package me.redstoner2019.threed.loader;

import me.redstoner2019.graphics.math.Vector3f;
import me.redstoner2019.threed.model.Mesh;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OBJLoader {
    public static Mesh load(String filePath) throws IOException {
        List<Vector3f> vertices = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                if (line.startsWith("v ")) {
                    vertices.add(new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    ));
                } else if (line.startsWith("vn ")) {
                    normals.add(new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    ));
                } else if (line.startsWith("f ")) {
                    for (int i = 1; i <= 3; i++) {
                        String[] parts = tokens[i].split("//"); // v//vn
                        indices.add(Integer.parseInt(parts[0]) - 1);
                    }
                }
            }
        }

        // Flatten
        float[] pos = new float[vertices.size() * 3];
        float[] nor = new float[normals.size() * 3];
        for (int i = 0; i < vertices.size(); i++) {
            Vector3f v = vertices.get(i);
            pos[i * 3] = v.x;
            pos[i * 3 + 1] = v.y;
            pos[i * 3 + 2] = v.z;
        }
        for (int i = 0; i < normals.size(); i++) {
            Vector3f n = normals.get(i);
            nor[i * 3] = n.x;
            nor[i * 3 + 1] = n.y;
            nor[i * 3 + 2] = n.z;
        }

        return new Mesh(pos, nor, indices.stream().mapToInt(i -> i).toArray());
    }
}

