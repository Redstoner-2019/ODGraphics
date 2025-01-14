#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D screenTexture;
uniform vec4 color = vec4(1,1,1,1);

void main() {
    vec4 texColor = texture(screenTexture, fragTexCoord).rgba;

    fragColor = color;
}