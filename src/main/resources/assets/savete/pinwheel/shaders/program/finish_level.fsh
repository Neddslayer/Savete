#include savete:vnoise
#include veil:space_helper

uniform sampler2D MainDepthTexture;

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

float fresnel(float amount, vec3 normal, vec3 view)
{
    return pow((1.0 - clamp(dot(normalize(normal), normalize(view)), 0.0, 1.0 )), amount);
}

vec3 localToScreenSpace(vec4 pos) {
    vec4 viewSpacePos = VeilCamera.ViewMat * pos;
    vec4 clipSpace = VeilCamera.ProjMat * (viewSpacePos / viewSpacePos.w);
    clipSpace.xyz /= clipSpace.w;
    return vec3((clipSpace.xy + vec2(1.0)) / 2.0, clipSpace.z);
}

void main() {
    vec2 screen_uv = gl_FragCoord.xy / ScreenSize;

    vec3 globalNormal = (VeilCamera.IProjMat * VeilCamera.IViewMat * vec4(normal , 1.0)).xyz - VeilCamera.CameraBobOffset;

    vec3 col;

    vec2 target = worldToScreenSpace(vec4(EntityPos, 1.0)).xy;

    float fres = fresnel(1, normal, -view);
    float visual_fres = fresnel(1, normal, -view);

    col = voronoi3d(globalNormal + VeilRenderTime);

    // #veil:albedo
    fragColor = vec4(col, 1.0);
}