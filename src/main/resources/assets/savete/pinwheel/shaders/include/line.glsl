float line(vec2 p1, vec2 p2, float width, vec2 uv)
{
    float dist = distance(p1, p2); // Distance between points
    float dist_uv = distance(p1, uv); // Distance from p1 to current pixel

    // If point is on line, according to dist, it should match current UV
    // Ideally the '0.001' should be SCREEN_PIXEL_SIZE.x, but we can't use that outside of the fragment function.
    return 1.0 - (floor(1.0 - 0.001 * width + distance(mix(p1, p2, clamp(dist_uv / dist, 0.0, 1.0)),  uv)));
}

float map(float value, float inMin, float inMax, float outMin, float outMax) {
    return outMin + (outMax - outMin) * (value - inMin) / (inMax - inMin);
}

bool lineLessY(vec2 p1, vec2 p2, vec2 uv) {
    float y = mix(p1.y, p2.y, map(uv.x, p1.x, p2.x, 0, 1));

    return uv.y < y;
}

bool lineGreaterY(vec2 p1, vec2 p2, vec2 uv) {
    float y = mix(p1.y, p2.y, map(uv.x, p1.x, p2.x, 0, 1));

    return uv.y > y;
}

bool lineLessX(vec2 p1, vec2 p2, vec2 uv) {
    float x = mix(p1.x, p2.x, map(uv.y, p1.y, p2.y, 0, 1));

    return uv.x < x;
}

bool lineGreaterX(vec2 p1, vec2 p2, vec2 uv) {
    float x = mix(p1.x, p2.x, map(uv.y, p1.y, p2.y, 0, 1));

    return uv.x > x;
}