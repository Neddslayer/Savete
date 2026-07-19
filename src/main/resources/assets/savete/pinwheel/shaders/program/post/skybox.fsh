#include savete:vnoise
#include veil:space_helper

#veil:buffer veil:camera VeilCamera

uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;

uniform float VeilRenderTime;
uniform vec2 ScreenSize;

in vec2 texCoord;

out vec4 fragColor;

vec3 StarLayer(vec3 uv){
    vec3 col = vec3(0);

    float star = pow(cnoise(uv * 90), 2);

    if (star > 0.8) col =
        mix(
            vec3(pow(sin(uv.z + uv.x + VeilRenderTime * 0.25), 2), 0, pow(cos(uv.x - uv.z - VeilRenderTime * 0.25), 2) + 0.23),
            vec3(1),
            0.3 + cos(2 * VeilRenderTime) * 0.25
        )
        * sin(90 * uv.x - 80 * uv.z + 50 * uv.y + VeilRenderTime * 1.5)
        * star;
    return clamp(col, vec3(0), vec3(1));
}

void main() {
    vec3 eyeDir = viewDirFromUv(texCoord);

    vec3 stars = StarLayer(eyeDir * 3.1415 * 0.5);

    vec3 rainbow = vec3(0.4 + sin(VeilRenderTime) * 0.2, 0.4 + sin(VeilRenderTime) * 0.2, 0.75 + sin(VeilRenderTime) * 0.25 + 0.25);

    vec3 col = stars + rainbow * max(eyeDir.y, -0.5) + sin(VeilRenderTime + 1) * 0.05;

    if (eyeDir.y > 0) {
        float sun = pow(eyeDir.y, 3);
        vec3 noise = clamp(voronoi3d(5 * eyeDir - vec3(0, VeilRenderTime, 0)), vec3(0), vec3(1));
        if (eyeDir.y > 0.8) {
            noise += (eyeDir.y - 0.8) * 2;
        }
        col = mix(col, noise, sun);
    }


    fragColor = vec4(col, 1.0);
}