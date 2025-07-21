#version 330 core

in vec2 TexCoords;
out vec4 FragColor;

uniform sampler2D tex;
uniform vec3 textColor;

void main() {
    float alpha = texture(tex, TexCoords).r;
    FragColor = vec4(textColor, alpha);
}
