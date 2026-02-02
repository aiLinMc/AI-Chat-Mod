package com.ailinmc.chatai.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;
    
    public static final ModConfigSpec.BooleanValue enableMarkdown;
    public static final ModConfigSpec.BooleanValue enableCodeBlocks;
    public static final ModConfigSpec.BooleanValue enableTables;
    public static final ModConfigSpec.BooleanValue enableMath;
    public static final ModConfigSpec.BooleanValue enableHeaders;
    public static final ModConfigSpec.BooleanValue enableLists;
    public static final ModConfigSpec.BooleanValue enableBold;
    public static final ModConfigSpec.BooleanValue enableItalic;
    public static final ModConfigSpec.BooleanValue enableStrikethrough;
    public static final ModConfigSpec.BooleanValue enableUnderline;
    public static final ModConfigSpec.BooleanValue enableInlineCode;
    
    public static final ModConfigSpec.BooleanValue showSeed;
    public static final ModConfigSpec.BooleanValue showGenerationTime;
    
    static {
        BUILDER.push("Markdown Settings");
        
        enableMarkdown = BUILDER
            .comment("Enable or disable all markdown formatting")
            .translation("chatai.config.enable_markdown")
            .define("enableMarkdown", true);
        
        enableCodeBlocks = BUILDER
            .comment("Enable code block formatting (```code```)")
            .translation("chatai.config.enable_code_blocks")
            .define("enableCodeBlocks", true);
        
        enableTables = BUILDER
            .comment("Enable table formatting")
            .translation("chatai.config.enable_tables")
            .define("enableTables", true);
        
        enableMath = BUILDER
            .comment("Enable math formula formatting ($formula$)")
            .translation("chatai.config.enable_math")
            .define("enableMath", true);
        
        enableHeaders = BUILDER
            .comment("Enable header formatting (# Header)")
            .translation("chatai.config.enable_headers")
            .define("enableHeaders", true);
        
        enableLists = BUILDER
            .comment("Enable list formatting (- item)")
            .translation("chatai.config.enable_lists")
            .define("enableLists", true);
        
        enableBold = BUILDER
            .comment("Enable bold text formatting (**text**)")
            .translation("chatai.config.enable_bold")
            .define("enableBold", true);
        
        enableItalic = BUILDER
            .comment("Enable italic text formatting (*text* or _text_)")
            .translation("chatai.config.enable_italic")
            .define("enableItalic", true);
        
        enableStrikethrough = BUILDER
            .comment("Enable strikethrough text formatting (~~text~~)")
            .translation("chatai.config.enable_strikethrough")
            .define("enableStrikethrough", true);
        
        enableUnderline = BUILDER
            .comment("Enable underline text formatting (__text__)")
            .translation("chatai.config.enable_underline")
            .define("enableUnderline", true);
        
        enableInlineCode = BUILDER
            .comment("Enable inline code formatting (`code`)")
            .translation("chatai.config.enable_inline_code")
            .define("enableInlineCode", true);
        
        BUILDER.pop();
        
        BUILDER.push("Display Settings");
        
        showSeed = BUILDER
            .comment("Show seed in chat message")
            .translation("chatai.config.show_seed")
            .define("showSeed", true);
        
        showGenerationTime = BUILDER
            .comment("Show generation time after AI response")
            .translation("chatai.config.show_generation_time")
            .define("showGenerationTime", true);
        
        BUILDER.pop();
        
        SPEC = BUILDER.build();
    }
}
