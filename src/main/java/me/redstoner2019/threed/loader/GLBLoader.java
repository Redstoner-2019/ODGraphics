package me.redstoner2019.threed.loader;

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import me.redstoner2019.graphics.texture.Texture;
import me.redstoner2019.threed.model.Mesh;
import me.redstoner2019.threed.model.Model;
import org.joml.Vector3f;

import java.io.File;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class GLBLoader {
    public static GltfModel load(URI uri) throws Exception {
        return new GltfModelReader().read(uri);
    }

    public static Model load(String path) throws Exception {
        GltfModel gltfModel = new GltfModelReader().read(new File(path).toURI());
        List<MeshModel> meshModels = gltfModel.getMeshModels();

        for (MeshModel meshModel : meshModels) {
            for (MeshPrimitiveModel primitiveModel : meshModel.getMeshPrimitiveModels()) {
                float[] positions = extractFloatArray(primitiveModel.getAttributes().get("POSITION"));
                float[] normals = extractFloatArray(primitiveModel.getAttributes().get("NORMAL"));
                float[] texCoords = primitiveModel.getAttributes().containsKey("TEXCOORD_0")
                        ? extractFloatArray(primitiveModel.getAttributes().get("TEXCOORD_0")) : null;

                int[] indices = extractIntArray(primitiveModel.getIndices());

                Mesh mesh = new Mesh(positions, normals, texCoords, indices);

                // Skip texture loading for now to avoid crashes
                // Just return model with default color
                return new Model(mesh, new Vector3f(1, 1, 1));
            }
        }

        throw new RuntimeException("No mesh found in glTF file: " + path);
    }

    // Alternative safer texture loading method
    public static Model loadWithSafeTextures(String path) throws Exception {
        GltfModel gltfModel = new GltfModelReader().read(new File(path).toURI());
        List<MeshModel> meshModels = gltfModel.getMeshModels();

        for (MeshModel meshModel : meshModels) {
            for (MeshPrimitiveModel primitiveModel : meshModel.getMeshPrimitiveModels()) {
                float[] positions = extractFloatArray(primitiveModel.getAttributes().get("POSITION"));
                float[] normals = extractFloatArray(primitiveModel.getAttributes().get("NORMAL"));
                float[] texCoords = primitiveModel.getAttributes().containsKey("TEXCOORD_0")
                        ? extractFloatArray(primitiveModel.getAttributes().get("TEXCOORD_0")) : null;

                int[] indices = extractIntArray(primitiveModel.getIndices());

                Mesh mesh = new Mesh(positions, normals, texCoords, indices);

                Texture texture = null;
                try {
                    // Attempt safe texture loading
                    List<TextureModel> allTextures = gltfModel.getTextureModels();
                    
                    if (allTextures != null && !allTextures.isEmpty()) {
                        TextureModel textureModel = allTextures.get(0);
                        if (textureModel != null) {
                            ImageModel imageModel = textureModel.getImageModel();
                            if (imageModel != null) {
                                // Only try external files for now, skip embedded data
                                if (imageModel.getUri() != null && !imageModel.getUri().startsWith("data:")) {
                                    String basePath = new File(path).getParent() + "/";
                                    String texturePath = basePath + imageModel.getUri();
                                    File textureFile = new File(texturePath);
                                    
                                    if (textureFile.exists() && textureFile.isFile()) {
                                        // Use file path instead of ByteBuffer to avoid memory issues
                                        texture = Texture.loadTexture(texturePath);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load texture safely: " + e.getMessage());
                    // Continue without texture
                }

                return texture != null
                        ? new Model(mesh, texture)
                        : new Model(mesh, new Vector3f(1, 1, 1));
            }
        }

        throw new RuntimeException("No mesh found in glTF file: " + path);
    }

    private static float[] extractFloatArray(AccessorModel accessor) {
        if (accessor == null) return new float[0];
        
        try {
            FloatBuffer buffer = accessor.getAccessorData().createByteBuffer().asFloatBuffer();
            float[] data = new float[buffer.remaining()];
            buffer.get(data);
            return data;
        } catch (Exception e) {
            System.err.println("Error extracting float array: " + e.getMessage());
            return new float[0];
        }
    }

    private static int[] extractIntArray(AccessorModel accessor) {
        if (accessor == null) return new int[0];
        
        try {
            IntBuffer buffer = accessor.getAccessorData().createByteBuffer().asIntBuffer();
            int[] data = new int[buffer.remaining()];
            buffer.get(data);
            return data;
        } catch (Exception e) {
            System.err.println("Error extracting int array: " + e.getMessage());
            return new int[0];
        }
    }
}