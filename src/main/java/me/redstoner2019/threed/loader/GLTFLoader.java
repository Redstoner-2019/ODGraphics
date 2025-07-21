package me.redstoner2019.threed.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.SceneModel;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import me.redstoner2019.threed.model.Mesh;
import me.redstoner2019.threed.model.Model;
import org.joml.Vector3f;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;

public class GLTFLoader {

    public static Model load(String path) throws Exception {
        return null;
    }

    private static int processNode(NodeModel node, Model parent, int meshIndex) {
        if (node.getMeshModels() != null) {
            for (var meshModel : node.getMeshModels()) {
                for (MeshPrimitiveModel prim : meshModel.getMeshPrimitiveModels()) {
                    Map<String, AccessorModel> attributes = prim.getAttributes();
                    AccessorModel posAccessor = attributes.get("POSITION");
                    AccessorModel normAccessor = attributes.get("NORMAL");

                    FloatBuffer posBuf = posAccessor.getAccessorData().createByteBuffer().asFloatBuffer();
                    FloatBuffer normBuf = normAccessor != null ? normAccessor.getAccessorData().createByteBuffer().asFloatBuffer() : null;

                    int vertexCount = posBuf.capacity() / 3;
                    float[] positions = new float[vertexCount * 3];
                    posBuf.get(positions);

                    float[] normals = new float[vertexCount * 3];
                    if (normBuf != null) {
                        normBuf.get(normals);
                    } else {
                        for (int i = 0; i < normals.length; i++) normals[i] = 0;
                    }

                    IntBuffer indexBuf = prim.getIndices().getAccessorData().createByteBuffer().asIntBuffer();
                    int[] indices = new int[indexBuf.capacity()];
                    indexBuf.get(indices);

                    Mesh mesh = new Mesh(positions, normals, indices);
                    Model subModel = new Model(mesh, new Vector3f((float)Math.random(), (float)Math.random(), (float)Math.random()));
                    parent.addChild("mesh_" + meshIndex++, subModel);
                }
            }
        }

        for (NodeModel child : node.getChildren()) {
            meshIndex = processNode(child, parent, meshIndex);
        }

        return meshIndex;
    }
}
