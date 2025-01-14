#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D textureSampler;

uniform vec2 texOffset;
uniform vec2 texScale;
uniform vec4 color = vec4(1,1,1,1);
uniform float noiseLevel = .5;
uniform float seed = 0;

void main() {
    vec2 textureCoord = (fragTexCoord * texScale) + texOffset;

    vec2 txc = textureCoord;

    vec4 textureColor = texture(textureSampler, textureCoord);

    vec4 recoloredColor = textureColor * color;


    fragColor = recoloredColor;
}
