#version 330

uniform sampler2D MainSampler;

layout(std140) uniform LsdUniforms {
    vec2 view_res;
    float Time;
    float ColorIntensity;
    float MorphStrength;
    float AberrationStrength;
};

in vec2 texCoord;
layout(location = 0) out vec4 color;

float noise(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

void main() {
    vec2 uv = texCoord;
    vec2 centered = uv * 2.0 - 1.0;
    float radius = length(centered);

    float swirl = sin(radius * 16.0 - Time * 5.7) * MorphStrength * 0.055;
    float rippleX = sin(uv.y * 42.0 + Time * 8.4) * MorphStrength * 0.018;
    float rippleY = cos(uv.x * 37.0 - Time * 7.6) * MorphStrength * 0.018;
    vec2 radialDir = radius > 0.0001 ? normalize(centered) : vec2(0.0);
    uv += radialDir * swirl + vec2(rippleX, rippleY);

    vec2 chromaDir = normalize(vec2(cos(Time * 1.9), sin(Time * 1.3)));
    float chroma = (0.002 + 0.010 * ColorIntensity) * AberrationStrength;

    float r = texture(MainSampler, uv + chromaDir * chroma).r;
    float g = texture(MainSampler, uv).g;
    float b = texture(MainSampler, uv - chromaDir * chroma).b;
    vec3 base = vec3(r, g, b);

    float pulseA = 0.5 + 0.5 * sin(Time * 1.7);
    float pulseB = 0.5 + 0.5 * sin(Time * 2.3 + 2.2);
    float pulseC = 0.5 + 0.5 * sin(Time * 2.9 + 4.4);

    mat3 psycho = mat3(
        0.25 + 0.75 * pulseA, 0.55 - 0.25 * pulseB, 0.20 + 0.35 * pulseC,
        0.20 + 0.30 * pulseC, 0.30 + 0.70 * pulseB, 0.50 - 0.20 * pulseA,
        0.65 - 0.25 * pulseA, 0.20 + 0.40 * pulseC, 0.15 + 0.85 * pulseB
    );
    vec3 shifted = clamp(psycho * base, 0.0, 1.0);

    float grain = (noise(uv * view_res * 0.75 + Time * 23.0) - 0.5) * 0.12 * ColorIntensity;
    vec3 finalRgb = mix(base, shifted, clamp(0.45 + 0.45 * ColorIntensity, 0.0, 1.0));
    finalRgb += vec3(grain);
    color = vec4(clamp(finalRgb, 0.0, 1.0), 1.0);
}
