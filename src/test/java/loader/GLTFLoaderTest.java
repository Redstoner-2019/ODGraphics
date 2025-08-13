package loader;

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import me.redstoner2019.graphics.texture.Texture;
import me.redstoner2019.threed.model.Mesh;
import me.redstoner2019.threed.model.Model;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.net.URI;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.system.MemoryStack.stackPush;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.stb.STBImage;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import java.nio.IntBuffer;
import org.lwjgl.stb.STBImageWrite;


public class GLTFLoaderTest {
    public static GltfModel load(URI uri) throws Exception {
        return new GltfModelReader().read(uri);
    }

    public static Model load(String path) throws Exception {
        GltfModel gltfModel = new GltfModelReader().read(new File(path).toURI());

        // Create root model to hold all children - initialize with default color to avoid null
        Model rootModel = new Model(null, new Vector3f(1, 1, 1));

        // Process the scene graph properly
        List<SceneModel> scenes = gltfModel.getSceneModels();
        if (scenes != null && !scenes.isEmpty()) {
            SceneModel defaultScene = scenes.get(0); // Use first scene
            int meshIndex = 0;

            for (NodeModel rootNode : defaultScene.getNodeModels()) {
                meshIndex = processNode(rootNode, rootModel, meshIndex, gltfModel, path);
            }
        } else {
            // Fallback: process all meshes directly if no scene structure
            List<MeshModel> meshModels = gltfModel.getMeshModels();
            int meshIndex = 0;

            for (MeshModel meshModel : meshModels) {
                for (MeshPrimitiveModel primitiveModel : meshModel.getMeshPrimitiveModels()) {
                    Model childModel = createModelFromPrimitive(primitiveModel, gltfModel, path);
                    rootModel.addChild("mesh_" + meshIndex++, childModel);
                }
            }
        }

        // If root model has no children, throw exception
        if (rootModel.getChildren().isEmpty()) {
            throw new RuntimeException("No mesh found in glTF file: " + path);
        }

        return rootModel;
    }

    private static int processNode(NodeModel node, Model parent, int meshIndex, GltfModel gltfModel, String path) {
        // Process meshes in this node
        if (node.getMeshModels() != null) {
            for (MeshModel meshModel : node.getMeshModels()) {
                for (MeshPrimitiveModel primitiveModel : meshModel.getMeshPrimitiveModels()) {
                    Model childModel = createModelFromPrimitive(primitiveModel, gltfModel, path);
                    parent.addChild("mesh_" + meshIndex++, childModel);
                }
            }
        }

        // Recursively process child nodes
        for (NodeModel childNode : node.getChildren()) {
            meshIndex = processNode(childNode, parent, meshIndex, gltfModel, path);
        }

        return meshIndex;
    }

    private static Model createModelFromPrimitive(MeshPrimitiveModel primitiveModel, GltfModel gltfModel, String path) {
        float[] positions = extractFloatArray(primitiveModel.getAttributes().get("POSITION"));
        float[] normals = extractFloatArray(primitiveModel.getAttributes().get("NORMAL"));
        float[] texCoords = primitiveModel.getAttributes().containsKey("TEXCOORD_0")
                ? extractFloatArray(primitiveModel.getAttributes().get("TEXCOORD_0")) : null;

        int[] indices = extractIntArray(primitiveModel.getIndices());

        System.out.println("Creating mesh with " + positions.length / 3 + " vertices");
        System.out.println("Has texture coordinates: " + (texCoords != null));
        if (texCoords != null) {
            System.out.println("First few texture coordinates: " + texCoords[0] + ", " + texCoords[1]);
        }

        Mesh mesh = new Mesh(positions, normals, texCoords, indices);

        Texture texture = loadTextureForPrimitive(primitiveModel, gltfModel, path);

        if (texture != null) {
            System.out.println("✓ Successfully loaded texture for primitive, texture ID: " + texture.getId());
            Model model = new Model(mesh, texture);
            model.setColor(null);
            System.out.println("✓ Model created with texture: " + (model.getTexture() != null));
            System.out.println("✓ Model texture ID: " + (model.getTexture() != null ? model.getTexture().getId() : "null"));
            return model;
        } else {
            System.out.println("✗ No texture loaded for primitive, using white color");
            return new Model(mesh, new Vector3f(1, 1, 1)); // Use white instead of red to distinguish
        }
    }

