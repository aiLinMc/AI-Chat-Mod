package com.ailinmc.chatai.gui;


import com.ailinmc.chatai.config.ModConfig;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.List;


public class ModConfigScreen extends Screen {
    private final Screen parent;
    
    private Button enableMarkdownButton;
    private Button enableCodeBlocksButton;
    private Button enableTablesButton;
    private Button enableMathButton;
    private Button enableHeadersButton;
    private Button enableListsButton;
    private Button enableBoldButton;
    private Button enableItalicButton;
    private Button enableStrikethroughButton;
    private Button enableUnderlineButton;
    private Button enableInlineCodeButton;
    
    private Button showSeedButton;
    private Button showGenerationTimeButton;
    
    private boolean tempEnableMarkdown;
    private boolean tempEnableCodeBlocks;
    private boolean tempEnableTables;
    private boolean tempEnableMath;
    private boolean tempEnableHeaders;
    private boolean tempEnableLists;
    private boolean tempEnableBold;
    private boolean tempEnableItalic;
    private boolean tempEnableStrikethrough;
    private boolean tempEnableUnderline;
    private boolean tempEnableInlineCode;
    
    private boolean tempShowSeed;
    private boolean tempShowGenerationTime;
    
    private String selectedTab = "markdown";
    
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private int contentHeight = 0;
    
    private List<Button> markdownButtons = new ArrayList<>();
    private List<Component> markdownLabels = new ArrayList<>();
    
    private List<Button> displayButtons = new ArrayList<>();
    private List<Component> displayLabels = new ArrayList<>();
    
    public ModConfigScreen(Screen parent) {
        super(Component.translatable("gui.chatai.config.title"));
        this.parent = parent;
        
        tempEnableMarkdown = ModConfig.enableMarkdown.get();
        tempEnableCodeBlocks = ModConfig.enableCodeBlocks.get();
        tempEnableTables = ModConfig.enableTables.get();
        tempEnableMath = ModConfig.enableMath.get();
        tempEnableHeaders = ModConfig.enableHeaders.get();
        tempEnableLists = ModConfig.enableLists.get();
        tempEnableBold = ModConfig.enableBold.get();
        tempEnableItalic = ModConfig.enableItalic.get();
        tempEnableStrikethrough = ModConfig.enableStrikethrough.get();
        tempEnableUnderline = ModConfig.enableUnderline.get();
        tempEnableInlineCode = ModConfig.enableInlineCode.get();
        
        tempShowSeed = ModConfig.showSeed.get();
        tempShowGenerationTime = ModConfig.showGenerationTime.get();
    }
    
    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
        
        int sidebarWidth = 120;
        int centerX = (this.width + sidebarWidth) / 2;
        int rowHeight = 24;
        int labelX = sidebarWidth + 20;
        int buttonX = this.width - 120;
        
        this.addRenderableWidget(Button.builder(Component.translatable("gui.chatai.config.tab.markdown"), button -> {
            saveCurrentTabChanges();
            selectedTab = "markdown";
            scrollOffset = 0;
            init();
        })
        .bounds(10, 50, sidebarWidth - 20, 20)
        .build());
        
        this.addRenderableWidget(Button.builder(Component.translatable("gui.chatai.config.tab.display"), button -> {
            saveCurrentTabChanges();
            selectedTab = "display";
            scrollOffset = 0;
            init();
        })
        .bounds(10, 80, sidebarWidth - 20, 20)
        .build());
        
