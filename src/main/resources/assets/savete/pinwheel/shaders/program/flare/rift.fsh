#include savete:vnoise
#include veil:space_helper

uniform sampler2D RiftTexture;
uniform sampler2D MainDepthTexture;

uniform mat4 ModelViewMat;

uniform vec4 ColorModulator;
uniform vec4 _Time;
uniform vec2 ScreenSize;
uniform vec3 EntityPos;

in vec4 vertexColor;
in vec3 vertexPos;
in vec2 texCoord0;

out vec4 fragColor;

//Light direction vector
#define LIGHT normalize(vec3(1,3,-1))
//Gamma for gamma correction
#define GAMMA 2.2
#define MAX 50.0

float dist(vec3 p)
{
    //Distance to ground floor
    float ground = p.y+3.0;
    //Repeat XY axes for boxes
    p.xz = mod(p.xz, 4.0)-2.0;
    //Shift downward
    p.y += 2.3;
    //Compute distance using rounded box formula
    float box = length(max(abs(p)-0.4,-0.1))-0.2;

    return min(ground, box); //Return the closest of them all
}
vec3 normal(vec3 p)
{
    //Signed epsilon for computing distance field derivative
    const vec2 e = vec2(2, -2) * 0.001;

    //Basically we're offsetting by slight amounts and computing rate of change.
    //This tells us which direction is most positive and then we normalize the vector.
    return normalize(dist(p+e.xxy)*e.xxy + dist(p+e.xyx)*e.xyx + dist(p+e.yxx)*e.yxx + dist(p+e.y)*e.y);
}
//Basic soft shadow function
float shadow(vec3 pos, vec3 dir, float mx)
{
    float d = 0.01; //Starting distance
    float s = 1.0; //Shadow value

    for(int i = 0; i<40; i++) // Step 40 times
    {
        float step_dist = dist(pos + dir * d); //Check distance field
        d += step_dist; //March forward

        if (step_dist<0.001) return 0.0; //Stop at intersection
        if (d>mx) break; //Stop at max distance
        s = min(s, step_dist / d * 5.0); //Brightness from proximity

    }
    return s; //Return shadow brightness
}
vec3 color(vec3 pos, vec3 dir, float dep)
{
    //Get normal
    vec3 n = normal(pos);

    //Coloring and checkerboard pattern
    vec3 cell = ceil(pos*3.);
    float checker = mod(cell.x+cell.y+cell.z, 2.0);
    vec3 col = pow(1.0 - vec3(0,.6,.8)*checker, vec3(GAMMA));

    //Dot product lighting
    float light = dot(n, LIGHT);
    //Fade to black
    light = max(light, 0.1+light*0.1);

    //Shadow raymarching
    float shade = shadow(pos, LIGHT, MAX)*0.9+0.1;
    //Blend the shading together with ambient light
    vec3 amb = pow(vec3(0.01,0.2,0.3), vec3(GAMMA));
    col *= mix(amb, vec3(1), min(light, shade));

    //Compute sky color using simple gradient
    vec3 sky = pow(vec3(0.2, 0.5, 0.8) * (1.0+dot(dir, LIGHT)), vec3(GAMMA));
    //Fade to background sky color
    col = mix(col, sky, smoothstep(0.0, MAX, dep));

    //Set color from dot lighting
    return col;
}

vec4 raymarch(vec3 dir) {
    vec2 u = gl_FragCoord.xy / ScreenSize;
    float depth = texture(MainDepthTexture, u).r;
    vec3 pos = VeilCamera.CameraPosition + VeilCamera.CameraBobOffset - EntityPos;
    float d = 0.0f;

    for(int s = 0; s < 100; s++) {
        float step_dist = dist(pos + dir * d);
        d += step_dist;

        if (step_dist<0.01 || d>MAX) break;
    }

    return vec4(pos + dir * d, d);
}

void main() {
    vec4 rift = texture(RiftTexture, texCoord0);
    if (rift.a == 0) discard;
    vec2 rounded_uv = floor(texCoord0 * 48) / 48;

    float hole = (2 * distance(vec2(0.5), rounded_uv));
    float voronoi = shard_noise(vec3(rounded_uv * 8, _Time.x * 50), 30000.0) * pow(1 - distance(vec2(0.5), rounded_uv), 1.25f);

    if (texCoord0.x < 0.333333 || texCoord0.x > 0.6667 || texCoord0.y < 0.16667 || texCoord0.y > 0.83333) fragColor = vec4(voronoi) * vec4(0.7, 0.3, 1.0, 2.0);
    else {
        vec3 dir = normalize(viewDirFromUv(gl_FragCoord.xy / ScreenSize));
        vec4 m = raymarch(dir);
        //Find color at intersection
        vec3 sky = pow(vec3(0.2, 0.5, 0.8) * (1.0+dot(dir, LIGHT)), vec3(GAMMA));
        //Fade to background sky color
        vec3 c = texture(MainDepthTexture, (gl_FragCoord.xy / ScreenSize) - 0.1).rgb;

        fragColor = vec4((c), 1.0);
    }
}