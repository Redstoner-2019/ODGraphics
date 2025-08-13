package loader;

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import me.redstoner2019.graphics.texture.Texture;
import me.redstoner2019.threed.model.Mesh;
import me.redstoner2019.threed.model.Model;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.system.MemoryStack.stackPush;

public class GLBLoaderTestNew {
    // Complete rewrite of the load method to handle all meshes and materials properly
    public static Model loadComplete(String path) throws Exception {
        GltfModel gltfModel = new GltfModelReader().read(new File(path).toURI());

        // Create the root model (empty mesh, will contain children)
        Model rootModel = new Model(new Mesh(new float[0], new float[0], new float[0], new int[0]), new Vector3f(1, 1, 1));

        // Process all scenes and nodes
        if (gltfModel.getSceneModels() != null && !gltfModel.getSceneModels().isEmpty()) {
            SceneModel scene = gltfModel.getSceneModels().get(0); // Use first scene

            int childIndex = 0;
            for (NodeModel node : scene.getNodeModels()) {
                childIndex = processNodeComplete(node, rootModel, childIndex, gltfModel, path);
            }
        }

        return rootModel;
    }

    private static int processNodeComplete(NodeModel node, Model parent, int childIndex, GltfModel gltfModel, String path) {
        try {
            // Process all meshes in this node
            for (MeshModel meshModel : node.getMeshModels()) {
                for (int primIndex = 0; primIndex < meshModel.getMeshPrimitiveModels().size(); primIndex++) {
                    MeshPrimitiveModel primitive = meshModel.getMeshPrimitiveModels().get(primIndex);

                    // Create a model for this primitive
                    Model primitiveModel = createModelFromPrimitive(primitive, gltfModel, path);

                    // Add as child with a unique name
                    String childName = "mesh_" + childIndex + "_prim_" + primIndex;
                    parent.addChild(childName, primitiveModel);

                    System.out.println("Added child: " + childName +
                            " with texture: " + (primitiveModel.getTexture() != null ? "YES" : "NO") +
                            " vertices: " + (primitive.getAttributes().get("POSITION") != null ?
                            primitive.getAttributes().get("POSITION").getCount() : 0));
                }
                childIndex++;
            }

            // Recursively process child nodes
            for (NodeModel childNode : node.getChildren()) {
                childIndex = processNodeComplete(childNode, parent, childIndex, gltfModel, path);
            }

        } catch (Exception e) {
            System.err.println("Error processing node: " + e.getMessage());
            e.printStackTrace();
        }

        return childIndex;
    }

    private static Model createModelFromPrimitive(MeshPrimitiveModel primitiveModel, GltfModel gltfModel, String path) {
        try {
            // Extract vertex data
            float[] positions = extractFloatArray(primitiveModel.getAttributes().get("POSITION"));
            float[] normals = extractFloatArray(primitiveModel.getAttributes().get("NORMAL"));
            float[] texCoords = primitiveModel.getAttributes().containsKey("TEXCOORD_0")
                    ? extractFloatArray(primitiveModel.getAttributes().get("TEXCOORD_0")) : null;
            int[] indices = extractIntArray(primitiveModel.getIndices());

            Mesh mesh = new Mesh(positions, normals, texCoords, indices);

            // For now, skip texture loading to avoid crashes - use random colors
            Vector3f randomColor = new Vector3f(
                    (float) Math.random(),
                    (float) Math.random(),
                    (float) Math.random()
            );
            return new Model(mesh, randomColor);

        } catch (Exception e) {
            System.err.println("Error creating model from primitive: " + e.getMessage());
            e.printStackTrace();
            // Return a fallback model
            Mesh fallbackMesh = new Mesh(new float[0], new float[0], new float[0], new int[0]);
            return new Model(fallbackMesh, new Vector3f(1, 0, 0)); // Red for errors
        }
    }

    // Safe version that loads textures without crashing
    public static Model loadCompleteWithTextures(String path) throws Exception {
        GltfModel gltfModel = new GltfModelReader().read(new File(path).toURI());

        // Create the root model (empty mesh, will contain children)
        Model rootModel = new Model(new Mesh(new float[0], new float[0], new float[0], new int[0]), new Vector3f(1, 1, 1));

        // Process all scenes and nodes
        if (gltfModel.getSceneModels() != null && !gltfModel.getSceneModels().isEmpty()) {
            SceneModel scene = gltfModel.getSceneModels().get(0); // Use first scene

            int childIndex = 0;
            for (NodeModel node : scene.getNodeModels()) {
                childIndex = processNodeCompleteWithTextures(node, rootModel, childIndex, gltfModel, path);
            }
        }

        return rootModel;
    }

