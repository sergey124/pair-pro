package org.vors.pairbot.telegram.handler.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.vors.pairbot.model.UserInfo;
import org.vors.pairbot.service.KeyboardService;
import org.vors.pairbot.service.MessageService;
import org.vors.pairbot.service.TimeZoneService;
import org.vors.pairbot.service.UserService;
import org.vors.pairbot.telegram.handler.ChatUpdateHandler;

import java.time.ZoneId;
import java.util.Optional;

import static org.vors.pairbot.constant.UpdateHandlerResult.HANDLING_FINISHED_OK;

@Component
public class SetLocationHandler implements ChatUpdateHandler {

    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;
    @Autowired
    private TimeZoneService timeZoneService;
    @Autowired
    private KeyboardService keyboardService;

    @Override
    public Object handle(Update update) throws TelegramApiException {
        Message message = update.getMessage();
        UserInfo user = userService.getExistingUser(message.getFrom().getId());

        Location location = message.getLocation();

        Optional<ZoneId> maybeZone = timeZoneService.setTimeZone(location.getLatitude(), location.getLongitude(), user);

        String text = "Time zone set as: " + maybeZone.orElse(ZoneId.systemDefault());
        messageService.sendMessage(message.getChatId(), text, keyboardService.removeCustomKeyboard());

        return HANDLING_FINISHED_OK;
    }
}
