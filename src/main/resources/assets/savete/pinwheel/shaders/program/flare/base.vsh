#version 440 core

#veil:buffer veil:camera VeilCamera

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in vec2 UV2;
layout(location = 4) in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat3 NormalMat;

out vec4 vertexColor;
out vec3 vertexPos;
out vec2 texCoord0;
out vec3 normal;
out vec3 view;

void main() {
    vec4 pos = vec4(Position, 1.0);
    view = normalize((ModelViewMat * pos).xyz - VeilCamera.CameraBobOffset);
    vertexPos = ((VeilCamera.IViewMat * ModelViewMat) * pos).xyz;
    gl_Position = ProjMat * ModelViewMat * pos;

    vertexColor = Color;
    texCoord0 = UV0;

    // #veil:normal
    normal = NormalMat * Normal;
}