package me.redstoner2019.threed.loader;

import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import me.redstoner2019.graphics.texture.Texture;
import me.redstoner2019.threed.model.Mesh;
import me.redstoner2019.threed.model.Model;
import org.joml.Vector3f;

import java.io.File;
import java.net.URI;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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

                Texture texture = null;
                MaterialModel material = primitiveModel.getMaterialModel();
                //primitiveModel.get
                //if (material != null) {
                //    TextureModel textureModel = material.
                //    if (textureModel != null) {
                //        ImageModel imageModel = textureModel.getImageModel();
                //        if (imageModel != null) {
                //            String textureName = imageModel.getUri();
                //            if (textureName != null) {
                //                texture = Texture.loadTexture("models/" + textureName); // adjust base path
                //            }
                //        }
                //    }
                //}

                return texture != null
                        ? new Model(mesh, texture)
                        : new Model(mesh, new Vector3f(1, 1, 1)); // fallback: white color
            }
        }

        throw new RuntimeException("No mesh found in glTF file: " + path);
    }

    private static float[] extractFloatArray(AccessorModel accessor) {
        if (accessor == null) return new float[0];
        FloatBuffer buffer = accessor.getAccessorData().createByteBuffer().asFloatBuffer();
        float[] data = new float[buffer.remaining()];
        buffer.get(data);
        return data;
    }

    private static int[] extractIntArray(AccessorModel accessor) {
        if (accessor == null) return new int[0];
        IntBuffer buffer = accessor.getAccessorData().createByteBuffer().asIntBuffer();
        int[] data = new int[buffer.remaining()];
        buffer.get(data);
        return data;
    }
}
