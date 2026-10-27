package com.caseflow.ticket;

import com.caseflow.common.ApiException;
import com.caseflow.operator.OperatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {
    @Mock TicketRepository tickets;
    @Mock OperatorRepository operators;
    @Mock TicketCommentRepository comments;
    @Mock TicketHistoryRepository history;
    TicketService service;

    @BeforeEach void setUp() { service = new TicketService(tickets, operators, comments, history, new TicketMapper()); }

    @Test void missingTicketReturnsNotFound() {
        when(tickets.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(99)).isInstanceOf(ApiException.class).hasMessage("Ticket not found");
    }
    @Test void workCannotStartWithoutAnAssigneeAndNoHistoryIsWritten() {
        var ticket = new Ticket("Delivery delay", "Depot awaiting confirmation", Priority.HIGH);
        when(tickets.findById(1L)).thenReturn(Optional.of(ticket));
        assertThatThrownBy(() -> service.transition(1, new TicketDtos.Transition(TicketStatus.IN_PROGRESS)))
                .isInstanceOf(ApiException.class).hasMessageContaining("Assign an operator");
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.NEW);
        verifyNoInteractions(history);
    }
    @Test void newTicketCannotBeClosedDirectly() {
        var ticket = new Ticket("Delivery delay", "Depot awaiting confirmation", Priority.HIGH);
        when(tickets.findById(1L)).thenReturn(Optional.of(ticket));
        assertThatThrownBy(() -> service.transition(1, new TicketDtos.Transition(TicketStatus.CLOSED)))
                .isInstanceOf(ApiException.class).hasMessageContaining("is not allowed");
        verifyNoInteractions(history);
    }
    @Test void closedTicketCannotBeEdited() {
        var ticket = new Ticket("Delivery delay", "Depot awaiting confirmation", Priority.HIGH);
        ticket.transitionTo(TicketStatus.CLOSED);
        when(tickets.findById(1L)).thenReturn(Optional.of(ticket));
        assertThatThrownBy(() -> service.update(1, new TicketDtos.UpdateTicket("Updated title", "Updated detail", Priority.LOW)))
                .isInstanceOf(ApiException.class).hasMessage("Closed tickets are read-only");
        assertThat(ticket.getTitle()).isEqualTo("Delivery delay");
        verifyNoInteractions(history);
    }
}