        if (selectedTab.equals("markdown")) {
            int startY = 60;
            int tabContentStart = 60;
            int tabContentEnd = this.height - 80;
            
            markdownButtons.clear();
            markdownLabels.clear();
            
            enableMarkdownButton = this.addRenderableWidget(Button.builder(
                    getOnOffText(tempEnableMarkdown), button -> {
                        tempEnableMarkdown = !tempEnableMarkdown;
                        if (tempEnableMarkdown) {
                            tempEnableCodeBlocks = true;
                            tempEnableTables = true;
                            tempEnableMath = true;
                            tempEnableHeaders = true;
                            tempEnableLists = true;
                            tempEnableBold = true;
                            tempEnableItalic = true;
                            tempEnableStrikethrough = true;
                            tempEnableUnderline = true;
                            tempEnableInlineCode = true;
                        } else {
                            tempEnableCodeBlocks = false;
                            tempEnableTables = false;
                            tempEnableMath = false;
                            tempEnableHeaders = false;
                            tempEnableLists = false;
                            tempEnableBold = false;
                            tempEnableItalic = false;
                            tempEnableStrikethrough = false;
                            tempEnableUnderline = false;
                            tempEnableInlineCode = false;
                        }
                        updateMarkdownButtons();
                    })
                .bounds(buttonX, startY, 80, 20)
                .build());
            markdownButtons.add(enableMarkdownButton);
            markdownLabels.add(Component.translatable("chatai.config.enable_markdown").withStyle(ChatFormatting.GOLD));
            
            startY += rowHeight;
            enableCodeBlocksButton = this.addRenderableWidget(Button.builder(
                    getOnOffText(tempEnableCodeBlocks), button -> {
                        tempEnableCodeBlocks = !tempEnableCodeBlocks;
                        if (tempEnableCodeBlocks) {
                            tempEnableMarkdown = true;
                        }
                        updateMarkdownButtons();
                    })
                .bounds(buttonX, startY, 80, 20)
                .build());
            markdownButtons.add(enableCodeBlocksButton);
            markdownLabels.add(Component.translatable("chatai.config.enable_code_blocks"));
            
            startY += rowHeight;
            enableTablesButton = this.addRenderableWidget(Button.builder(
                    getOnOffText(tempEnableTables), button -> {
                        tempEnableTables = !tempEnableTables;
                        if (tempEnableTables) {
                            tempEnableMarkdown = true;
                        }
                        updateMarkdownButtons();
                    })
                .bounds(buttonX, startY, 80, 20)
                .build());
            markdownButtons.add(enableTablesButton);
            markdownLabels.add(Component.translatable("chatai.config.enable_tables"));
            
            startY += rowHeight;
            enableMathButton = this.addRenderableWidget(Button.builder(
                    getOnOffText(tempEnableMath), button -> {
                        tempEnableMath = !tempEnableMath;
                        if (tempEnableMath) {
                            tempEnableMarkdown = true;
                        }
                        updateMarkdownButtons();
                    })
                .bounds(buttonX, startY, 80, 20)
                .build());
            markdownButtons.add(enableMathButton);
            markdownLabels.add(Component.translatable("chatai.config.enable_math"));
            
            startY += rowHeight;
            enableHeadersButton = this.addRenderableWidget(Button.builder(
                    getOnOffText(tempEnableHeaders), button -> {
                        tempEnableHeaders = !tempEnableHeaders;
                                               if (tempEnableHeaders) {
                            tempEnableMarkdown = true;
                        }
                        updateMarkdownButtons();
                    })
                .bounds(buttonX, startY, 80, 20)
                .build());
            markdownButtons.add(enableHeadersButton);
            markdownLabels.add(Component.translatable("chatai.config.enable_headers"));
            
            startY += rowHeight;
            enableListsButton = this.addRenderableWidget(Button.builder(
                    getOnOffText(tempEnableLists), button -> {
                        tempEnableLists = !tempEnableLists;
                        if (tempEnableLists) {
                            tempEnableMarkdown = true;
                        }
                        updateMarkdownButtons();
                    })
                .bounds(buttonX, startY, 80, 20)
                .build());
            markdownButtons.add(enableListsButton);
            markdownLabels.add(Component.translatable("chatai.config.enable_lists"));
            
            startY += rowHeight;
            enableBoldButton = this.addRenderableWidget(Button.builder(
                    getOnOffText(tempEnableBold), button -> {
                        tempEnableBold = !tempEnableBold;
                        if (tempEnableBold) {
                            tempEnableMarkdown = true;
                        }
                        updateMarkdownButtons();
                    })
                .bounds(buttonX, startY, 80, 20)
                .build());
            markdownButtons.add(enableBoldButton);
            markdownLabels.add(Component.translatable("chatai.config.enable_bold"));
            
            startY += rowHeight;
            enableItalicButton = this.addRenderableWidget(Button.builder(
                    getOnOffText(tempEnableItalic), button -> {
                        tempEnableItalic = !tempEnableItalic;
                        if (tempEnableItalic) {
                            tempEnableMarkdown = true;
                        }
                        updateMarkdownButtons();
                    })
                .bounds(buttonX, startY, 80, 20)
                .build());
            markdownButtons.add(enableItalicButton);
            markdownLabels.add(Component.translatable("chatai.config.enable_italic"));
            
            startY += rowHeight;
            enableStrikethroughButton = this.addRenderableWidget(Button.builder(
                    getOnOffText(tempEnableStrikethrough), button -> {
                        tempEnableStrikethrough = !tempEnableStrikethrough;
                        if (tempEnableStrikethrough) {
                            tempEnableMarkdown = true;
                        }
                        updateMarkdownButtons();
                    })
                .bounds(buttonX, startY, 80, 20)
                .build());
            markdownButtons.add(enableStrikethroughButton);
            markdownLabels.add(Component.translatable("chatai.config.enable_strikethrough"));
            
            startY += rowHeight;
            enableUnderlineButton = this.addRenderableWidget(Button.builder(
                    getOnOffText(tempEnableUnderline), button -> {
                        tempEnableUnderline = !tempEnableUnderline;
                        if (tempEnableUnderline) {
                            tempEnableMarkdown = true;
                        }
                        updateMarkdownButtons();
                    })
                .bounds(buttonX, startY, 80, 20)
                .build());
            markdownButtons.add(enableUnderlineButton);
            markdownLabels.add(Component.translatable("chatai.config.enable_underline"));
            
            startY += rowHeight;
            enableInlineCodeButton = this.addRenderableWidget(Button.builder(
                    getOnOffText(tempEnableInlineCode), button -> {
                        tempEnableInlineCode = !tempEnableInlineCode;
                        if (tempEnableInlineCode) {
                            tempEnableMarkdown = true;
                        }
                        updateMarkdownButtons();
                    })
                .bounds(buttonX, startY, 80, 20)
                .build());
            markdownButtons.add(enableInlineCodeButton);
            markdownLabels.add(Component.translatable("chatai.config.enable_inline_code"));
            
            contentHeight = startY - tabContentStart + 20;
            maxScrollOffset = Math.max(0, contentHeight - (tabContentEnd - tabContentStart));
            scrollOffset = Math.min(scrollOffset, maxScrollOffset);
            
            updateMarkdownButtons();
        }
        
