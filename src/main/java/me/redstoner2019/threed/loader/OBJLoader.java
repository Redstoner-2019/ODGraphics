package me.redstoner2019.threed.loader;

import me.redstoner2019.graphics.texture.Texture;
import me.redstoner2019.threed.model.Face;
import me.redstoner2019.threed.model.Mesh;
import me.redstoner2019.threed.model.Model;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class OBJLoader {

    public static Model loadWithMaterials(String objPath, String mtlPath, String textureFolder) throws IOException {
        Map<String, Texture> materials = MTLLoader.loadMTL(mtlPath, textureFolder);

        List<Vector3f> positions = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Vector2f> texCoords = new ArrayList<>();
        Map<String, List<Face>> materialFaces = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(objPath))) {
            String line;
            String currentMaterial = "default";
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("v ")) {
                    String[] tokens = line.trim().split("\s+");
                    positions.add(new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    ));
                } else if (line.startsWith("vn ")) {
                    String[] tokens = line.trim().split("\s+");
                    normals.add(new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3])
                    ));
                } else if (line.startsWith("vt ")) {
                    String[] tokens = line.trim().split("\s+");
                    texCoords.add(new Vector2f(
                            Float.parseFloat(tokens[1]),
                            1 - Float.parseFloat(tokens[2]) // Flip V
                    ));
                } else if (line.startsWith("usemtl ")) {
                    currentMaterial = line.split("\s+")[1];
                } else if (line.startsWith("f ")) {
                    String[] tokens = line.trim().split("\s+");
                    Face face = new Face(List.of(tokens[1], tokens[2], tokens[3]));
                    materialFaces.computeIfAbsent(currentMaterial, k -> new ArrayList<>()).add(face);
                }
            }
        }

        Vector3f defaultColor = new Vector3f(1, 1, 1);
        return buildModelFromFaces(materialFaces, positions, texCoords, normals, materials, defaultColor);
    }

    private static Model buildModelFromFaces(
            Map<String, List<Face>> materialFaces,
            List<Vector3f> positions,
            List<Vector2f> texCoords,
            List<Vector3f> normals,
            Map<String, Texture> materialTextures,
            Vector3f defaultColor
    ) {
        Model parentModel = new Model(new Mesh(new float[0], new float[0], new float[0], new int[0]), defaultColor);

        for (Map.Entry<String, List<Face>> entry : materialFaces.entrySet()) {
            String materialName = entry.getKey();
            List<Face> faces = entry.getValue();

            List<float[]> finalPositions = new ArrayList<>();
            List<float[]> finalNormals = new ArrayList<>();
            List<float[]> finalTexCoords = new ArrayList<>();
            List<Integer> indices = new ArrayList<>();
            Map<String, Integer> vertexMap = new HashMap<>();

            for (Face face : faces) {
                for (String key : face.vertices) {
                    if (!vertexMap.containsKey(key)) {
                        int posIndex;
                        int texIndex = 0;
                        int normIndex = 0;

                        if (key.contains("//")) {
                            String[] parts = key.split("//");
                            posIndex = Integer.parseInt(parts[0]) - 1;
                            normIndex = Integer.parseInt(parts[1]) - 1;
                        } else if (key.contains("/")) {
                            String[] parts = key.split("/");
                            posIndex = Integer.parseInt(parts[0]) - 1;
                            if (!parts[1].isEmpty()) {
                                texIndex = Integer.parseInt(parts[1]) - 1;
                            }
                            if (parts.length == 3 && !parts[2].isEmpty()) {
                                normIndex = Integer.parseInt(parts[2]) - 1;
                            }
                        } else {
                            posIndex = Integer.parseInt(key) - 1;
                        }

                        Vector3f pos = positions.get(posIndex);
                        Vector2f tex = texCoords.size() > texIndex ? texCoords.get(texIndex) : new Vector2f(0, 0);
                        Vector3f norm = normals.size() > normIndex ? normals.get(normIndex) : new Vector3f(0, 1, 0);

                        finalPositions.add(new float[]{pos.x, pos.y, pos.z});
                        finalTexCoords.add(new float[]{tex.x, tex.y});
                        finalNormals.add(new float[]{norm.x, norm.y, norm.z});

                        vertexMap.put(key, finalPositions.size() - 1);
                    }
                    indices.add(vertexMap.get(key));
                }
            }

            float[] posArray = new float[finalPositions.size() * 3];
            float[] normArray = new float[finalNormals.size() * 3];
            float[] texCoordArray = new float[finalTexCoords.size() * 2];

            for (int i = 0; i < finalPositions.size(); i++) {
                float[] p = finalPositions.get(i);
                float[] n = finalNormals.get(i);
                float[] t = finalTexCoords.get(i);

                posArray[i * 3] = p[0];
                posArray[i * 3 + 1] = p[1];
                posArray[i * 3 + 2] = p[2];

                normArray[i * 3] = n[0];
                normArray[i * 3 + 1] = n[1];
                normArray[i * 3 + 2] = n[2];

                texCoordArray[i * 2] = t[0];
                texCoordArray[i * 2 + 1] = t[1];
            }

            int[] idxArray = indices.stream().mapToInt(i -> i).toArray();
            Mesh mesh = new Mesh(posArray, normArray, texCoordArray, idxArray);

            Texture tex = materialTextures.get(materialName);
            Model subModel = tex != null ? new Model(mesh, tex) : new Model(mesh, defaultColor);
            parentModel.addChild(materialName, subModel);
        }

        return parentModel;
    }
}
