#include savete:vnoise
#include veil:space_helper

#veil:buffer veil:camera VeilCamera

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;

uniform float VeilRenderTime;
uniform vec2 ScreenSize;

in vec2 texCoord;

out vec4 fragColor;

float linearize_depth(float d,float zNear,float zFar) {
    return zNear * zFar / (zFar + d * (zNear - zFar));
}

void main() {
    vec3 col = texture(DiffuseSampler0, texCoord).rgb;

    float time = floor(fract(VeilRenderTime) * 8) / 8;

    vec2 uv = texCoord;
    ivec2 size = textureSize(DiffuseSampler0, 0);
    uv.x *= float(size.x) / float(size.y);

    vec2 rounded_uv = floor(uv * 512) / 512;

    float screen_static = rand(rounded_uv + time) * 0.1;

    float depthRaw = texture(DiffuseDepthSampler, texCoord).r;
    if (depthRaw == 1) fragColor = vec4(col, 1.0);
    else fragColor = vec4(col - (screen_static) + 0.05, 1.0);
}