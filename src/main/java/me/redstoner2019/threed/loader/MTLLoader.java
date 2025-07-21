package me.redstoner2019.threed.loader;

import me.redstoner2019.graphics.texture.Texture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MTLLoader {
    public static Map<String, Texture> loadMTL(String mtlFilePath, String textureFolder) throws IOException {
        Map<String, Texture> materials = new HashMap<>();
        String currentMaterial = null;
        Texture currentTexture = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(mtlFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("newmtl ")) {
                    currentMaterial = line.substring(7).trim();
                } else if (line.startsWith("map_Kd ") && currentMaterial != null) {
                    String texturePath = line.substring(7).trim();
                    currentTexture = Texture.loadTexture(textureFolder + texturePath);
                    materials.put(currentMaterial, currentTexture);
                }
            }
        }

        return materials;
    }
}

