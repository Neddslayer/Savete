#include savete:vnoise
#include savete:line

uniform sampler2D RiftTexture;
uniform sampler2D MainDepthTexture;

uniform mat4 ModelViewMat;

uniform vec4 ColorModulator;
uniform vec4 _Time;
uniform vec2 ScreenSize;
uniform vec3 EntityPos;
uniform float VeilRenderTime;
uniform float Players;

in vec4 vertexColor;
in vec3 vertexPos;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    float xMax = 2.3333333 / 1.33333;
    vec2 uv = texCoord0;
    uv.x *= xMax;

    vec2 rounded_uv = floor(uv * 128) / 128;

    float val = rand(rounded_uv + VeilRenderTime * (0.5 - Players * 0.5));

    // #veil:albedo
    fragColor = vec4(vec3(val), 1.0);
}