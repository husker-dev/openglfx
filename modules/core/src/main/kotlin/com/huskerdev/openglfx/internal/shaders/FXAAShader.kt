package com.huskerdev.openglfx.internal.shaders

internal class FXAAShader: PassthroughShader(
    fragmentSource = """
        #version 100
        precision highp float;
        
        #define FXAA_SPAN_MAX     8.0
        #define FXAA_REDUCE_MUL   (1.0/FXAA_SPAN_MAX)
        #define FXAA_REDUCE_MIN   (1.0/128.0)
        #define FXAA_SUBPIX_SHIFT (1.0/4.0)
        
        vec4 fxaa(sampler2D tex, vec2 fragCoord, vec2 texSize) {
            vec2 rcpFrame = 1. / texSize.xy;
            vec2 uv2 = fragCoord / texSize.xy;
            vec4 uv = vec4(uv2, uv2 - (rcpFrame * (0.5 + FXAA_SUBPIX_SHIFT)));
        
            vec3 rgbNW = texture2D(tex, uv.zw).rgb;
            vec3 rgbNE = texture2D(tex, uv.zw + vec2(1,0)*rcpFrame.xy).rgb;
            vec3 rgbSW = texture2D(tex, uv.zw + vec2(0,1)*rcpFrame.xy).rgb;
            vec3 rgbSE = texture2D(tex, uv.zw + vec2(1,1)*rcpFrame.xy).rgb;
            vec3 rgbM  = texture2D(tex, uv.xy).rgb;
        
            vec3 luma = vec3(0.299, 0.587, 0.114);
            float lumaNW = dot(rgbNW, luma);
            float lumaNE = dot(rgbNE, luma);
            float lumaSW = dot(rgbSW, luma);
            float lumaSE = dot(rgbSE, luma);
            float lumaM  = dot(rgbM,  luma);
        
            float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
            float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));
        
            vec2 dir;
            dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
            dir.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));
        
            float dirReduce = max(
                (lumaNW + lumaNE + lumaSW + lumaSE) * (0.25 * FXAA_REDUCE_MUL),
                FXAA_REDUCE_MIN);
            float rcpDirMin = 1.0/(min(abs(dir.x), abs(dir.y)) + dirReduce);
            
            dir = min(vec2( FXAA_SPAN_MAX,  FXAA_SPAN_MAX),
                  max(vec2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX),
                  dir * rcpDirMin)) * rcpFrame.xy;
        
            vec4 rgbA = (1.0/2.0) * (
                texture2D(tex, uv.xy + dir * (1.0/3.0 - 0.5)).rgba +
                texture2D(tex, uv.xy + dir * (2.0/3.0 - 0.5)).rgba);
            vec4 rgbB = rgbA * (1.0/2.0) + (1.0/4.0) * (
                texture2D(tex, uv.xy + dir * (0.0/3.0 - 0.5)).rgba +
                texture2D(tex, uv.xy + dir * (3.0/3.0 - 0.5)).rgba);
            
            float lumaB = dot(rgbB.rgb, luma);
        
            if((lumaB < lumaMin) || (lumaB > lumaMax)) return rgbA;
            return rgbB; 
        }
        
        uniform sampler2D tex;
        uniform vec2 tex_size;
        
        void main() {
	        gl_FragColor = fxaa(tex, gl_FragCoord.xy, tex_size);
        }
    """.trimIndent()
)