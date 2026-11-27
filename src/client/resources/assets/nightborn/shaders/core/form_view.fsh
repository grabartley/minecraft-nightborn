#version 150

uniform sampler2D SceneSampler;
uniform sampler2D DepthSampler;

uniform mat4 InverseViewProj;
uniform vec2 ScreenSize;
uniform float Strength;
uniform float Time;
uniform float Darkness;
uniform vec3 EchoNear;
uniform vec3 EchoLook;
uniform float Ripples[128];
uniform float RippleWaves[64];
uniform float CreatureRects[64];
uniform float CreatureDepths[32];
uniform vec3 TintColor;
uniform vec2 Tint;
uniform vec3 Tremble;
uniform vec3 WashColor;
uniform vec4 Wash;

in vec2 texCoord;

out vec4 fragColor;

const float SKY = 1000.0;

vec3 seen(vec2 uv) {
    float depth = texture(DepthSampler, uv).r;
    vec4 at = InverseViewProj * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    return at.xyz / at.w;
}

float distanceAt(vec2 uv) {
    if (texture(DepthSampler, uv).r >= 1.0) {
        return SKY;
    }
    return length(seen(uv));
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(cell), hash(cell + vec2(1.0, 0.0)), u.x),
               mix(hash(cell + vec2(0.0, 1.0)), hash(cell + vec2(1.0, 1.0)), u.x), u.y);
}

float luma(vec3 color) {
    return dot(color, vec3(0.299, 0.587, 0.114));
}

float shapeEdge(vec2 uv, float here) {
    vec2 step = ScreenSize * 1.5;
    float left = min(distanceAt(uv - vec2(step.x, 0.0)), SKY);
    float right = min(distanceAt(uv + vec2(step.x, 0.0)), SKY);
    float down = min(distanceAt(uv - vec2(0.0, step.y)), SKY);
    float up = min(distanceAt(uv + vec2(0.0, step.y)), SKY);
    float crease = (abs(left + right - 2.0 * here) + abs(up + down - 2.0 * here)) / max(here, 0.5);
    float rim = max(max(abs(left - here), abs(right - here)), max(abs(up - here), abs(down - here))) / max(here, 0.5);
    return clamp(smoothstep(0.015, 0.06, crease) + smoothstep(0.08, 0.25, rim), 0.0, 1.0);
}

float grainEdge(vec2 uv) {
    vec2 step = ScreenSize;
    float gx = luma(texture(SceneSampler, uv + vec2(step.x, 0.0)).rgb) - luma(texture(SceneSampler, uv - vec2(step.x, 0.0)).rgb);
    float gy = luma(texture(SceneSampler, uv + vec2(0.0, step.y)).rgb) - luma(texture(SceneSampler, uv - vec2(0.0, step.y)).rgb);
    return smoothstep(0.08, 0.35, length(vec2(gx, gy)));
}

float heard(vec3 point, float here) {
    float light = EchoNear.y * (1.0 - smoothstep(EchoNear.x * 0.5, EchoNear.x, here));
    for (int i = 0; i < 32; i++) {
        float alpha = RippleWaves[i * 2];
        if (alpha <= 0.0) {
            continue;
        }
        float width = RippleWaves[i * 2 + 1];
        vec3 origin = vec3(Ripples[i * 4], Ripples[i * 4 + 1], Ripples[i * 4 + 2]);
        float radius = Ripples[i * 4 + 3];
        float from = length(point - origin);
        float front = exp(-pow((from - radius) / width, 2.0));
        float wake = from < radius ? 0.5 * exp(-(radius - from) / (width * 4.0)) : 0.0;
        light += alpha * (front + wake);
    }
    return clamp(light, 0.0, 1.5);
}

float creature(vec2 uv, float here) {
    for (int i = 0; i < 16; i++) {
        float far = CreatureDepths[i * 2 + 1];
        if (far <= 0.0) {
            continue;
        }
        vec4 rect = vec4(CreatureRects[i * 4], CreatureRects[i * 4 + 1], CreatureRects[i * 4 + 2], CreatureRects[i * 4 + 3]);
        if (uv.x >= rect.x && uv.x <= rect.z && uv.y >= rect.y && uv.y <= rect.w
                && here >= CreatureDepths[i * 2] - 0.1 && here <= far + 0.1) {
            return 1.0;
        }
    }
    return 0.0;
}

vec3 echo(vec2 uv) {
    float here = distanceAt(uv);
    if (here >= SKY) {
        return vec3(0.0);
    }
    float edge = max(shapeEdge(uv, here), grainEdge(uv) * 0.6);
    float light = heard(seen(uv), here);
    float glow = (EchoLook.x * edge + EchoLook.y) * light * (1.0 + EchoLook.z * creature(uv, here));
    return vec3(clamp(glow, 0.0, 1.0));
}

void main() {
    vec2 centred = texCoord - 0.5;
    float rim = clamp(length(centred) * 1.41421, 0.0, 1.0);
    vec2 uv = texCoord;

    float shake = smoothstep(Tremble.y, 1.0, rim) * Tremble.x;
    uv += (vec2(noise(texCoord * 9.0 + Time * Tremble.z), noise(texCoord * 9.0 - Time * Tremble.z + 17.0)) - 0.5) * 2.0 * shake;
    uv += vec2(sin(Time * 0.7 + texCoord.y * 6.0), cos(Time * 0.5 + texCoord.x * 5.0)) * Wash.w;
    uv = clamp(uv, ScreenSize, 1.0 - ScreenSize);

    vec3 scene = texture(SceneSampler, texCoord).rgb;
    vec3 color = texture(SceneSampler, uv).rgb;

    if (Wash.z > 0.0) {
        float reach = Wash.z * (0.4 + 0.6 * rim);
        vec3 soft = color;
        for (int i = 0; i < 8; i++) {
            float angle = float(i) * 0.785398;
            soft += texture(SceneSampler, clamp(uv + vec2(cos(angle), sin(angle)) * ScreenSize * reach * 2.0, ScreenSize, 1.0 - ScreenSize)).rgb;
        }
        color = soft / 9.0;
    }

    if (EchoNear.z > 0.5) {
        color = color * (1.0 - Darkness) + echo(uv);
    } else {
        color *= 1.0 - Darkness;
    }

    float tint = mix(Tint.x, Tint.y, smoothstep(0.2, 1.0, rim));
    vec3 rage = mix(color * TintColor * 1.6, TintColor * luma(color) * 1.8, 0.5);
    color = mix(color, rage, tint);

    color = mix(vec3(luma(color)), color, Wash.y);
    color = mix(color, WashColor, Wash.x * (0.6 + 0.4 * rim));

    fragColor = vec4(mix(scene, clamp(color, 0.0, 1.0), Strength), 1.0);
}
