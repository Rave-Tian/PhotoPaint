package com.example.photopaint.ui.components.paint;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ShaderSet {

    private static final Map<String, Map<String, Object>> AVAILBALBE_SHADERS = createMap();

    private static final String VERTEX = "vertex";
    private static final String FRAGMENT = "fragment";
    private static final String ATTRIBUTES = "attributes";
    private static final String UNIFORMS = "uniforms";

    private static final String PAINT_BRUSH_VSH = "" +
            "precision highp float; " +
            "uniform mat4 mvpMatrix; " +
            "attribute vec4 inPosition; " +
            "attribute vec2 inTexcoord; " +
            "attribute float alpha; " +
            "attribute float inRed; " +
            "attribute float inGreen; " +
            "attribute float inBlue; " +
            "varying vec2 varTexcoord; " +
            "varying float varIntensity; " +
            "varying float varRed; " +
            "varying float varGreen; " +
            "varying float varBlue; " +
            "void main (void) {" +
            " gl_Position = mvpMatrix * inPosition;" +
            " varTexcoord = inTexcoord;" +
            " varIntensity = alpha;" +
            " varRed = inRed;" +
            " varGreen = inGreen;" +
            " varBlue = inBlue;" +
            " }";

    // 画刷的片元着色器
    private static final String PAINT_BRUSH_FSH = "" +
            "precision highp float; " +
            "varying vec2 varTexcoord; " +
            "varying float varIntensity; " +
            "varying float varRed; " +
            "varying float varGreen; " +
            "varying float varBlue; " +
            "uniform sampler2D texture; " +
            "void main (void) {" +
            " gl_FragColor = texture2D(texture, varTexcoord.st, 0.0);" +
            " gl_FragColor.r = varRed * texture2D(texture, varTexcoord.st, 0.0).r;" +
            " gl_FragColor.g = varGreen * texture2D(texture, varTexcoord.st, 0.0).g;" +
            " gl_FragColor.b = varBlue * texture2D(texture, varTexcoord.st, 0.0).b;" +
//            " gl_FragColor.a = varIntensity;" +
            " gl_FragColor.a = varIntensity * texture2D(texture, varTexcoord.st, 0.0).a;" +
            " }";

    private static final String PAINT_MOSAIC = "" +
            "precision highp float;" +
            "varying vec2 varyTextureCoord;" +
            "uniform sampler2D Texture;" +
            "const vec2 TexSize = vec2(400.0, 400.0);" +
            "const vec2 mosaicSize = vec2(10.0, 10.0);" +
            "void main () {" +
            " vec2 intXY = vec2(varyTextureCoord.x * TexSize.x, varyTextureCoord.y * TexSize.y);" +
            " vec2 XYMosaic = vec2(floor(intXY.x/mosaicSize.x) * mosaicSize.x, floor(intXY.y/mosaicSize.y) * mosaicSize.y);" +
            " vec2 UVMosaic = vec2(XYMosaic.x/TexSize.x, XYMosaic.y/TexSize.y);" +
            " vec4 color = texture2D(Texture, UVMosaic);" +
            " gl_FragColor = color;" +
            "}";
    private static final String PAINT_BLIT_VSH = "" +
            "precision highp float; " +
            "uniform mat4 mvpMatrix; " +
            "attribute vec4 inPosition; " +
            "attribute vec2 inTexcoord; " +
            "varying vec2 varTexcoord; " +
            "void main (void) {" +
            " gl_Position = mvpMatrix * inPosition;" +
            " varTexcoord = inTexcoord;" +
            " }";
    private static final String PAINT_BLIT_FSH = "" +
            "precision highp float;" +
            "varying vec2 varTexcoord;" +
            "uniform sampler2D texture;" +
            " void main (void) {" +
            " vec4 tex = texture2D(texture, varTexcoord.st, 0.0);" +
            " gl_FragColor = texture2D(texture, varTexcoord.st, 0.0);" +
            " gl_FragColor.rgb *= gl_FragColor.a;" +
            " }";

    private static final String PAINT_BLITWITHMASK_FSH = "" +
            "precision highp float;" +
            "varying vec2 varTexcoord;" +
            "varying float varRed;" +
            "uniform sampler2D texture;" +
            "uniform sampler2D mask;" +
            "uniform vec4 color;" +
            "void main (void) {" +
            " vec4 dst = texture2D(texture, varTexcoord.st, 0.0);" +
            " vec4 overlay = texture2D(mask, varTexcoord.st, 0.0);" +
//            " float srcAlpha = texture2D(mask, varTexcoord.st, 0.0).a;" +
//            " float outAlpha = srcAlpha + dst.a * (1.0 - srcAlpha);" +
//            " gl_FragColor.rgb = (color.rgb * srcAlpha + dst.rgb * dst.a * (1.0 - srcAlpha)) / outAlpha;" +
//            " gl_FragColor.a = outAlpha;" +
//            " gl_FragColor.rgb *= gl_FragColor.a;" +
            " gl_FragColor = mix(dst, overlay, overlay.a);" +
            " }";
    private static final String PAINT_COMPOSITEWITHMASK_FSH = "" +
            "precision highp float;" +
            "varying vec2 varTexcoord;" +
            "uniform sampler2D mask;" +
            "uniform vec4 color; void main(void) {" +
//            " float alpha = color.a * texture2D(mask, varTexcoord.st, 0.0).a;" +
//            " gl_FragColor.rgb = color.rgb;" +
//            " gl_FragColor.a = alpha;" +
            " gl_FragColor = texture2D(mask, varTexcoord.st, 1.0);" +
            " }";

    private static final String PAINT_NONPREMULTIPLIEDBLIT_FSH = "" +
            "precision highp float;" +
            "varying vec2 varTexcoord;" +
            "uniform sampler2D texture;" +
            "void main (void) {" +
            " gl_FragColor = texture2D(texture, varTexcoord.st, 0.0);" +
            " }";

    private static Map<String, Map<String, Object>> createMap() {
        Map<String, Map<String, Object>> result = new HashMap<>();

        Map<String, Object> shader = new HashMap<>();
        shader.put(VERTEX, PAINT_BRUSH_VSH);
        shader.put(FRAGMENT, PAINT_BRUSH_FSH);
        shader.put(ATTRIBUTES, new String[]{"inPosition", "inTexcoord", "alpha"});
        shader.put(UNIFORMS, new String[]{"mvpMatrix", "texture"});
        result.put("brush", Collections.unmodifiableMap(shader));

        shader = new HashMap<>();
        shader.put(VERTEX, PAINT_BLIT_VSH);
        shader.put(FRAGMENT, PAINT_BLIT_FSH);
        shader.put(ATTRIBUTES, new String[]{"inPosition", "inTexcoord"});
        shader.put(UNIFORMS, new String[]{"mvpMatrix", "texture"});
        result.put("blit", Collections.unmodifiableMap(shader));

        shader = new HashMap<>();
        shader.put(VERTEX, PAINT_BLIT_VSH);
        shader.put(FRAGMENT, PAINT_BLITWITHMASK_FSH);
        shader.put(ATTRIBUTES, new String[]{"inPosition", "inTexcoord"});
        shader.put(UNIFORMS, new String[]{"mvpMatrix", "texture", "mask", "color"});
        result.put("blitWithMask", Collections.unmodifiableMap(shader));

        shader = new HashMap<>();
        shader.put(VERTEX, PAINT_BLIT_VSH);
        shader.put(FRAGMENT, PAINT_COMPOSITEWITHMASK_FSH);
        shader.put(ATTRIBUTES, new String[]{"inPosition", "inTexcoord"});
        shader.put(UNIFORMS, new String[]{"mvpMatrix", "mask", "color"});
        result.put("compositeWithMask", Collections.unmodifiableMap(shader));

        shader = new HashMap<>();
        shader.put(VERTEX, PAINT_BLIT_VSH);
        shader.put(FRAGMENT, PAINT_NONPREMULTIPLIEDBLIT_FSH);
        shader.put(ATTRIBUTES, new String[]{"inPosition", "inTexcoord"});
        shader.put(UNIFORMS, new String[]{"mvpMatrix", "texture"});
        result.put("nonPremultipliedBlit", Collections.unmodifiableMap(shader));

        return Collections.unmodifiableMap(result);
    }

    public static Map<String, Shader> setup() {
        Map<String, Shader> result = new HashMap<>();

        for (Map.Entry<String, Map<String, Object>> entry : AVAILBALBE_SHADERS.entrySet()) {
            Map<String, Object> value = entry.getValue();

            String vertex = (String) value.get(VERTEX);
            String fragment = (String) value.get(FRAGMENT);
            String[] attributes = (String[]) value.get(ATTRIBUTES);
            String[] uniforms = (String[]) value.get(UNIFORMS);

            Shader shader = new Shader(vertex, fragment, attributes, uniforms);
            result.put(entry.getKey(), shader);
        }

        return Collections.unmodifiableMap(result);
    }
}
