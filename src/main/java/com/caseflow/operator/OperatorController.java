package com.caseflow.operator;

import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operators")
public class OperatorController {
    private final OperatorRepository operators;
    public OperatorController(OperatorRepository operators) { this.operators = operators; }

    @GetMapping
    public List<OperatorView> list() {
        return operators.findAllByOrderByDisplayNameAsc().stream()
                .map(o -> new OperatorView(o.getId(), o.getDisplayName(), o.isActive())).toList();
    }
    public record OperatorView(Long id, String displayName, boolean active) {}
}
