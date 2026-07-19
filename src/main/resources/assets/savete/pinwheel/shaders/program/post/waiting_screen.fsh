#include savete:vnoise

#veil:buffer veil:camera VeilCamera

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;

uniform float VeilRenderTime;
uniform float Fill;

in vec2 texCoord;

out vec4 fragColor;

float border(vec2 uv, float border_width)
{
    vec2 bottom_left = smoothstep(vec2(0), vec2(border_width), uv);
    vec2 top_right = smoothstep(vec2(0), vec2(border_width), 1.0 - uv);
    return bottom_left.x * bottom_left.y * top_right.x * top_right.y;
}

void main() {
    vec3 col = clamp(voronoi3d(vec3(texCoord * 5, VeilRenderTime)), vec3(0), vec3(1)) + vec3(0, 0, 1);
    vec3 tex = texture(DiffuseSampler0, texCoord).rgb;

    float vig = smoothstep(0., 1., border(texCoord, (1.0 - Fill)));

    fragColor = vec4(mix(tex, col, 1.0 - vig), 1.0);
}