        if (selectedTab.equals("display")) {
            int startY = 60;
            int tabContentStart = 60;
            int tabContentEnd = this.height - 80;
            
            displayButtons.clear();
            displayLabels.clear();
            
            showSeedButton = this.addRenderableWidget(Button.builder(
                    getOnOffText(tempShowSeed), button -> {
                        tempShowSeed = !tempShowSeed;
                        updateDisplayButtons();
                    })
                .bounds(buttonX, startY, 80, 20)
                .build());
            displayButtons.add(showSeedButton);
            displayLabels.add(Component.translatable("chatai.config.show_seed"));
            
            startY += rowHeight;
            showGenerationTimeButton = this.addRenderableWidget(Button.builder(
                    getOnOffText(tempShowGenerationTime), button -> {
                        tempShowGenerationTime = !tempShowGenerationTime;
                        updateDisplayButtons();
                    })
                .bounds(buttonX, startY, 80, 20)
                .build());
            displayButtons.add(showGenerationTimeButton);
            displayLabels.add(Component.translatable("chatai.config.show_generation_time"));
            
            contentHeight = startY - tabContentStart + 20;
            maxScrollOffset = Math.max(0, contentHeight - (tabContentEnd - tabContentStart));
            scrollOffset = Math.min(scrollOffset, maxScrollOffset);
            
            updateDisplayButtons();
        }
        
