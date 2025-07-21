#version 330 core

layout (location = 0) in vec4 vertex; // x, y, s, t

uniform vec2 screenSize;

out vec2 TexCoords;

void main() {
    vec2 pos = vertex.xy;
    pos = (pos / screenSize) * 2.0 - 1.0;
    pos.y = -pos.y;

    gl_Position = vec4(pos, 0.0, 1.0);
    TexCoords = vertex.zw;
}
