package com.huskerdev.openglfx.internal.shaders

internal class FXAAShader: PassthroughShader() {
    override val vertexSource = """
        #version 330 core
        
        varying vec2 v_rgbNW;
        varying vec2 v_rgbNE;
        varying vec2 v_rgbSW;
        varying vec2 v_rgbSE;
        varying vec2 v_rgbM;
        
        uniform vec2 tex_size;
        
        layout (location = 0) in vec2 aPos;

        void main() {
            gl_Position = aPos;
            texcoords(aPos * tex_size, tex_size, v_rgbNW, v_rgbNE, v_rgbSW, v_rgbSE, v_rgbM);
        }
        
        void texcoords(vec2 fragCoord, vec2 resolution,
        			out vec2 v_rgbNW, out vec2 v_rgbNE,
        			out vec2 v_rgbSW, out vec2 v_rgbSE,
        			out vec2 v_rgbM) {
        	vec2 inverseVP = 1.0 / resolution.xy;
        	v_rgbNW = (fragCoord + vec2(-1.0, -1.0)) * inverseVP;
        	v_rgbNE = (fragCoord + vec2(1.0, -1.0)) * inverseVP;
        	v_rgbSW = (fragCoord + vec2(-1.0, 1.0)) * inverseVP;
        	v_rgbSE = (fragCoord + vec2(1.0, 1.0)) * inverseVP;
        	v_rgbM = vec2(fragCoord * inverseVP);
        }
    """.trimIndent()
    override val fragmentSource = """
        #version 330 core
        
        #define FXAA_REDUCE_MIN   (1.0/ 128.0)
        #define FXAA_REDUCE_MUL   (1.0 / 8.0)
        #define FXAA_SPAN_MAX     8.0
        
        varying vec2 v_rgbNW;
        varying vec2 v_rgbNE;
        varying vec2 v_rgbSW;
        varying vec2 v_rgbSE;
        varying vec2 v_rgbM;
        
        uniform sampler2D tex;
        uniform vec2 tex_size;
        
        layout(location = 0) out vec4 out_color;
        
        void main() {
            out_color = fxaa(tex, gl_FragCoord.xy, tex_size, v_rgbNW, v_rgbNE, v_rgbSW, v_rgbSE, v_rgbM)
        }
        
        vec4 fxaa(sampler2D tex, vec2 fragCoord, vec2 resolution,
                    vec2 v_rgbNW, vec2 v_rgbNE, 
                    vec2 v_rgbSW, vec2 v_rgbSE, 
                    vec2 v_rgbM) {
            vec4 color;
            mediump vec2 inverseVP = vec2(1.0 / resolution.x, 1.0 / resolution.y);
            vec3 rgbNW = texture2D(tex, v_rgbNW).xyz;
            vec3 rgbNE = texture2D(tex, v_rgbNE).xyz;
            vec3 rgbSW = texture2D(tex, v_rgbSW).xyz;
            vec3 rgbSE = texture2D(tex, v_rgbSE).xyz;
            vec4 texColor = texture2D(tex, v_rgbM);
            vec3 rgbM  = texColor.xyz;
            vec3 luma = vec3(0.299, 0.587, 0.114);
            float lumaNW = dot(rgbNW, luma);
            float lumaNE = dot(rgbNE, luma);
            float lumaSW = dot(rgbSW, luma);
            float lumaSE = dot(rgbSE, luma);
            float lumaM  = dot(rgbM,  luma);
            float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
            float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));
            
            mediump vec2 dir;
            dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
            dir.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));
            
            float dirReduce = max((lumaNW + lumaNE + lumaSW + lumaSE) *
                                  (0.25 * FXAA_REDUCE_MUL), FXAA_REDUCE_MIN);
            
            float rcpDirMin = 1.0 / (min(abs(dir.x), abs(dir.y)) + dirReduce);
            dir = min(vec2(FXAA_SPAN_MAX, FXAA_SPAN_MAX),
                      max(vec2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX),
                      dir * rcpDirMin)) * inverseVP;
            
            vec3 rgbA = 0.5 * (
                texture2D(tex, fragCoord * inverseVP + dir * (1.0 / 3.0 - 0.5)).xyz +
                texture2D(tex, fragCoord * inverseVP + dir * (2.0 / 3.0 - 0.5)).xyz);
            vec3 rgbB = rgbA * 0.5 + 0.25 * (
                texture2D(tex, fragCoord * inverseVP + dir * -0.5).xyz +
                texture2D(tex, fragCoord * inverseVP + dir * 0.5).xyz);

            float lumaB = dot(rgbB, luma);
            if ((lumaB < lumaMin) || (lumaB > lumaMax))
                color = vec4(rgbA, texColor.a);
            else
                color = vec4(rgbB, texColor.a);
            return color;
        }
    """.trimIndent()
}