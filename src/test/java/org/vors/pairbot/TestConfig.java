package org.vors.pairbot;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.telegram.telegrambots.starter.TelegramBotInitializer;
import org.vors.pairbot.telegram.PairBot;

@TestConfiguration
public class TestConfig {
    @MockBean
    public PairBot pairBot;

    @MockBean
    public TelegramBotInitializer telegramBotInitializer;

}