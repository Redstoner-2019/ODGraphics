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

void main() {
    vec3 norm = normalize(fragNormal);
    vec3 lightDirNorm = normalize(-lightDir);
    float diff = max(dot(norm, lightDirNorm), 0.0);
    vec3 diffuse = diff * lightColor;

    // Optional: ambient
    vec3 ambient = 0.1 * lightColor;

    vec3 finalColor;

    // Try to sample texture (fallback if texture is not used)
    vec4 texColor = texture(texture0, vec2(fragTexCoord.x, -fragTexCoord.y));

    if (texColor.a > 0.0) {
        finalColor = (ambient + diffuse) * texColor.rgb;
    } else {
        finalColor = (ambient + diffuse) * objectColor;
    }

    fragColor = vec4(finalColor, 1.0);
}
