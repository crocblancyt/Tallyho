#version 150

const int nsamples = 6;
const vec2 center = vec2(0.5);

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform float blurWidth;

out vec4 fragColor;

void main() {
//    vec2 center = vec2(InSize.x/2, InSize.y/2);
    float blurStart = 1.0;

    vec2 uv = texCoord;
    uv -= center;
    float precompute = blurWidth * (1.0 / float(nsamples - 1));

    vec4 color = vec4(0.0);
    for(int i = 0; i < nsamples; i++) {
        float scale = blurStart + (float(i)* precompute);
        color += texture(DiffuseSampler, uv * scale + center);
    }


    color /= float(nsamples);

    fragColor = vec4(color.xyz, 1.0);

    // Gaussian Blur
//    vec4 blurred = vec4(0.0);
//    float totalStrength = 0.0;
//    float totalAlpha = 0.0;
//    float totalSamples = 0.0;
//    for(float r = -Radius; r <= Radius; r += 1.0) {
//        vec4 sampleValue = texture(DiffuseSampler, texCoord + oneTexel * r * BlurDir);
//
//		// Accumulate average alpha
//        totalAlpha = totalAlpha + sampleValue.a;
//        totalSamples = totalSamples + 1.0;
//
//		// Accumulate smoothed blur
//        float strength = 1.0 - abs(r / Radius);
//        totalStrength = totalStrength + strength;
//        blurred = blurred + sampleValue;
//    }
//    fragColor = vec4(blurred.rgb / (Radius * 2.0 + 1.0), totalAlpha);
}
