package com.dxsoft.rsgzgl.workflow;

import com.dxsoft.rsgzgl.common.PageRequest;
import com.dxsoft.rsgzgl.common.PageResponse;
import com.dxsoft.rsgzgl.exchange.notification.ExchangeDeploymentProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payroll-workflows")
class PayrollWorkflowController {

    private final PayrollWorkflowService payrollWorkflowService;

    PayrollWorkflowController(PayrollWorkflowService payrollWorkflowService) {
        this.payrollWorkflowService = payrollWorkflowService;
    }

    @GetMapping
    PageResponse<PayrollWorkflowRecord> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String payrollModule,
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return payrollWorkflowService.list(status, payrollModule, organizationCode, keyword, PageRequest.of(page, size));
    }

    @GetMapping("/pending-count")
    PendingCountResponse pendingCount() {
        return new PendingCountResponse(payrollWorkflowService.countPending());
    }

    @GetMapping("/{id}")
    PayrollWorkflowRecord get(@PathVariable long id) {
        return payrollWorkflowService.findById(id)
                .orElseThrow(() -> new com.dxsoft.rsgzgl.common.NotFoundException("Payroll workflow not found: " + id));
    }

    record PendingCountResponse(long count) {
    }
}

@RestController
@RequestMapping("/api/config")
class WorkflowConfigController {

    private final PayrollWorkflowProperties workflowProperties;
    private final ExchangeDeploymentProperties deploymentProperties;

    WorkflowConfigController(
            PayrollWorkflowProperties workflowProperties,
            ExchangeDeploymentProperties deploymentProperties) {
        this.workflowProperties = workflowProperties;
        this.deploymentProperties = deploymentProperties;
    }

    @GetMapping("/workflow")
    WorkflowConfigResponse workflow() {
        return new WorkflowConfigResponse(
                workflowProperties.isEnabled(),
                deploymentProperties.sharedDatabase(),
                workflowProperties.isHideDataExchange(),
                workflowProperties.isAutoQueueOnApprove());
    }

    record WorkflowConfigResponse(
            boolean enabled,
            boolean sharedDatabase,
            boolean hideDataExchange,
            boolean autoQueueOnApprove) {
    }
}
