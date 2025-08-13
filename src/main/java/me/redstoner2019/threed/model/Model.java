package me.redstoner2019.threed.model;

import me.redstoner2019.graphics.shader.ShaderProgram;
import me.redstoner2019.graphics.texture.Texture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL13;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Model {
    private Mesh mesh;
    private Texture texture;
    private Vector3f color;

    private Vector3f position = new Vector3f(0, 0, 0);
    private Vector3f positionOffset = new Vector3f(0, 0, 0);
    private Vector3f rotation = new Vector3f(0,0,0); // in degrees
    private Vector3f scale = new Vector3f(1, 1, 1);

    private HashMap<String, Model> children = new HashMap<>();

    public Model() {
    }

    public Model(Mesh mesh, Texture texture, Vector3f color) {
        this.mesh = mesh;
        this.texture = texture; // This line was setting texture to null instead of the parameter
        this.color = color;
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
        if(this.color == null) this.color = new Vector3f(1, 1, 1);
    }

    public Model(Mesh mesh, Texture texture) {
        this.mesh = mesh;
        this.texture = texture;
        this.color = new Vector3f(1, 1, 1);
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
    }

    public Model(Mesh mesh, Vector3f color) {
        this.mesh = mesh;
        this.texture = null;
        this.color = color;
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
    }

    public Matrix4f getModelMatrix(){
        return new Matrix4f()
                .translate(position.x + positionOffset.x, position.y + positionOffset.y, position.z + positionOffset.z)
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z))
                .scale(scale.x, scale.y, scale.z);
    }

    public void render(ShaderProgram shader) {
        // Only render if this model has a mesh
        if (mesh != null) {
            Matrix4f modelMatrix = getModelMatrix();

            shader.setUniform4fv("model", modelMatrix);

            if (texture != null) {
                shader.setUniform1i("useTexture", 1);
                shader.setUniform3f("objectColor", new Vector3f(1, 1, 1));
                shader.setUniform1i("texture0", 0);  // Add this line - bind texture to slot 0
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, texture.getId());
            } else {
                shader.setUniform1i("useTexture", 0);
                shader.setUniform3f("objectColor", color);
            }

            mesh.render();
        }

        // Always render children regardless of whether this model has a mesh
        for(Model child : children.values()){
            child.setScale(scale);
            child.setPosition(position);
            child.setRotation(rotation);
            child.setPositionOffset(positionOffset);
            child.render(shader);
        }
    }

    /**
     * Normalizes the model so that all vertices are within maxDistance units from the center.
     * This method works on the entire model hierarchy, maintaining relative positions.
     * 
     * @param maxDistance The maximum distance any vertex should be from the model's center
     */
    public void normalize(float maxDistance) {
        // First pass: calculate global bounding box for the entire model hierarchy
        float[] globalBounds = calculateGlobalBounds();
        if (globalBounds == null) return; // No meshes found
        
        float minX = globalBounds[0], maxX = globalBounds[1];
        float minY = globalBounds[2], maxY = globalBounds[3];
        float minZ = globalBounds[4], maxZ = globalBounds[5];
        
        // Calculate global center
        float centerX = (minX + maxX) / 2f;
        float centerY = (minY + maxY) / 2f;
        float centerZ = (minZ + maxZ) / 2f;
        
        // Find the maximum distance from global center across all vertices
        float currentMaxDistance = calculateMaxDistanceFromCenter(centerX, centerY, centerZ);
        
        if (currentMaxDistance > 0) {
            float scaleFactor = maxDistance / currentMaxDistance;
            
            // Second pass: apply the same transformation to all meshes
            applyNormalization(centerX, centerY, centerZ, scaleFactor);
        }
    }

    private float[] calculateGlobalBounds() {
        float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
        float minZ = Float.MAX_VALUE, maxZ = Float.MIN_VALUE;
        
        boolean foundAnyMesh = false;
        
        // Check this model's mesh
        if (mesh != null) {
            float[] positions = mesh.getPositions();
            if (positions != null && positions.length > 0) {
                foundAnyMesh = true;
                for (int i = 0; i < positions.length; i += 3) {
                    float x = positions[i];
                    float y = positions[i + 1];
                    float z = positions[i + 2];
                    
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                    minZ = Math.min(minZ, z);
                    maxZ = Math.max(maxZ, z);
                }
            }
        }
        
        // Recursively check all children
        for (Model child : children.values()) {
            float[] childBounds = child.calculateGlobalBounds();
            if (childBounds != null) {
                foundAnyMesh = true;
                minX = Math.min(minX, childBounds[0]);
                maxX = Math.max(maxX, childBounds[1]);
                minY = Math.min(minY, childBounds[2]);
                maxY = Math.max(maxY, childBounds[3]);
                minZ = Math.min(minZ, childBounds[4]);
                maxZ = Math.max(maxZ, childBounds[5]);
            }
        }
        
        return foundAnyMesh ? new float[]{minX, maxX, minY, maxY, minZ, maxZ} : null;
    }

    private float calculateMaxDistanceFromCenter(float centerX, float centerY, float centerZ) {
        float maxDistance = 0f;
        
        // Check this model's mesh
        if (mesh != null) {
            float[] positions = mesh.getPositions();
            if (positions != null) {
                for (int i = 0; i < positions.length; i += 3) {
                    float x = positions[i] - centerX;
                    float y = positions[i + 1] - centerY;
                    float z = positions[i + 2] - centerZ;
                    float distance = (float) Math.sqrt(x * x + y * y + z * z);
                    maxDistance = Math.max(maxDistance, distance);
                }
            }
        }
        
        // Recursively check all children
        for (Model child : children.values()) {
            float childMaxDistance = child.calculateMaxDistanceFromCenter(centerX, centerY, centerZ);
            maxDistance = Math.max(maxDistance, childMaxDistance);
        }
        
        return maxDistance;
    }

    private void applyNormalization(float centerX, float centerY, float centerZ, float scaleFactor) {
        // Apply normalization to this model's mesh if it exists
        if (mesh != null) {
            float[] positions = mesh.getPositions();
            float[] normals = mesh.getNormals();
            float[] texCoords = mesh.getTexCoords();
            int[] indices = mesh.getIndices();
            
            if (positions != null && positions.length > 0) {
                // Apply transformation: center and scale vertices
                for (int i = 0; i < positions.length; i += 3) {
                    positions[i] = (positions[i] - centerX) * scaleFactor;
                    positions[i + 1] = (positions[i + 1] - centerY) * scaleFactor;
                    positions[i + 2] = (positions[i + 2] - centerZ) * scaleFactor;
                }
            
                // Create new mesh with normalized positions
                this.mesh = new Mesh(positions, normals, texCoords, indices);
            }
        }
        
        // Recursively apply to all children
        for (Model child : children.values()) {
            child.applyNormalization(centerX, centerY, centerZ, scaleFactor);
        }
    }

    private void normalizeMesh(float maxDistance) {
        // This method is now deprecated in favor of the new normalize approach
        // Keeping it for backward compatibility but it's not recommended for complex models
        float[] positions = mesh.getPositions();
        float[] normals = mesh.getNormals();
        float[] texCoords = mesh.getTexCoords();
        int[] indices = mesh.getIndices();
        
        if (positions == null || positions.length == 0) {
            return;
        }

        // Find the bounding box to determine center and current max distance
        float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
        float minZ = Float.MAX_VALUE, maxZ = Float.MIN_VALUE;

        for (int i = 0; i < positions.length; i += 3) {
            float x = positions[i];
            float y = positions[i + 1];
            float z = positions[i + 2];

            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
        }

        // Calculate center
        float centerX = (minX + maxX) / 2f;
        float centerY = (minY + maxY) / 2f;
        float centerZ = (minZ + maxZ) / 2f;

        // Find the maximum distance from center
        float currentMaxDistance = 0f;
        for (int i = 0; i < positions.length; i += 3) {
            float x = positions[i] - centerX;
            float y = positions[i + 1] - centerY;
            float z = positions[i + 2] - centerZ;
            float distance = (float) Math.sqrt(x * x + y * y + z * z);
            currentMaxDistance = Math.max(currentMaxDistance, distance);
        }

        // Calculate scale factor
        if (currentMaxDistance > 0) {
            float scaleFactor = maxDistance / currentMaxDistance;

            // Apply normalization: center and scale vertices
            for (int i = 0; i < positions.length; i += 3) {
                // Center the vertex
                positions[i] = (positions[i] - centerX) * scaleFactor;
                positions[i + 1] = (positions[i + 1] - centerY) * scaleFactor;
                positions[i + 2] = (positions[i + 2] - centerZ) * scaleFactor;
            }

            // Create new mesh with normalized positions
            this.mesh = new Mesh(positions, normals, texCoords, indices);
        }
    }

    public void rotate(float yawDegrees, float pitchDegrees) {
        float yaw = (float) Math.toRadians(yawDegrees);
        float pitch = (float) Math.toRadians(pitchDegrees);

        // Add to existing rotation
        rotation.y += yaw;
        rotation.x += pitch;

        // Optional: clamp pitch to avoid flipping (e.g., for camera-like behavior)
        //rotation.x = Math.max(- (float)Math.PI / 2f, Math.min((float)Math.PI / 2f, rotation.x));
    }

    public void addChild(String name, Model model) {
        children.put(name, model);
    }

    public Model getChild(String name) {
        return children.get(name);
    }

    public List<Model> getChildren() {
        return new ArrayList<>(children.values());
    }

    // Getters and setters

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Texture getTexture() {
        return texture;
    }

    public Vector3f getPositionOffset() {
        return positionOffset;
    }

    public void setPositionOffset(Vector3f positionOffset) {
        this.positionOffset = positionOffset;
    }

    public void setChildren(HashMap<String, Model> children) {
        this.children = children;
    }

    public void print(){
        System.out.println(color.x + " " + color.y + " " + color.z + " " + children.keySet());
        for(Model m : children.values()){
            m.print();
        }
    }
}