#version 330 core
out vec4 fragColor;
in vec2 texCoord;

uniform sampler2D screenTexture;
uniform float seed = 0;

float random(vec2 st) {
    return ((fract(sin(dot(st.xy, vec2(12.9898,78.233))) * 43758.5453123)));
}

vec4 lerp(vec4 a, vec4 b, float t) {
    return a + (b - a) * t;
}

void main() {
    int pixelsX = 200;
    int pixelsY = 200;

    vec2 pixelSeed1 = vec2(floor(texCoord.x * pixelsX) + seed,floor(texCoord.y * pixelsY) + seed);
    vec2 pixelSeed2 = vec2(floor(texCoord.x * pixelsX) + seed,floor(texCoord.y * pixelsY) + seed);
    vec2 pixelSeed3 = vec2(floor(texCoord.x * pixelsX) + seed,floor(texCoord.y * pixelsY) + seed);
    vec2 pixelSeed4 = vec2(floor(texCoord.x * pixelsX) + seed,floor(texCoord.y * pixelsY) + seed);

    vec4 randomColor = vec4(random(pixelSeed1),random(pixelSeed2),random(pixelSeed3),random(pixelSeed4));

    vec4 color = texture(screenTexture, texCoord).rgba;

    fragColor = lerp(color,randomColor,0.5);
}
