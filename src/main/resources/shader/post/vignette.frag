#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D screenTexture;
uniform vec4 color = vec4(1,1,1,1);

void main() {
    vec4 texColor = texture(screenTexture, fragTexCoord).rgba;

    vec2 position = vec2(1,1) - (fragTexCoord * 2);

    float d = distance(vec2(0,0),position);

    d = d * d;

    d = d / 4;

    d = 1 - d;

    fragColor = texColor * d * color;
}