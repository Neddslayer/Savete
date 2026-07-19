uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec3 vertexPos;
in vec2 texCoord0;
in vec3 normal;
in vec3 view;

out vec4 fragColor;

void main() {
    vec4 tex = texture(Sampler0, texCoord0);

    if (tex.a < 0.1) discard;

    // #veil:albedo
    fragColor = tex * ColorModulator;
}