    private static Texture loadTextureForPrimitive(MeshPrimitiveModel primitiveModel, GltfModel gltfModel, String path) {
        try {
            // Get material specifically for this primitive
            MaterialModel material = primitiveModel.getMaterialModel();
            if (material != null) {
                System.out.println("Found material for primitive: " + material);

                // Try to find textures associated with this material
                // Since MaterialModel doesn't expose texture getters directly, we need to check
                // if this material uses any of the available textures

                // For now, let's try a different approach - get the material index and match it
                List<MaterialModel> allMaterials = gltfModel.getMaterialModels();
                List<TextureModel> allTextures = gltfModel.getTextureModels();

                if (allMaterials != null && allTextures != null && !allTextures.isEmpty()) {
                    // Find the index of this material
                    int materialIndex = -1;
                    for (int i = 0; i < allMaterials.size(); i++) {
                        if (allMaterials.get(i) == material) {
                            materialIndex = i;
                            break;
                        }
                    }

                    System.out.println("Material index: " + materialIndex + ", available textures: " + allTextures.size());

                    // Use the first available texture for now (you might need to improve this logic)
                    if (!allTextures.isEmpty()) {
                        return loadTextureFromModel(allTextures.get(0), path);
                    }
                }
            } else {
                System.out.println("No material found for primitive - trying first available texture");

                // Fallback: use first available texture
                List<TextureModel> allTextures = gltfModel.getTextureModels();
                if (allTextures != null && !allTextures.isEmpty()) {
                    return loadTextureFromModel(allTextures.get(0), path);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load texture for primitive: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private static Texture loadTextureFromModel(TextureModel textureModel, String basePath) {
        try {
            ImageModel imageModel = textureModel.getImageModel();
            if (imageModel != null) {
                // Handle embedded buffer data (common in GLB files)
                if (imageModel.getImageData() != null) {
                    System.out.println("Loading embedded texture from buffer data");
                    ByteBuffer imageBuffer = imageModel.getImageData();

                    if (imageBuffer != null && imageBuffer.remaining() > 0) {
                        // Use the working loadTexture method for ByteBuffer data
                        return loadTexture(imageBuffer);
                    }
                }

                // Handle external files - use resource loading
                if (imageModel.getUri() != null && !imageModel.getUri().startsWith("data:")) {
                    String resourcePath = "models/" + imageModel.getUri(); // Adjust path as needed
                    System.out.println("Loading external texture from resources: " + resourcePath);

                    try {
                        return Texture.loadTextureFromResource(resourcePath);
                    } catch (Exception e) {
                        System.err.println("Failed to load texture from resources: " + e.getMessage());
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading texture: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Use the safer loadTexture method
    public static Texture loadTexture(ByteBuffer imageBuffer) {
        if (imageBuffer == null || imageBuffer.remaining() == 0) {
            System.err.println("Buffer is null or empty");
            return null;
        }

        System.out.println("Loading texture from buffer, size: " + imageBuffer.remaining() + " bytes");

        // Create a copy of the buffer to avoid position/limit issues
        ByteBuffer bufferCopy = ByteBuffer.allocateDirect(imageBuffer.remaining());
        bufferCopy.put(imageBuffer);
        bufferCopy.flip();

        int textureID;
        Texture t;

        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(true);

            // First, let's check if STB can identify the image format
            if (!STBImage.stbi_info_from_memory(bufferCopy, width, height, channels)) {
                System.err.println("STB cannot identify image format: " + STBImage.stbi_failure_reason());
                return null;
            }

            System.out.println("Image info: " + width.get(0) + "x" + height.get(0) + " channels: " + channels.get(0));

            // Reset buffer position for actual loading
            bufferCopy.rewind();

            ByteBuffer image = STBImage.stbi_load_from_memory(bufferCopy, width, height, channels, 4);

            if (image == null) {
                System.err.println("Failed to load texture from memory: " + STBImage.stbi_failure_reason());
                return null;
            }

            System.out.println("Successfully decoded image: " + width.get(0) + "x" + height.get(0) + " channels: " + channels.get(0));

            textureID = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width.get(), height.get(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image);
            GL30.glGenerateMipmap(GL_TEXTURE_2D);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

            STBImage.stbi_image_free(image);

            t = new Texture(textureID);
            t.setWidth(width.get(0));
            t.setHeight(height.get(0));

            System.out.println("Created OpenGL texture with ID: " + textureID);
        } catch (Exception e) {
            System.err.println("Exception during texture loading: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return t;
    }

    public static float[] extractFloatArray(AccessorModel accessor) {
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

    public static int[] extractIntArray(AccessorModel accessor) {
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

    public static void testTextureLoading(String gltfPath) {
        try {
            GltfModel gltfModel = new GltfModelReader().read(new File(gltfPath).toURI());

            System.out.println("=== GLTF Model Analysis ===");
            System.out.println("Materials: " + (gltfModel.getMaterialModels() != null ? gltfModel.getMaterialModels().size() : 0));
            System.out.println("Textures: " + (gltfModel.getTextureModels() != null ? gltfModel.getTextureModels().size() : 0));
            System.out.println("Images: " + (gltfModel.getImageModels() != null ? gltfModel.getImageModels().size() : 0));

            if (gltfModel.getTextureModels() != null) {
                for (int i = 0; i < gltfModel.getTextureModels().size(); i++) {
                    TextureModel textureModel = gltfModel.getTextureModels().get(i);
                    ImageModel imageModel = textureModel.getImageModel();
                    System.out.println("Texture " + i + ": " +
                            (imageModel != null ?
                                    (imageModel.getUri() != null ? "URI: " + imageModel.getUri() : "Embedded data: " + (imageModel.getImageData() != null ? imageModel.getImageData().remaining() + " bytes" : "null"))
                                    : "No image"));
                }
            }

            // Try loading the first texture
            if (gltfModel.getTextureModels() != null && !gltfModel.getTextureModels().isEmpty()) {
                Texture testTexture = loadTextureFromModel(gltfModel.getTextureModels().get(0), gltfPath);
                System.out.println("Test texture loading result: " + (testTexture != null ? "SUCCESS (ID: " + testTexture.getId() + ")" : "FAILED"));
            }

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Add this method to save texture data for debugging
    public static void saveTextureDebug(ByteBuffer imageBuffer, String outputPath) {
        if (imageBuffer == null || imageBuffer.remaining() == 0) {
            System.err.println("Cannot save texture: buffer is null or empty");
            return;
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            // Don't flip for debugging - we want to see the raw data
            STBImage.stbi_set_flip_vertically_on_load(false);

            ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, width, height, channels, 0);

            if (image == null) {
                System.err.println("Failed to decode image for debugging: " + STBImage.stbi_failure_reason());
                return;
            }

            int w = width.get(0);
            int h = height.get(0);
            int c = channels.get(0);

            System.out.println("Debug: Saving texture " + w + "x" + h + " with " + c + " channels to " + outputPath);

            // Save as PNG (works for 1, 3, or 4 channels)
            if (STBImageWrite.stbi_write_png(outputPath, w, h, c, image, w * c)) {
                System.err.println("Failed to write debug texture to " + outputPath);
            } else {
                System.out.println("✓ Debug texture saved successfully to " + outputPath);
            }

            STBImage.stbi_image_free(image);

        } catch (Exception e) {
            System.err.println("Error saving debug texture: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Enhanced version of debugGLB that saves textures
    public static void debugGLBWithTextureSaving(String glbPath) {
        try {
            System.out.println("=== DEBUGGING GLB FILE WITH TEXTURE SAVING ===");
            System.out.println("Loading: " + glbPath);

            GltfModel gltfModel = new GltfModelReader().read(new File(glbPath).toURI());

            System.out.println("Materials: " + (gltfModel.getMaterialModels() != null ? gltfModel.getMaterialModels().size() : 0));
            System.out.println("Textures: " + (gltfModel.getTextureModels() != null ? gltfModel.getTextureModels().size() : 0));
            System.out.println("Images: " + (gltfModel.getImageModels() != null ? gltfModel.getImageModels().size() : 0));

            // Save all embedded images for debugging
            if (gltfModel.getImageModels() != null) {
                for (int i = 0; i < gltfModel.getImageModels().size(); i++) {
                    ImageModel imageModel = gltfModel.getImageModels().get(i);
                    if (imageModel.getImageData() != null) {
                        ByteBuffer imageData = imageModel.getImageData();
                        System.out.println("Image " + i + " has embedded data: " + imageData.remaining() + " bytes");

                        // Save the raw texture data
                        String debugPath = "debug_texture_" + i + ".png";
                        saveTextureDebug(imageData, debugPath);

                        // Also test loading it through your texture system
                        System.out.println("Loading texture from buffer, size: " + imageData.remaining() + " bytes");

                        try (MemoryStack stack = stackPush()) {
                            IntBuffer width = stack.mallocInt(1);
                            IntBuffer height = stack.mallocInt(1);
                            IntBuffer channels = stack.mallocInt(1);

                            STBImage.stbi_set_flip_vertically_on_load(true);
                            ByteBuffer image = STBImage.stbi_load_from_memory(imageData, width, height, channels, 4);

                            if (image != null) {
                                System.out.println("Image info: " + width.get(0) + "x" + height.get(0) + " channels: " + channels.get(0));
                                System.out.println("Successfully decoded image: " + width.get(0) + "x" + height.get(0) + " channels: " + channels.get(0));

                                // Test OpenGL texture creation
                                int textureID = GL11.glGenTextures();
                                GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
                                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width.get(0), height.get(0), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image);
                                GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
                                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
                                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

                                System.out.println("Created OpenGL texture with ID: " + textureID);

                                STBImage.stbi_image_free(image);

                                System.out.println("Texture loading result: SUCCESS (ID: " + textureID + ")");
                                System.out.println("Texture dimensions: " + width.get(0) + "x" + height.get(0));
                            } else {
                                System.err.println("Failed to decode image: " + STBImage.stbi_failure_reason());
                            }
                        }
                    }
                }
            }

            System.out.println("\n=== LOADING FULL MODEL ===");
            // Continue with your existing debugging...
            Model loadedModel = load(glbPath);
            System.out.println("Model loaded successfully");

        } catch (Exception e) {
            System.err.println("Debug failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Also add this simple test method that forces a specific color
    public static Model loadWithForcedColor(String path, Vector3f testColor) throws Exception {
        GltfModel gltfModel = new GltfModelReader().read(new File(path).toURI());

        Model rootModel = new Model(null, testColor);

        List<SceneModel> scenes = gltfModel.getSceneModels();
        if (scenes != null && !scenes.isEmpty()) {
            SceneModel defaultScene = scenes.get(0);
            int meshIndex = 0;

            for (NodeModel rootNode : defaultScene.getNodeModels()) {
                meshIndex = processNodeWithForcedColor(rootNode, rootModel, meshIndex, testColor);
            }
        }

        return rootModel;
    }

    private static int processNodeWithForcedColor(NodeModel node, Model parent, int meshIndex, Vector3f color) {
        if (node.getMeshModels() != null) {
            for (MeshModel meshModel : node.getMeshModels()) {
                for (MeshPrimitiveModel primitiveModel : meshModel.getMeshPrimitiveModels()) {
                    float[] positions = extractFloatArray(primitiveModel.getAttributes().get("POSITION"));
                    float[] normals = extractFloatArray(primitiveModel.getAttributes().get("NORMAL"));
                    float[] texCoords = primitiveModel.getAttributes().containsKey("TEXCOORD_0")
                            ? extractFloatArray(primitiveModel.getAttributes().get("TEXCOORD_0")) : null;

                    int[] indices = extractIntArray(primitiveModel.getIndices());

                    Mesh mesh = new Mesh(positions, normals, texCoords, indices);
                    Model childModel = new Model(mesh, color);
                    parent.addChild("mesh_" + meshIndex++, childModel);
                }
            }
        }

        for (NodeModel childNode : node.getChildren()) {
            meshIndex = processNodeWithForcedColor(childNode, parent, meshIndex, color);
        }

        return meshIndex;
    }

    // Add this safer method to save texture data for debugging
    public static void saveTextureDebugSafe(ByteBuffer imageBuffer, String outputPath) {
        if (imageBuffer == null || imageBuffer.remaining() == 0) {
            System.err.println("Cannot save texture: buffer is null or empty");
            return;
        }

        try {
            // Create a safe copy of the buffer to avoid corruption
            ByteBuffer safeCopy = ByteBuffer.allocateDirect(imageBuffer.remaining());

            // Save current position/limit
            int originalPosition = imageBuffer.position();
            int originalLimit = imageBuffer.limit();

            // Copy the data
            safeCopy.put(imageBuffer);
            safeCopy.flip(); // Prepare for reading

            // Restore original buffer state
            imageBuffer.position(originalPosition);
            imageBuffer.limit(originalLimit);

            System.out.println("Debug: Created safe buffer copy, size: " + safeCopy.remaining() + " bytes");

            try (MemoryStack stack = stackPush()) {
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);

                // Don't flip for debugging - we want to see the raw data
                STBImage.stbi_set_flip_vertically_on_load(false);

                ByteBuffer image = STBImage.stbi_load_from_memory(safeCopy, width, height, channels, 0);

                if (image == null) {
                    System.err.println("Failed to decode image for debugging: " + STBImage.stbi_failure_reason());
                    return;
                }

                int w = width.get(0);
                int h = height.get(0);
                int c = channels.get(0);

                System.out.println("Debug: Decoded image " + w + "x" + h + " with " + c + " channels");

                // Save as PNG (works for 1, 3, or 4 channels)
                if (STBImageWrite.stbi_write_png(outputPath, w, h, c, image, w * c)) {
                    System.err.println("Failed to write debug texture to " + outputPath);
                } else {
                    System.out.println("✓ Debug texture saved successfully to " + outputPath);
                }

                STBImage.stbi_image_free(image);

            }

        } catch (Exception e) {
            System.err.println("Error saving debug texture: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Safer version of the debug method
    public static void debugGLBSafe(String glbPath) {
        try {
            System.out.println("=== SAFE GLB DEBUGGING ===");
            System.out.println("Loading: " + glbPath);

            GltfModel gltfModel = new GltfModelReader().read(new File(glbPath).toURI());

            System.out.println("Materials: " + (gltfModel.getMaterialModels() != null ? gltfModel.getMaterialModels().size() : 0));
            System.out.println("Textures: " + (gltfModel.getTextureModels() != null ? gltfModel.getTextureModels().size() : 0));
            System.out.println("Images: " + (gltfModel.getImageModels() != null ? gltfModel.getImageModels().size() : 0));

            // Only process the first few images to avoid overwhelming output
            if (gltfModel.getImageModels() != null) {
                int maxImages = Math.min(3, gltfModel.getImageModels().size()); // Only process first 3 images

                for (int i = 0; i < maxImages; i++) {
                    System.out.println("\n--- Processing Image " + i + " ---");
                    ImageModel imageModel = gltfModel.getImageModels().get(i);

                    if (imageModel.getImageData() != null) {
                        ByteBuffer imageData = imageModel.getImageData();
                        System.out.println("Image " + i + " has embedded data: " + imageData.remaining() + " bytes");
                        System.out.println("Buffer position: " + imageData.position() + ", limit: " + imageData.limit());

                        // Save the raw texture data safely
                        String debugPath = "debug_texture_safe_" + i + ".png";
                        saveTextureDebugSafe(imageData, debugPath);

                    } else {
                        System.out.println("Image " + i + " has no embedded data (external file or missing)");
                    }
                }
            }

            System.out.println("\n=== Debug completed safely ===");

        } catch (Exception e) {
            System.err.println("Safe debug failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Also create a method to just extract and examine buffer data without STB
    public static void examineBufferData(String glbPath) {
        try {
            System.out.println("=== EXAMINING BUFFER DATA ===");

            GltfModel gltfModel = new GltfModelReader().read(new File(glbPath).toURI());

            if (gltfModel.getImageModels() != null && !gltfModel.getImageModels().isEmpty()) {
                ImageModel imageModel = gltfModel.getImageModels().get(0);

                if (imageModel.getImageData() != null) {
                    ByteBuffer imageData = imageModel.getImageData();

                    System.out.println("Buffer info:");
                    System.out.println("  Capacity: " + imageData.capacity());
                    System.out.println("  Position: " + imageData.position());
                    System.out.println("  Limit: " + imageData.limit());
                    System.out.println("  Remaining: " + imageData.remaining());
                    System.out.println("  Is direct: " + imageData.isDirect());
                    System.out.println("  Has array: " + imageData.hasArray());

                    // Examine first few bytes to see if it's valid image data
                    if (imageData.remaining() >= 16) {
                        byte[] header = new byte[16];
                        int originalPos = imageData.position();
                        imageData.get(header);
                        imageData.position(originalPos); // Restore position

                        System.out.print("  First 16 bytes: ");
                        for (byte b : header) {
                            System.out.printf("%02X ", b & 0xFF);
                        }
                        System.out.println();

                        // Check for common image format headers
                        if (header[0] == (byte) 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G') {
                            System.out.println("  Format: PNG detected");
                        } else if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8) {
                            System.out.println("  Format: JPEG detected");
                        } else {
                            System.out.println("  Format: Unknown or corrupted");
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Buffer examination failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Enhanced debugging with proper material-texture mapping
    public static void debugGLBWithMaterialMapping(String glbPath) {
        try {
            System.out.println("=== GLB MATERIAL-TEXTURE MAPPING DEBUG ===");
            System.out.println("Loading: " + glbPath);

            GltfModel gltfModel = new GltfModelReader().read(new File(glbPath).toURI());

            System.out.println("Materials: " + (gltfModel.getMaterialModels() != null ? gltfModel.getMaterialModels().size() : 0));
            System.out.println("Textures: " + (gltfModel.getTextureModels() != null ? gltfModel.getTextureModels().size() : 0));
            System.out.println("Images: " + (gltfModel.getImageModels() != null ? gltfModel.getImageModels().size() : 0));

            // First, save all images
            if (gltfModel.getImageModels() != null) {
                for (int i = 0; i < gltfModel.getImageModels().size(); i++) {
                    ImageModel imageModel = gltfModel.getImageModels().get(i);
                    if (imageModel.getImageData() != null) {
                        String debugPath = "debug_image_" + i + ".png";
                        saveTextureDebugFixed(imageModel.getImageData(), debugPath);
                    }
                }
            }

            // Now analyze material-texture relationships
            System.out.println("\n--- MATERIAL-TEXTURE MAPPING ---");
            if (gltfModel.getMaterialModels() != null) {
                for (int i = 0; i < gltfModel.getMaterialModels().size(); i++) {
                    MaterialModel material = gltfModel.getMaterialModels().get(i);
                    System.out.println("Material " + i + ":");

                    // Check if material has any textures by examining the material model's properties
                    // The exact method names depend on the GLTF library version
                    try {
                        // Try to access material properties reflectively to find texture references
                        System.out.println("  Material class: " + material.getClass().getName());

                        // Check for common texture types by trying different accessor methods
                        checkMaterialTexture(material, "baseColorTexture", gltfModel);
                        checkMaterialTexture(material, "normalTexture", gltfModel);
                        checkMaterialTexture(material, "metallicRoughnessTexture", gltfModel);
                        checkMaterialTexture(material, "occlusionTexture", gltfModel);
                        checkMaterialTexture(material, "emissiveTexture", gltfModel);

                    } catch (Exception e) {
                        System.out.println("  Could not access material textures: " + e.getMessage());
                    }
                }
            }

            // Analyze mesh-material relationships
            System.out.println("\n--- MESH-MATERIAL MAPPING ---");
            if (gltfModel.getMeshModels() != null) {
                for (int meshIndex = 0; meshIndex < gltfModel.getMeshModels().size(); meshIndex++) {
                    MeshModel meshModel = gltfModel.getMeshModels().get(meshIndex);
                    System.out.println("Mesh " + meshIndex + ":");

                    for (int primIndex = 0; primIndex < meshModel.getMeshPrimitiveModels().size(); primIndex++) {
                        MeshPrimitiveModel primitive = meshModel.getMeshPrimitiveModels().get(primIndex);
                        MaterialModel material = primitive.getMaterialModel();

                        if (material != null) {
                            int materialIndex = gltfModel.getMaterialModels().indexOf(material);
                            System.out.println("  Primitive " + primIndex + " -> Material " + materialIndex);
                        } else {
                            System.out.println("  Primitive " + primIndex + " -> No material");
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Material mapping debug failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to safely check for material textures using reflection
    private static void checkMaterialTexture(MaterialModel material, String textureType, GltfModel gltfModel) {
        try {
            // Use reflection to access texture methods since the exact API might vary
            java.lang.reflect.Method[] methods = material.getClass().getMethods();

            for (java.lang.reflect.Method method : methods) {
                String methodName = method.getName().toLowerCase();
                if (methodName.contains(textureType.toLowerCase()) &&
                        method.getParameterCount() == 0 &&
                        method.getReturnType() != void.class) {

                    Object textureObj = method.invoke(material);
                    if (textureObj != null && textureObj instanceof TextureModel) {
                        TextureModel texture = (TextureModel) textureObj;
                        int textureIndex = gltfModel.getTextureModels().indexOf(texture);
                        int imageIndex = texture.getImageModel() != null ?
                                gltfModel.getImageModels().indexOf(texture.getImageModel()) : -1;

                        System.out.println("  " + textureType + ": texture[" + textureIndex + "] -> image[" + imageIndex + "]");
                    } else if (textureObj == null) {
                        System.out.println("  " + textureType + ": none");
                    }
                    break;
                }
            }
        } catch (Exception e) {
            // Silently ignore - this texture type might not be available
        }
    }

    // Simplified version that just focuses on getting textures working
    public static void debugGLBSimple(String glbPath) {
        try {
            System.out.println("=== SIMPLE GLB DEBUG ===");
            System.out.println("Loading: " + glbPath);

            GltfModel gltfModel = new GltfModelReader().read(new File(glbPath).toURI());

            System.out.println("Materials: " + (gltfModel.getMaterialModels() != null ? gltfModel.getMaterialModels().size() : 0));
            System.out.println("Textures: " + (gltfModel.getTextureModels() != null ? gltfModel.getTextureModels().size() : 0));
            System.out.println("Images: " + (gltfModel.getImageModels() != null ? gltfModel.getImageModels().size() : 0));

            // Save all images
            if (gltfModel.getImageModels() != null) {
                for (int i = 0; i < gltfModel.getImageModels().size(); i++) {
                    ImageModel imageModel = gltfModel.getImageModels().get(i);
                    if (imageModel.getImageData() != null) {
                        String debugPath = "debug_image_" + i + ".png";
                        saveTextureDebugFixed(imageModel.getImageData(), debugPath);
                    }
                }
            }

            // Show texture-image relationships
            if (gltfModel.getTextureModels() != null) {
                System.out.println("\n--- TEXTURE-IMAGE MAPPING ---");
                for (int i = 0; i < gltfModel.getTextureModels().size(); i++) {
                    TextureModel texture = gltfModel.getTextureModels().get(i);
                    int imageIndex = texture.getImageModel() != null ?
                            gltfModel.getImageModels().indexOf(texture.getImageModel()) : -1;

                    System.out.println("Texture " + i + " -> Image " + imageIndex);
                }
            }

        } catch (Exception e) {
            System.err.println("Simple debug failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Fix the PNG writing logic
    public static void saveTextureDebugFixed(ByteBuffer imageBuffer, String outputPath) {
        if (imageBuffer == null || imageBuffer.remaining() == 0) {
            System.err.println("Cannot save texture: buffer is null or empty");
            return;
        }

        try {
            // Create a safe copy of the buffer to avoid corruption
            ByteBuffer safeCopy = ByteBuffer.allocateDirect(imageBuffer.remaining());

            // Save current position/limit
            int originalPosition = imageBuffer.position();
            int originalLimit = imageBuffer.limit();

            // Copy the data
            safeCopy.put(imageBuffer);
            safeCopy.flip(); // Prepare for reading

            // Restore original buffer state
            imageBuffer.position(originalPosition);
            imageBuffer.limit(originalLimit);

            System.out.println("Debug: Created safe buffer copy, size: " + safeCopy.remaining() + " bytes");

            try (MemoryStack stack = stackPush()) {
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);

                // Don't flip for debugging - we want to see the raw data
                STBImage.stbi_set_flip_vertically_on_load(false);

                ByteBuffer image = STBImage.stbi_load_from_memory(safeCopy, width, height, channels, 0);

                if (image == null) {
                    System.err.println("Failed to decode image for debugging: " + STBImage.stbi_failure_reason());
                    return;
                }

                int w = width.get(0);
                int h = height.get(0);
                int c = channels.get(0);

                System.out.println("Debug: Decoded image " + w + "x" + h + " with " + c + " channels");

                // Save as PNG (works for 1, 3, or 4 channels)
                // FIX: stbi_write_png returns int (not boolean), 1 on success, 0 on failure
                boolean result = STBImageWrite.stbi_write_png(outputPath, w, h, c, image, w * c);
                if (result) {
                    System.err.println("Failed to write debug texture to " + outputPath);
                } else {
                    System.out.println("✓ Debug texture saved successfully to " + outputPath);
                }

                STBImage.stbi_image_free(image);

            }

        } catch (Exception e) {
            System.err.println("Error saving debug texture: " + e.getMessage());
            e.printStackTrace();
        }
    }
}