    private static int processNodeCompleteWithTextures(NodeModel node, Model parent, int childIndex, GltfModel gltfModel, String path) {
        try {
            // Process all meshes in this node
            for (MeshModel meshModel : node.getMeshModels()) {
                for (int primIndex = 0; primIndex < meshModel.getMeshPrimitiveModels().size(); primIndex++) {
                    MeshPrimitiveModel primitive = meshModel.getMeshPrimitiveModels().get(primIndex);

                    // Create a model for this primitive
                    Model primitiveModel = createModelFromPrimitiveWithTextures(primitive, gltfModel, path);

                    // Add as child with a unique name
                    String childName = "mesh_" + childIndex + "_prim_" + primIndex;
                    parent.addChild(childName, primitiveModel);

                    System.out.println("Added child: " + childName +
                            " with texture: " + (primitiveModel.getTexture() != null ? "YES" : "NO") +
                            " vertices: " + (primitive.getAttributes().get("POSITION") != null ?
                            primitive.getAttributes().get("POSITION").getCount() : 0));
                }
                childIndex++;
            }

            // Recursively process child nodes
            for (NodeModel childNode : node.getChildren()) {
                childIndex = processNodeCompleteWithTextures(childNode, parent, childIndex, gltfModel, path);
            }

        } catch (Exception e) {
            System.err.println("Error processing node: " + e.getMessage());
            e.printStackTrace();
        }

        return childIndex;
    }

    private static Model createModelFromPrimitiveWithTextures(MeshPrimitiveModel primitiveModel, GltfModel gltfModel, String path) {
        try {
            // Extract vertex data
            float[] positions = extractFloatArray(primitiveModel.getAttributes().get("POSITION"));
            float[] normals = extractFloatArray(primitiveModel.getAttributes().get("NORMAL"));
            float[] texCoords = primitiveModel.getAttributes().containsKey("TEXCOORD_0")
                    ? extractFloatArray(primitiveModel.getAttributes().get("TEXCOORD_0")) : null;
            int[] indices = extractIntArray(primitiveModel.getIndices());

            Mesh mesh = new Mesh(positions, normals, texCoords, indices);

            // Try to load texture safely
            Texture texture = loadTextureForPrimitiveSafe(primitiveModel, gltfModel, path);

            if (texture != null) {
                return new Model(mesh, texture);
            } else {
                // Use a random color so we can see different parts
                Vector3f randomColor = new Vector3f(
                        (float) Math.random(),
                        (float) Math.random(),
                        (float) Math.random()
                );
                return new Model(mesh, randomColor);
            }

        } catch (Exception e) {
            System.err.println("Error creating model from primitive: " + e.getMessage());
            e.printStackTrace();
            // Return a fallback model
            Mesh fallbackMesh = new Mesh(new float[0], new float[0], new float[0], new int[0]);
            return new Model(fallbackMesh, new Vector3f(1, 0, 0)); // Red for errors
        }
    }

    private static Texture loadTextureForPrimitiveSafe(MeshPrimitiveModel primitiveModel, GltfModel gltfModel, String path) {
        try {
            MaterialModel material = primitiveModel.getMaterialModel();
            if (material == null) {
                System.out.println("No material for this primitive");
                return null;
            }

            // Find the first available texture in the material
            TextureModel textureModel = findFirstTexture(material, gltfModel);

            if (textureModel != null) {
                System.out.println("Found texture for primitive - loading safely...");
                return loadTextureFromModelSafe(textureModel, new File(path).getParent() + "/");
            } else {
                System.out.println("No texture found in material");
                return null;
            }

        } catch (Exception e) {
            System.err.println("Error loading texture for primitive: " + e.getMessage());
            return null;
        }
    }

    // Helper method to find any texture in a material using reflection
    private static TextureModel findFirstTexture(MaterialModel material, GltfModel gltfModel) {
        try {
            // Try to find any texture using reflection
            java.lang.reflect.Method[] methods = material.getClass().getMethods();

            for (java.lang.reflect.Method method : methods) {
                String methodName = method.getName().toLowerCase();
                if ((methodName.contains("texture") || methodName.contains("color")) &&
                        method.getParameterCount() == 0 &&
                        method.getReturnType() != void.class) {

                    try {
                        Object result = method.invoke(material);
                        if (result instanceof TextureModel) {
                            System.out.println("Found texture via method: " + method.getName());
                            return (TextureModel) result;
                        }
                    } catch (Exception e) {
                        // Continue trying other methods
                    }
                }
            }

            // If no texture found via reflection, try to get any texture from the model
            if (gltfModel.getTextureModels() != null && !gltfModel.getTextureModels().isEmpty()) {
                System.out.println("Using first available texture from model");
                return gltfModel.getTextureModels().get(0);
            }

        } catch (Exception e) {
            System.err.println("Error finding texture: " + e.getMessage());
        }

        return null;
    }

