package com.lingshu.controller.admin;

import com.lingshu.dto.response.ApiResponse;
import com.lingshu.entity.InterviewFollowUpRule;
import com.lingshu.service.admin.AdminEvaluationRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/evaluation-rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminEvaluationRuleController {

    private final AdminEvaluationRuleService adminEvaluationRuleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InterviewFollowUpRule>>> listRules(
            @RequestParam(required = false) Long questionId,
            @RequestParam(required = false) String triggerType) {
        return ResponseEntity.ok(ApiResponse.success(
                adminEvaluationRuleService.listRules(questionId, triggerType)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InterviewFollowUpRule>> createRule(@RequestBody InterviewFollowUpRule request) {
        return ResponseEntity.ok(ApiResponse.success("评估规则创建成功", adminEvaluationRuleService.createRule(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InterviewFollowUpRule>> updateRule(@PathVariable Long id,
                                                                         @RequestBody InterviewFollowUpRule request) {
        return ResponseEntity.ok(ApiResponse.success("评估规则更新成功", adminEvaluationRuleService.updateRule(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long id) {
        adminEvaluationRuleService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success("评估规则删除成功", null));
    }
}
