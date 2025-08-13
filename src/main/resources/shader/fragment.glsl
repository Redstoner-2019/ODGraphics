#version 410 core

in vec3 fragNormal;
in vec3 fragPos;
in vec2 fragTexCoord;

out vec4 fragColor;

uniform vec3 lightDir;
uniform vec3 lightColor;
uniform vec3 objectColor;
uniform vec3 viewPos;

uniform sampler2D texture0;
uniform int useTexture;  // Add this uniform to match your Model.render()

void main() {
    vec3 norm = normalize(fragNormal);
    vec3 lightDirNorm = normalize(-lightDir);
    float diff = max(dot(norm, lightDirNorm), 0.0);
    vec3 diffuse = diff * lightColor;

    // Optional: ambient
    vec3 ambient = 0.1 * lightColor;

    vec3 finalColor;

    if (useTexture == 1) {
        // Use texture
        vec4 texColor = texture(texture0, fragTexCoord);
        finalColor = (ambient + diffuse) * texColor.rgb;
        finalColor = texColor.rgb;
    } else {
        // Use solid color
        finalColor = (ambient + diffuse) * objectColor;
    }

    fragColor = vec4(finalColor, 1.0);

}