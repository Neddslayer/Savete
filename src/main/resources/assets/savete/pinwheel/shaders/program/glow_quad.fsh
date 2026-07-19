#include savete:vnoise
#include veil:space_helper

uniform mat4 ModelViewMat;

uniform vec4 ColorModulator;
uniform float VeilRenderTime;
uniform vec2 ScreenSize;
uniform vec3 EntityPos;

in vec4 vertexColor;
in vec3 vertexPos;
in vec2 texCoord0;
in vec3 normal;
in vec3 view;

out vec4 fragColor;

void main() {
    // #veil:albedo
    fragColor = vertexColor * ColorModulator;
}