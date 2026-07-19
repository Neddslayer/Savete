#include savete:vnoise

uniform sampler2D RiftTexture;
uniform sampler2D MainDepthTexture;

uniform mat4 ModelViewMat;

uniform vec4 ColorModulator;
uniform vec4 _Time;
uniform vec2 ScreenSize;

in vec4 vertexColor;
in vec3 vertexPos;
in vec2 texCoord0;

out vec4 fragColor;

//By Inigo Quilez, under MIT license
//https://www.shadertoy.com/view/ttcyRS
vec3 oklab_mix(vec3 lin1, vec3 lin2, float a)
{
    // https://bottosson.github.io/posts/oklab
    const mat3 kCONEtoLMS = mat3(
            0.4121656120,  0.2118591070,  0.0883097947,
            0.5362752080,  0.6807189584,  0.2818474174,
            0.0514575653,  0.1074065790,  0.6302613616);
    const mat3 kLMStoCONE = mat3(
            4.0767245293, -1.2681437731, -0.0041119885,
            -3.3072168827,  2.6093323231, -0.7034763098,
            0.2307590544, -0.3411344290,  1.7068625689);

    // rgb to cone (arg of pow can't be negative)
    vec3 lms1 = pow( kCONEtoLMS*lin1, vec3(1.0/3.0) );
    vec3 lms2 = pow( kCONEtoLMS*lin2, vec3(1.0/3.0) );
    // lerp
    vec3 lms = mix( lms1, lms2, a );
    // gain in the middle (no oklab anymore, but looks better?)
    lms *= 1.0+0.2*a*(1.0-a);
    // cone to rgb
    return kLMStoCONE*(lms*lms*lms);
}

void main() {
    vec2 rounded_uv = floor((texCoord0 + _Time.xx * 5) * 16) / 16;

    float val = rand(rounded_uv);

    // #veil:albedo
    fragColor = vec4(oklab_mix(vec3(0.5, 0.2411, 0.0423), vec3(1, 0.5607, 0), val) * (sin(_Time.x * 50) * sin(_Time.x * 50) * 0.2 + 0.2), 1.0);
}