#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D screenTexture;
uniform float seed = 0;
uniform float strength = 0.5;
uniform int pixelsX = 200;
uniform int pixelsY = 200;

float random(vec2 st) {
    return ((fract(sin(dot(st.xy, vec2(12.9898,78.233))) * 43758.5453123)));
}

vec4 lerp(vec4 a, vec4 b, float t) {
    return a + (b - a) * t;
}

void main() {
    vec2 pixelSeed1 = vec2(floor(fragTexCoord.x * pixelsX) + seed,floor(fragTexCoord.y * pixelsY) + seed);
    vec2 pixelSeed2 = vec2(floor(fragTexCoord.x * pixelsX) + seed,floor(fragTexCoord.y * pixelsY) + seed);
    vec2 pixelSeed3 = vec2(floor(fragTexCoord.x * pixelsX) + seed,floor(fragTexCoord.y * pixelsY) + seed);
    vec2 pixelSeed4 = vec2(floor(fragTexCoord.x * pixelsX) + seed,floor(fragTexCoord.y * pixelsY) + seed);

    vec4 randomColor = vec4(random(pixelSeed1),random(pixelSeed2),random(pixelSeed3),random(pixelSeed4));

    vec4 color = texture(screenTexture, fragTexCoord).rgba;

    fragColor = lerp(color,randomColor,strength);
}
