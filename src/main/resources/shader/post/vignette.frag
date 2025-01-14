#version 330 core

in vec2 fragTexCoord;
out vec4 FragColor;

uniform sampler2D screenTexture;
uniform float strength = 3;

void main() {
    vec4 texColor = texture(screenTexture, fragTexCoord).rgba;

    vec2 position = vec2(1,1) - (fragTexCoord * 2);

    float d = distance(vec2(0,0),position);

    d = d * d;

    d = d / strength;

    d = 1 - d;

    FragColor = texColor * d;
}