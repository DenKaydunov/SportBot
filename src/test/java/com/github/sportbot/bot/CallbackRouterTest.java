package com.github.sportbot.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallbackRouterTest {

    @Mock
    private SubscriptionHandler subscriptionHandler;

    @Mock
    private PaginationHandler paginationHandler;

    @Mock
    private SportBot bot;

    @InjectMocks
    private CallbackRouter callbackRouter;

    private Update update;
    private CallbackQuery callbackQuery;

    @BeforeEach
    void setUp() {
        update = new Update();
        callbackQuery = new CallbackQuery();
        callbackQuery.setId("callback-123");
        update.setCallbackQuery(callbackQuery);
    }

    @Test
    void route_whenPageNext_shouldCallPaginationHandler() {
        // Given
        callbackQuery.setData("page_next_5");

        // When
        callbackRouter.route(update, bot);

        // Then
        verify(paginationHandler).handlePagination(update, bot);
        verifyNoInteractions(subscriptionHandler);
    }

    @Test
    void route_whenPagePrev_shouldCallPaginationHandler() {
        // Given
        callbackQuery.setData("page_prev_3");

        // When
        callbackRouter.route(update, bot);

        // Then
        verify(paginationHandler).handlePagination(update, bot);
        verifyNoInteractions(subscriptionHandler);
    }

    @Test
    void route_whenSubscription_shouldCallSubscriptionHandler() {
        // Given
        callbackQuery.setData("sub_123456");

        // When
        callbackRouter.route(update, bot);

        // Then
        verify(subscriptionHandler).handleSubscription(update, bot);
        verifyNoInteractions(paginationHandler);
    }

    @Test
    void route_whenUnsubscription_shouldCallUnsubscriptionHandler() {
        // Given
        callbackQuery.setData("unsub_789012");

        // When
        callbackRouter.route(update, bot);

        // Then
        verify(subscriptionHandler).handleUnsubscription(update, bot);
        verifyNoInteractions(paginationHandler);
    }

    @Test
    void route_whenPageCurrent_shouldAnswerCallback() {
        // Given
        callbackQuery.setData("page_current");

        // When
        callbackRouter.route(update, bot);

        // Then
        verify(bot).answerCallback(eq("callback-123"), any(String.class));
        verifyNoInteractions(subscriptionHandler);
        verifyNoInteractions(paginationHandler);
    }

    @Test
    void route_whenUnknownCallback_shouldAnswerWithUnknownMessage() {
        // Given
        callbackQuery.setData("unknown_action");

        // When
        callbackRouter.route(update, bot);

        // Then
        verify(bot).answerCallback(eq("callback-123"), any(String.class));
        verifyNoInteractions(subscriptionHandler);
        verifyNoInteractions(paginationHandler);
    }

    @Test
    void route_whenPaginationInSubscriptionCallback_shouldCallPaginationHandler() {
        // Given - pagination has priority
        callbackQuery.setData("sub_page_next_5");

        // When
        callbackRouter.route(update, bot);

        // Then
        verify(paginationHandler).handlePagination(update, bot);
        verifyNoInteractions(subscriptionHandler);
    }
}
