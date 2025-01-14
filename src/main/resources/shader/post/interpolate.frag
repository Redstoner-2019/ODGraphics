#version 330 core
in vec2 fragTexCoord;

out vec4 FragColor;

uniform sampler2D image1;
uniform sampler2D image2;
uniform float dt;

void main() {
    vec4 color1 = texture(image1, fragTexCoord);
    vec4 color2 = texture(image2, fragTexCoord);

    FragColor = mix(color1, color2, dt);
}