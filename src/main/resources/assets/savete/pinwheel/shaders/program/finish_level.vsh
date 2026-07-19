#version 440 core

#include savete:vnoise

#veil:buffer veil:camera VeilCamera

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in vec2 UV2;
layout(location = 4) in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 NormalMat;
uniform float VeilRenderTime;

out vec4 vertexColor;
out vec3 vertexPos;
out vec2 texCoord0;
out vec3 normal;
out vec3 view;

void main() {
    vertexColor = Color;
    texCoord0 = UV0;
    normal = NormalMat * Normal;

    vec4 pos = vec4(Position + 0.25 * (vec3(rand(normal + fract(VeilRenderTime)), rand(normal + fract(VeilRenderTime) + 12), rand(normal + fract(VeilRenderTime) + 34)) - 0.5), 1.0);
    view = normalize((ModelViewMat * pos).xyz - VeilCamera.CameraBobOffset);
    vertexPos = ((VeilCamera.IViewMat * ModelViewMat) * pos).xyz;
    gl_Position = ProjMat * ModelViewMat * pos;
}