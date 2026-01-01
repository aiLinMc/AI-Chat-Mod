package com.ailinmc.chatai;

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
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

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
                    .then(Commands.argument("seed", IntegerArgumentType.integer(0))
                        .then(Commands.argument("prompt", StringArgumentType.greedyString())
                            .executes(context -> {
                                int seed = IntegerArgumentType.getInteger(context, "seed");
                                String prompt = StringArgumentType.getString(context, "prompt");
                                return executeAichatCommand(context, seed, prompt);
                            })
                        )
                    )
                    .then(Commands.argument("prompt", StringArgumentType.greedyString())
                        .executes(context -> {
                            String prompt = StringArgumentType.getString(context, "prompt");
                            int randomSeed = generateRandomSeed();
                            return executeAichatCommand(context, randomSeed, prompt);
                        })
                    )
            );
        }

        private static int executeAichatCommand(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context, int seed, String prompt) {
            CommandSourceStack source = context.getSource();
            String playerName = source.getDisplayName().getString();

            source.sendSuccess(() -> Component.literal("<" + playerName + "> (Seed: " + seed + "): " + prompt), false);

            CompletableFuture.runAsync(() -> {
                try {
                    String response = fetchAIResponse(prompt, seed);
                    source.getServer().execute(() -> {
                        // 检查AI回复是否包含多行内容
                        if (response.contains("\n")) {
                            source.sendSuccess(() -> Component.literal("<Chat AI>\n" + response), false);
                        } else {
                            source.sendSuccess(() -> Component.literal("<Chat AI> " + response), false);
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

            // 先处理多行代码块（```lang\ncode\n```）
            text = processCodeBlocks(text);

            // 处理各级标题
            text = processHeader(text, "^#\\s+(.+)$", "§6§l$1§r");
            text = processHeader(text, "^##\\s+(.+)$", "§c§l$1§r");
            text = processHeader(text, "^###\\s+(.+)$", "§e§l$1§r");
            text = processHeader(text, "^####\\s+(.+)$", "§a§l$1§r");
            text = processHeader(text, "^#####\\s+(.+)$", "§b§l$1§r");
            text = processHeader(text, "^######\\s+(.+)$", "§9§l$1§r");

            // 处理缩进
            text = text.replaceAll("- (.*?)", " · ");
            text = text.replaceAll("  - (.*?)", "  · ");

            // 处理粗体
            text = text.replaceAll("\\*\\*(.*?)\\*\\*", "§l$1§r");
            
            // 处理斜体
            text = text.replaceAll("\\*(.*?)\\*", "§o$1§r");
            text = text.replaceAll("_(.*?)_", "§o$1§r");
            
            // 处理删除线
            text = text.replaceAll("~~(.*?)~~", "§m$1§r");
            
            // 处理下划线
            text = text.replaceAll("__(.*?)__", "§n$1§r");
            
            // 处理行内代码
            text = text.replaceAll("`(.*?)`", "§7$1§r");

            return text;
        }

        private static String processCodeBlocks(String text) {
            // 匹配多行代码块：```[language]\ncode\n```
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