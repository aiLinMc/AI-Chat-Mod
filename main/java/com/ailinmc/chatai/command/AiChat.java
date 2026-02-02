package com.ailinmc.chatai.command;

import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.Arrays;

import com.ailinmc.chatai.config.ModConfig;
import com.ailinmc.chatai.gui.ModConfigScreen;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class AiChat {
    public AiChat() {
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        new AiChat();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientLoad(FMLClientSetupEvent event) {
    }

    @EventBusSubscriber
    private static class AiChatForgeBusEvents {
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
            dispatcher.register(
                Commands.literal("aichat")
                    .then(Commands.argument("seed", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            builder.suggest("random");
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("prompt", StringArgumentType.greedyString())
                            .executes(context -> {
                                String seedString = StringArgumentType.getString(context, "seed");
                                String prompt = StringArgumentType.getString(context, "prompt");
                                return executeAichatCommand(context, seedString, prompt);
                            })
                        )
                    )
            );
        }

        private static int executeAichatCommand(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context, String seedString, String prompt) {
            CommandSourceStack source = context.getSource();
            
            // 命令是否来自命令方块
            if (source.getEntity() == null) {
                source.sendFailure(Component.translatable("message.chatai.disable_command_block"));
                return 0;
            }
            
            String playerName = source.getDisplayName().getString();

            int seed;
            String displaySeed;
            if ("random".equalsIgnoreCase(seedString.trim())) {
                seed = generateRandomSeed();
                displaySeed = "random(" + seed + ")";
            } else {
                try {
                    seed = Integer.parseInt(seedString.trim());
                    displaySeed = String.valueOf(seed);
                } catch (NumberFormatException e) {
                    source.sendFailure(Component.translatable("message.chatai.seed_error"));
                    return 0;
                }
            }

            String userMessage;
            if (ModConfig.showSeed.get()) {
                userMessage = "<" + playerName + "> (Seed: " + displaySeed + "): " + prompt;
            } else {
                userMessage = "<" + playerName + "> " + prompt;
            }
            source.sendSuccess(() -> Component.literal(userMessage), false);

            long startTime = System.currentTimeMillis();
            
            CompletableFuture.runAsync(() -> {
                try {
                    String response = fetchAIResponse(prompt, seed);
                    long endTime = System.currentTimeMillis();
                    long generationTime = endTime - startTime;
                    
                    source.getServer().execute(() -> {
                        // AI回复是否包含多行内容
                        if (response.contains("\n")) {
                            source.sendSuccess(() -> Component.literal("<Chat AI>\n" + response), false);
                        } else {
                            source.sendSuccess(() -> Component.literal("<Chat AI> " + response), false);
                        }
                        
                        if (ModConfig.showGenerationTime.get()) {
                            source.sendSuccess(() -> Component.translatable("message.chatai.generation_time", generationTime), false);
                        }
                        
                        source.sendSuccess(() -> Component.literal(""), false);
                    });
                } catch (Exception e) {
                    source.getServer().execute(() -> {
                        source.sendFailure(Component.translatable("message.chatai.timeout"));
                    });
                }
            });
            return 1;
        }

        private static int generateRandomSeed() {
            Random random = new Random();
            return Math.abs(random.nextInt());
        }

        private static String processMarkdown(String text) {
            if (text == null || text.isEmpty()) {
                return text;
            }

            if (!ModConfig.enableMarkdown.get()) {
                return text;
            }

            if (ModConfig.enableCodeBlocks.get()) {
                text = processCodeBlocks(text);
            }
            if (ModConfig.enableTables.get()) {
                text = processTables(text);
            }
            if (ModConfig.enableMath.get()) {
                text = processMath(text);
            }
            if (ModConfig.enableHeaders.get()) {
                text = processHeader(text, "^#\\s+(.+)$", "§6§l$1§r");
                text = processHeader(text, "^##\\s+(.+)$", "§c§l$1§r");
                text = processHeader(text, "^###\\s+(.+)$", "§e§l$1§r");
                text = processHeader(text, "^####\\s+(.+)$", "§a§l$1§r");
                text = processHeader(text, "^#####\\s+(.+)$", "§b§l$1§r");
                text = processHeader(text, "^######\\s+(.+)$", "§9§l$1§r");
            }
            if (ModConfig.enableLists.get()) {
                text = text.replaceAll("- (.*?)", " · ");
                text = text.replaceAll("  - (.*?)", "  · ");
            }
            if (ModConfig.enableBold.get()) {
                text = text.replaceAll("\\*\\*(.*?)\\*\\*", "§l$1§r");
            }
            if (ModConfig.enableItalic.get()) {
                text = text.replaceAll("\\*(.*?)\\*", "§o$1§r");
                text = text.replaceAll("_(.*?)_", "§o$1§r");
            }
            if (ModConfig.enableStrikethrough.get()) {
                text = text.replaceAll("~~(.*?)~~", "§m$1§r");
            }
            if (ModConfig.enableUnderline.get()) {
                text = text.replaceAll("__(.*?)__", "§n$1§r");
            }
            if (ModConfig.enableInlineCode.get()) {
                text = text.replaceAll("`(.*?)`", "§7$1§r");
            }

            return text;
        }

        private static String processCodeBlocks(String text) {
            // 匹配多行代码块
            Pattern pattern = Pattern.compile("```([a-zA-Z0-9]*)\\n(.*?)```", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(text);
            StringBuffer sb = new StringBuffer();
            
            while (matcher.find()) {
                String language = matcher.group(1);
                String code = matcher.group(2);
                
                // 处理代码内容，保持缩进
                String[] lines = code.split("\n");
                StringBuilder processedCode = new StringBuilder();
                int minIndent = Integer.MAX_VALUE;
                
                // 找出最小缩进
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    int indent = 0;
                    for (char c : line.toCharArray()) {
                        if (c == ' ') {
                            indent++;
                        } else {
                            break;
                        }
                    }
                    minIndent = Math.min(minIndent, indent);
                }
                
                // 移除公共缩进并处理
                for (String line : lines) {
                    if (line.trim().isEmpty()) {
                        processedCode.append("\n");
                    } else {
                        String trimmedLine = line.substring(Math.min(minIndent, line.length()));
                        processedCode.append("§7").append(trimmedLine).append("§r\n");
                    }
                }
                
                // 构建最终显示格式
                StringBuilder formattedCode = new StringBuilder();
                if (!language.isEmpty()) {
                    formattedCode.append("§8┌─ §2").append(language).append(" §8─┐§r\n");
                } else {
                    formattedCode.append("§8┌─────┐§r\n");
                }
                
                // 添加代码行
                String[] processedLines = processedCode.toString().split("\n");
                for (String line : processedLines) {
                    if (line.trim().isEmpty()) {
                        formattedCode.append("§8│     │§r\n");
                    } else {
                        // 限制行长度以适应聊天框
                        String displayLine = line;
                        if (displayLine.length() > 35) {
                            displayLine = displayLine.substring(0, 32) + "...";
                        }
                        formattedCode.append("§8│ §7").append(displayLine).append("§8 │§r\n");
                    }
                }
                
                formattedCode.append("§8└─────┘§r");
                
                matcher.appendReplacement(sb, formattedCode.toString());
            }
            matcher.appendTail(sb);
            
            return sb.toString();
        }

        private static String processHeader(String text, String pattern, String replacement) {
            Pattern p = Pattern.compile(pattern, Pattern.MULTILINE);
            Matcher m = p.matcher(text);
            StringBuffer sb = new StringBuffer();
            
            while (m.find()) {
                m.appendReplacement(sb, replacement);
            }
            m.appendTail(sb);
            
            return sb.toString();
        }

        private static String processTables(String text) {
            // 匹配完整的表格
            Pattern tablePattern = Pattern.compile("(\\|.*?\\|\\s*\\n(?:\\|.*?\\|\\s*\\n)*)", Pattern.MULTILINE | Pattern.DOTALL);
            Matcher matcher = tablePattern.matcher(text);
            StringBuffer sb = new StringBuffer();
            
            while (matcher.find()) {
                String tableText = matcher.group(1);
                String formattedTable = formatTable(tableText);
                matcher.appendReplacement(sb, formattedTable);
            }
            matcher.appendTail(sb);
            
            return sb.toString();
        }

        private static String formatTable(String tableText) {
            String[] lines = tableText.trim().split("\n");
            if (lines.length < 2) {
                return tableText;
            }

            // 解析表格数据
            String[][] cells = new String[lines.length][];
            int[] columnWidths;
            String[][] alignments = new String[lines.length][];
            
            // 解析第一行（表头）
            cells[0] = parseTableLine(lines[0]);
            if (cells[0] == null || cells[0].length < 2) {
                return tableText;
            }
            
            // 解析第二行（对齐行）
            alignments[1] = parseTableLine(lines[1]);
            if (alignments[1] == null || alignments[1].length != cells[0].length) {
                return tableText;
            }
            
            // 计算列宽
            columnWidths = new int[cells[0].length];
            for (int col = 0; col < cells[0].length; col++) {
                columnWidths[col] = cells[0][col].length();
            }
            
            // 解析数据行
            for (int i = 2; i < lines.length; i++) {
                cells[i] = parseTableLine(lines[i]);
                if (cells[i] != null && cells[i].length == cells[0].length) {
                    for (int col = 0; col < cells[i].length; col++) {
                        columnWidths[col] = Math.max(columnWidths[col], cells[i][col].length());
                    }
                }
            }
            
            // 限制列宽以适应聊天框
            for (int i = 0; i < columnWidths.length; i++) {
                columnWidths[i] = Math.min(columnWidths[i], 12); // 最大宽度12字符
            }
            
            // 生成格式化表格
            StringBuilder result = new StringBuilder();
            result.append("§8┌");
            for (int col = 0; col < columnWidths.length; col++) {
                result.append("─".repeat(Math.max(1, columnWidths[col] + 2)));
                if (col < columnWidths.length - 1) {
                    result.append("┬");
                }
            }
            result.append("┐§r\n");
            
            // 表头行
            result.append("§8│§r ");
            for (int col = 0; col < cells[0].length; col++) {
                String cell = cells[0][col];
                if (cell.length() > columnWidths[col]) {
                    cell = cell.substring(0, columnWidths[col] - 3) + "...";
                }
                result.append("§f").append(padCell(cell, columnWidths[col], "center")).append("§8 │§r ");
            }
            result.append("\n");
            
            // 分隔线
            result.append("§8├");
            for (int col = 0; col < columnWidths.length; col++) {
                result.append("─".repeat(Math.max(1, columnWidths[col] + 2)));
                if (col < columnWidths.length - 1) {
                    result.append("┼");
                }
            }
            result.append("┤§r\n");
            
            // 数据行
            for (int i = 2; i < lines.length; i++) {
                if (cells[i] != null && cells[i].length == cells[0].length) {
                    result.append("§8│§r ");
                    for (int col = 0; col < cells[i].length; col++) {
                        String cell = cells[i][col];
                        if (cell.length() > columnWidths[col]) {
                            cell = cell.substring(0, columnWidths[col] - 3) + "...";
                        }
                        String alignment = alignments[1][col];
                        String alignType = "left";
                        if (alignment.contains(":")) {
                            if (alignment.startsWith(":") && alignment.endsWith(":")) {
                                alignType = "center";
                            } else if (alignment.endsWith(":")) {
                                alignType = "right";
                            }
                        }
                        result.append("§7").append(padCell(cell, columnWidths[col], alignType)).append("§8 │§r ");
                    }
                    result.append("\n");
                }
            }
            
            // 底边框
            result.append("§8└");
            for (int col = 0; col < columnWidths.length; col++) {
                result.append("─".repeat(Math.max(1, columnWidths[col] + 2)));
                if (col < columnWidths.length - 1) {
                    result.append("┴");
                }
            }
            result.append("┘§r");
            
            return result.toString();
        }

        private static String processMath(String text) {
            if (text == null || text.isEmpty()) {
                return text;
            }

            // 处理块级数学公式
            text = processBlockMath(text);
            
            // 处理行内数学公式
            text = processInlineMath(text);

            return text;
        }

        private static String processBlockMath(String text) {
            // 匹配块级数学公式
            Pattern pattern = Pattern.compile("\\$\\$([\\s\\S]*?)\\$\\$", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(text);
            StringBuffer sb = new StringBuffer();
            
            while (matcher.find()) {
                String mathContent = matcher.group(1).trim();
                String formattedMath = formatBlockMath(mathContent);
                matcher.appendReplacement(sb, formattedMath);
            }
            matcher.appendTail(sb);
            
            return sb.toString();
        }

        private static String processInlineMath(String text) {
            // 匹配行内数学公式
            Pattern pattern = Pattern.compile("\\$([^$\\n]+?)\\$");
            Matcher matcher = pattern.matcher(text);
            StringBuffer sb = new StringBuffer();
            
            while (matcher.find()) {
                String mathContent = matcher.group(1).trim();
                String formattedMath = formatInlineMath(mathContent);
                matcher.appendReplacement(sb, formattedMath);
            }
            matcher.appendTail(sb);
            
            return sb.toString();
        }

        private static String formatBlockMath(String mathContent) {
            // 分割成多行
            String[] lines = mathContent.split("\n");
            StringBuilder result = new StringBuilder();
            
            // 顶边框
            result.append("§8┌");
            int maxWidth = Math.min(32, mathContent.length()); // 限制最大宽度
            result.append("─".repeat(Math.max(1, maxWidth)));
            result.append("┐§r\n");
            
            // 数学内容行
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    result.append("§8").append(" ".repeat(Math.max(1, maxWidth))).append("§r\n");
                } else {
                    // 限制行长度
                    String displayLine = line;
                    if (displayLine.length() > 60) {
                        displayLine = displayLine.substring(0, 57) + "...";
                    }
                    
                    // 居中对齐
                    //int padding = maxWidth - displayLine.length();
                    int padding = 2;
                    int leftPad = padding / 2;
                    int rightPad = padding - leftPad;
                    
                    result.append("   ".repeat(leftPad))
                          .append("§d§l").append(displayLine).append("§r")
                          .append(" ".repeat(rightPad))
                          .append("§r\n");
                }
            }
            
            // 底边框
            result.append("§8└")
                  .append("─".repeat(Math.max(1, maxWidth)))
                  .append("┘§r");
            
            return result.toString();
        }

        private static String formatInlineMath(String mathContent) {
            String processed = mathContent;
            
            // 处理常见的数学符号和操作符
            processed = processed.replace("∑", "∑")
                                .replace("∫", "∫")
                                .replace("√", "√")
                                .replace("π", "π")
                                .replace("∞", "∞")
                                .replace("≤", "≤")
                                .replace("≥", "≥")
                                .replace("≠", "≠")
                                .replace("≈", "≈")
                                .replace("→", "→")
                                .replace("←", "←")
                                .replace("↑", "↑")
                                .replace("↓", "↓")
                                .replace("±", "±")
                                .replace("×", "×")
                                .replace("÷", "÷")
                                .replace("²", "²")
                                .replace("³", "³")
                                .replace("^2", "²")
                                .replace("^3", "³");
            
            // 处理分数形式
            processed = processed.replaceAll("(\\d+)/(\\d+)", "$1⁄$2"); // 使用分数斜杠
            
            // 处理上标
            processed = processed.replaceAll("\\^(\\w)", "§l$1§r");
            
            // 处理下标
            processed = processed.replaceAll("_(\\w)", "§o$1§r");
            
            // 限制长度以适应行内显示
            if (processed.length() > 50) {
                processed = processed.substring(0, 47) + "...";
            }
            
            return "§d§l" + processed + "§r";
        }

        private static String[] parseTableLine(String line) {
            // 移除首尾的|并分割
            line = line.trim();
            if (line.startsWith("|")) {
                line = line.substring(1);
            }
            if (line.endsWith("|")) {
                line = line.substring(0, line.length() - 1);
            }
            
            String[] cells = line.split("\\|");
            for (int i = 0; i < cells.length; i++) {
                cells[i] = cells[i].trim();
            }
            
            return cells;
        }

        private static String padCell(String content, int width, String alignment) {
            if (content.length() >= width) {
                return content;
            }
            
            int padding = width - content.length();
            switch (alignment) {
                case "center":
                    int leftPad = padding / 2;
                    int rightPad = padding - leftPad;
                    return " ".repeat(leftPad) + content + " ".repeat(rightPad);
                case "right":
                    return " ".repeat(padding) + content;
                default: // left
                    return content + " ".repeat(padding);
            }
        }

        private static String fetchAIResponse(String prompt, int seed) throws Exception {
            String systemInstruction = "You are an AI need to answer user input. You can answer in whatever language the user inputs. The seed is " + seed + ". The following is the user's input:     ";
            String fullPrompt = systemInstruction + prompt;

            String encodedPrompt = URLEncoder.encode(fullPrompt, StandardCharsets.UTF_8);
            String apiUrl = "https://text.pollinations.ai/" + encodedPrompt + "?model=openai";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("User-Agent", "Minecraft Mod")
                    .timeout(java.time.Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = client.send(
                request, 
                HttpResponse.BodyHandlers.ofString()
            );

            // 检查HTTP状态码
            if (response.statusCode() != 200) {
                // 抛出带本地化键标识的自定义异常
                throw new Exception("message.chatai.timeout");
            }
            
            String aiResponse = response.body();
            return processMarkdown(aiResponse);
        }
    }
}