        int saveCancelY = this.height - 45;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.chatai.config.save"), button -> saveSettings())
                .bounds(this.width - 220, saveCancelY, 100, 20)
                .build());
        
        this.addRenderableWidget(Button.builder(Component.translatable("gui.chatai.config.cancel"), button -> onClose())
                .bounds(this.width - 110, saveCancelY, 100, 20)
                .build());
    }
    
    private Component getOnOffText(boolean enabled) {
        return Component.translatable(enabled ? "gui.chatai.config.on" : "gui.chatai.config.off");
    }
    
    private void updateMarkdownButtons() {
        enableMarkdownButton.setMessage(getOnOffText(tempEnableMarkdown));
        enableCodeBlocksButton.setMessage(getOnOffText(tempEnableCodeBlocks));
        enableTablesButton.setMessage(getOnOffText(tempEnableTables));
        enableMathButton.setMessage(getOnOffText(tempEnableMath));
        enableHeadersButton.setMessage(getOnOffText(tempEnableHeaders));
        enableListsButton.setMessage(getOnOffText(tempEnableLists));
        enableBoldButton.setMessage(getOnOffText(tempEnableBold));
        enableItalicButton.setMessage(getOnOffText(tempEnableItalic));
        enableStrikethroughButton.setMessage(getOnOffText(tempEnableStrikethrough));
        enableUnderlineButton.setMessage(getOnOffText(tempEnableUnderline));
        enableInlineCodeButton.setMessage(getOnOffText(tempEnableInlineCode));
    }
    
    private void updateDisplayButtons() {
        showSeedButton.setMessage(getOnOffText(tempShowSeed));
        showGenerationTimeButton.setMessage(getOnOffText(tempShowGenerationTime));
    }
    
    private void saveCurrentTabChanges() {
    }
    
    private void saveSettings() {
        saveCurrentTabChanges();
        
        ModConfig.enableMarkdown.set(tempEnableMarkdown);
        ModConfig.enableCodeBlocks.set(tempEnableCodeBlocks);
        ModConfig.enableTables.set(tempEnableTables);
        ModConfig.enableMath.set(tempEnableMath);
        ModConfig.enableHeaders.set(tempEnableHeaders);
        ModConfig.enableLists.set(tempEnableLists);
        ModConfig.enableBold.set(tempEnableBold);
        ModConfig.enableItalic.set(tempEnableItalic);
        ModConfig.enableStrikethrough.set(tempEnableStrikethrough);
        ModConfig.enableUnderline.set(tempEnableUnderline);
        ModConfig.enableInlineCode.set(tempEnableInlineCode);
        
        ModConfig.showSeed.set(tempShowSeed);
        ModConfig.showGenerationTime.set(tempShowGenerationTime);
        
        ModConfig.SPEC.save();
        
        this.onClose();
    }
    
    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int sidebarWidth = 120;
        int tabContentStart = 60;
        int tabContentEnd = this.height - 80;
        
        if (mouseX > sidebarWidth && mouseY > tabContentStart && mouseY < tabContentEnd) {
            int delta = (int) (verticalAmount * -10);
            scrollOffset += delta;
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        int sidebarWidth = 120;
        int centerX = (this.width + sidebarWidth) / 2;
        int labelX = sidebarWidth + 20;
        int rowHeight = 24;
        int tabContentStart = 60;
        int tabContentEnd = this.height - 80;
        
        for (int i = 0; i < markdownButtons.size(); i++) {
            Button button = markdownButtons.get(i);
            int buttonY = 60 + i * rowHeight - scrollOffset;
            
            if (buttonY >= tabContentStart - 20 && buttonY <= tabContentEnd) {
                button.setY(buttonY);
            } else {
                button.setY(-1000);
            }
        }
        
        for (int i = 0; i < displayButtons.size(); i++) {
            Button button = displayButtons.get(i);
            int buttonY = 60 + i * rowHeight - scrollOffset;
            
            if (buttonY >= tabContentStart - 20 && buttonY <= tabContentEnd) {
                button.setY(buttonY);
            } else {
                button.setY(-1000);
            }
        }
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        guiGraphics.fill(0, 0, sidebarWidth, this.height, 0x80000000);
        guiGraphics.fill(sidebarWidth, 0, sidebarWidth + 1, this.height, 0xFF000000);
        
        guiGraphics.drawCenteredString(this.font, this.title, sidebarWidth / 2, 20, 0xFFFFFF);
        
        if (selectedTab.equals("markdown")) {
            guiGraphics.fill(10, 50, sidebarWidth - 10, 70, 0x4000FF00);
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.chatai.config.tab.markdown"), centerX, 25, 0xFFFFFF);
            
            for (int i = 0; i < markdownButtons.size(); i++) {
                Button button = markdownButtons.get(i);
                Component label = markdownLabels.get(i);
                
                int buttonY = 60 + i * rowHeight - scrollOffset;
                
                if (buttonY >= tabContentStart - 20 && buttonY <= tabContentEnd) {
                    guiGraphics.drawString(this.font, label, labelX, buttonY + 6, 0xFFFFFF);
                }
            }
            
            if (maxScrollOffset > 0) {
                int scrollbarX = this.width - 20;
                int scrollbarHeight = tabContentEnd - tabContentStart;
                int scrollbarThumbHeight = Math.max(20, (int) ((scrollbarHeight * 1.0 / contentHeight) * scrollbarHeight));
                int scrollbarThumbY = tabContentStart + (int) ((scrollOffset * 1.0 / maxScrollOffset) * (scrollbarHeight - scrollbarThumbHeight));
                
                guiGraphics.fill(scrollbarX, tabContentStart, scrollbarX + 8, tabContentEnd, 0x30000000);
                guiGraphics.fill(scrollbarX, scrollbarThumbY, scrollbarX + 8, scrollbarThumbY + scrollbarThumbHeight, 0x80FFFFFF);
            }
        }
        
        if (selectedTab.equals("display")) {
            guiGraphics.fill(10, 80, sidebarWidth - 10, 100, 0x4000FF00);
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.chatai.config.tab.display"), centerX, 25, 0xFFFFFF);
            
            for (int i = 0; i < displayButtons.size(); i++) {
                Button button = displayButtons.get(i);
                Component label = displayLabels.get(i);
                
                int buttonY = 60 + i * rowHeight - scrollOffset;
                
                if (buttonY >= tabContentStart - 20 && buttonY <= tabContentEnd) {
                    guiGraphics.drawString(this.font, label, labelX, buttonY + 6, 0xFFFFFF);
                }
            }
            
            if (maxScrollOffset > 0) {
                int scrollbarX = this.width - 20;
                int scrollbarHeight = tabContentEnd - tabContentStart;
                int scrollbarThumbHeight = Math.max(20, (int) ((scrollbarHeight * 1.0 / contentHeight) * scrollbarHeight));
                int scrollbarThumbY = tabContentStart + (int) ((scrollOffset * 1.0 / maxScrollOffset) * (scrollbarHeight - scrollbarThumbHeight));
                
                guiGraphics.fill(scrollbarX, tabContentStart, scrollbarX + 8, tabContentEnd, 0x30000000);
                guiGraphics.fill(scrollbarX, scrollbarThumbY, scrollbarX + 8, scrollbarThumbY + scrollbarThumbHeight, 0x80FFFFFF);
            }
        }
    }
    
    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