    private static Texture loadTextureFromModelSafe(TextureModel textureModel, String basePath) {
        try {
            ImageModel imageModel = textureModel.getImageModel();
            if (imageModel == null) {
                System.out.println("No image model in texture");
                return null;
            }

            // Try embedded data first
            if (imageModel.getImageData() != null) {
                System.out.println("Loading embedded texture data safely...");
                ByteBuffer imageData = imageModel.getImageData();
                return loadTextureSafe(imageData);
            }

            // Try external file
            if (imageModel.getUri() != null && !imageModel.getUri().startsWith("data:")) {
                String texturePath = basePath + imageModel.getUri();
                File textureFile = new File(texturePath);

                if (textureFile.exists() && textureFile.isFile()) {
                    System.out.println("Loading external texture: " + texturePath);
                    return Texture.loadTexture(texturePath);
                }
            }

            System.out.println("Could not load texture - no valid data source");
            return null;

        } catch (Exception e) {
            System.err.println("Error loading texture from model: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Ultra-safe texture loading with extensive validation
    public static Texture loadTextureSafe(ByteBuffer originalBuffer) {
        if (originalBuffer == null) {
            System.err.println("Buffer is null");
            return null;
        }

        if (originalBuffer.remaining() == 0) {
            System.err.println("Buffer is empty");
            return null;
        }

        try {
            // Create a safe copy of the buffer
            ByteBuffer safeBuffer = BufferUtils.createByteBuffer(originalBuffer.remaining());
            
            // Save original position/limit
            int originalPosition = originalBuffer.position();
            int originalLimit = originalBuffer.limit();
            
            // Copy the data byte by byte to ensure safety
            while (originalBuffer.hasRemaining()) {
                safeBuffer.put(originalBuffer.get());
            }
            
            // Restore original buffer state
            originalBuffer.position(originalPosition);
            originalBuffer.limit(originalLimit);
            
            // Prepare safe buffer for reading
            safeBuffer.flip();
            
            System.out.println("Created safe buffer copy, size: " + safeBuffer.remaining() + " bytes");

            try (MemoryStack stack = stackPush()) {
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);

                // Don't flip - GLB textures are usually already in the correct orientation
                STBImage.stbi_set_flip_vertically_on_load(false);

                // Use the safe buffer copy
                ByteBuffer image = STBImage.stbi_load_from_memory(safeBuffer, width, height, channels, 4);

                if (image == null) {
                    String error = STBImage.stbi_failure_reason();
                    System.err.println("Failed to load texture from memory: " + (error != null ? error : "Unknown error"));
                    return null;
                }

                int w = width.get(0);
                int h = height.get(0);
                int c = channels.get(0);

                System.out.println("Successfully loaded texture: " + w + "x" + h + " with " + c + " channels");

                // Validate dimensions
                if (w <= 0 || h <= 0 || w > 8192 || h > 8192) {
                    System.err.println("Invalid texture dimensions: " + w + "x" + h);
                    STBImage.stbi_image_free(image);
                    return null;
                }

                int textureID = GL11.glGenTextures();
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image);
                GL30.glGenerateMipmap(GL_TEXTURE_2D);

                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

                // Free the STB image
                STBImage.stbi_image_free(image);

                Texture texture = new Texture(textureID);
                texture.setWidth(w);
                texture.setHeight(h);

                return texture;

            }

        } catch (Exception e) {
            System.err.println("Exception in safe texture loading: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Helper methods for extracting data from GLTF accessors
    private static float[] extractFloatArray(AccessorModel accessor) {
        if (accessor == null) return new float[0];
        
        FloatBuffer buffer = accessor.getAccessorData().createByteBuffer().asFloatBuffer();
        float[] array = new float[buffer.remaining()];
        buffer.get(array);
        return array;
    }

    private static int[] extractIntArray(AccessorModel accessor) {
        if (accessor == null) return new int[0];
        
        ByteBuffer byteBuffer = accessor.getAccessorData().createByteBuffer();
        int[] result = new int[accessor.getCount()];
        
        // Handle different component types - getComponentType() returns an int
        int componentType = accessor.getComponentType();
        if (componentType == 5123) { // UNSIGNED_SHORT
            for (int i = 0; i < result.length; i++) {
                result[i] = byteBuffer.getShort() & 0xFFFF;
            }
        } else if (componentType == 5125) { // UNSIGNED_INT
            for (int i = 0; i < result.length; i++) {
                result[i] = byteBuffer.getInt();
            }
        } else {
            // Default fallback
            for (int i = 0; i < result.length; i++) {
                result[i] = byteBuffer.getShort() & 0xFFFF;
            }
        }
        
        return result;
    }
}