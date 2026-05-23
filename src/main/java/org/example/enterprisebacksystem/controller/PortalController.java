package org.example.enterprisebacksystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.enterprisebacksystem.common.api.ApiResponse;
import org.example.enterprisebacksystem.domain.Department;
import org.example.enterprisebacksystem.domain.Employee;
import org.example.enterprisebacksystem.domain.Expense;
import org.example.enterprisebacksystem.domain.Notice;
import org.example.enterprisebacksystem.domain.OaProcess;
import org.example.enterprisebacksystem.dto.portal.ContactRuleResp;
import org.example.enterprisebacksystem.dto.portal.FinanceFlowResp;
import org.example.enterprisebacksystem.dto.portal.FinanceItemResp;
import org.example.enterprisebacksystem.dto.portal.MetricResp;
import org.example.enterprisebacksystem.dto.portal.NoticeResp;
import org.example.enterprisebacksystem.dto.portal.OrgGroupResp;
import org.example.enterprisebacksystem.dto.portal.OrgPersonResp;
import org.example.enterprisebacksystem.dto.portal.PortalOverviewResp;
import org.example.enterprisebacksystem.dto.portal.RailLinkResp;
import org.example.enterprisebacksystem.dto.portal.WorkAppResp;
import org.example.enterprisebacksystem.mapper.AuditLogMapper;
import org.example.enterprisebacksystem.service.DepartmentService;
import org.example.enterprisebacksystem.service.EmployeeService;
import org.example.enterprisebacksystem.service.ExpenseService;
import org.example.enterprisebacksystem.service.NoticeService;
import org.example.enterprisebacksystem.service.OaProcessService;
import org.example.enterprisebacksystem.service.PermissionService;
import org.example.enterprisebacksystem.service.RoleService;
import org.example.enterprisebacksystem.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalController {

    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final AuditLogMapper auditLogMapper;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final NoticeService noticeService;
    private final OaProcessService oaProcessService;
    private final ExpenseService expenseService;

    @GetMapping("/overview")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PortalOverviewResp> overview() {
        PortalOverviewResp resp = new PortalOverviewResp();
        resp.setMetrics(metrics());
        resp.setNotices(notices());
        resp.setContactRules(contactRules());
        resp.setOrgGroups(orgGroups());
        resp.setWorkApps(workApps());
        resp.setFinanceItems(financeItems());
        resp.setFinanceFlows(financeFlows());
        resp.setRightLinks(rightLinks());
        return ApiResponse.ok(resp);
    }

    private List<MetricResp> metrics() {
        long userCount = userService.count();
        long roleCount = roleService.count();
        long permissionCount = permissionService.count();
        Long auditCount = auditLogMapper.selectCount(null);
        long onlineCount = employeeService.count(new LambdaQueryWrapper<Employee>().eq(Employee::getOnlineStatus, 1));

        return List.of(
                new MetricResp("在线员工", String.valueOf(onlineCount)),
                new MetricResp("系统用户", String.valueOf(userCount)),
                new MetricResp("角色数量", String.valueOf(roleCount)),
                new MetricResp("权限点", String.valueOf(permissionCount)),
                new MetricResp("审计记录", String.valueOf(auditCount == null ? 0 : auditCount))
        );
    }

    private List<NoticeResp> notices() {
        List<Notice> records = noticeService.page(
                new Page<>(1, 5),
                new LambdaQueryWrapper<Notice>()
                        .eq(Notice::getStatus, 1)
                        .orderByDesc(Notice::getPublishTime)
                        .orderByDesc(Notice::getCreateTime)
        ).getRecords();

        if (!records.isEmpty()) {
            return records.stream()
                    .map(notice -> new NoticeResp(notice.getTitle(), notice.getContent(), notice.getLevel()))
                    .collect(Collectors.toList());
        }

        return List.of(
                new NoticeResp("权限变更审批", "部门负责人可以为基层员工申请临时访问权限。", "安全"),
                new NoticeResp("费用报销提醒", "本周五前提交差旅票据，逾期进入下月周期。", "费用"),
                new NoticeResp("OA 流程同步", "入职登记、离职登记、调岗流程已纳入工作台。", "OA")
        );
    }

    private List<ContactRuleResp> contactRules() {
        return List.of(
                new ContactRuleResp("基层员工", "可联系部门负责人", "管理层灰锁"),
                new ContactRuleResp("部门负责人", "可联系管理层并管理下属权限", "绿锁"),
                new ContactRuleResp("管理层", "全局审批与授权", "无锁")
        );
    }

    private List<OrgGroupResp> orgGroups() {
        List<Department> departments = departmentService.list(
                new LambdaQueryWrapper<Department>()
                        .eq(Department::getStatus, 1)
                        .orderByAsc(Department::getParentId)
                        .orderByAsc(Department::getId)
        );
        List<Employee> employees = employeeService.list(
                new LambdaQueryWrapper<Employee>()
                        .eq(Employee::getStatus, 1)
                        .orderByAsc(Employee::getContactLevel)
                        .orderByAsc(Employee::getId)
        );

        if (!departments.isEmpty()) {
            Map<Long, List<Employee>> employeesByDepartment = employees.stream()
                    .collect(Collectors.groupingBy(Employee::getDepartmentId));

            return departments.stream()
                    .map(department -> new OrgGroupResp(
                            department.getName(),
                            employeesByDepartment.getOrDefault(department.getId(), List.of()).stream()
                                    .map(employee -> new OrgPersonResp(
                                            employee.getName(),
                                            employee.getPosition(),
                                            contactText(employee.getContactLevel()),
                                            employee.getContactLevel() != null && employee.getContactLevel() >= 3
                                    ))
                                    .collect(Collectors.toList())
                    ))
                    .collect(Collectors.toList());
        }

        return List.of(
                new OrgGroupResp("管理层", List.of(
                        new OrgPersonResp("老板", "决策审批", "仅管理层可联系", true),
                        new OrgPersonResp("带班经理", "排班协调", "部门负责人可联系", true),
                        new OrgPersonResp("部门经理", "跨部门审批", "部门负责人可联系", true)
                )),
                new OrgGroupResp("部门负责人", List.of(
                        new OrgPersonResp("人力负责人", "入职、离职、升迁", "员工可联系", false),
                        new OrgPersonResp("会计负责人", "报销、薪水发放", "员工可联系", false),
                        new OrgPersonResp("工作部门负责人", "项目调度", "员工可联系", false)
                )),
                new OrgGroupResp("基层员工", List.of(
                        new OrgPersonResp("业务员工", "项目执行", "联系部门负责人", false),
                        new OrgPersonResp("实习员工", "辅助执行", "联系直属负责人", false)
                ))
        );
    }

    private List<WorkAppResp> workApps() {
        List<OaProcess> records = oaProcessService.page(
                new Page<>(1, 8),
                new LambdaQueryWrapper<OaProcess>()
                        .eq(OaProcess::getStatus, 1)
                        .orderByDesc(OaProcess::getPendingCount)
                        .orderByDesc(OaProcess::getCreateTime)
        ).getRecords();

        if (!records.isEmpty()) {
            return records.stream()
                    .map(process -> new WorkAppResp(
                            process.getTitle(),
                            process.getStatusText(),
                            process.getCategory(),
                            process.getTone()
                    ))
                    .collect(Collectors.toList());
        }

        return List.of(
                new WorkAppResp("项目审批", "8 个待处理", "工作部门负责人", "blue"),
                new WorkAppResp("人员出差登记", "2 个进行中", "人力负责人", "green"),
                new WorkAppResp("新入职登记", "3 个草稿", "人力负责人", "amber"),
                new WorkAppResp("部门人事调动", "1 个待确认", "部门经理", "red"),
                new WorkAppResp("离职登记", "0 个逾期", "人力负责人", "green"),
                new WorkAppResp("升迁登记", "4 个待评审", "管理层", "blue")
        );
    }

    private List<FinanceItemResp> financeItems() {
        BigDecimal travel = sumExpense("差旅报销");
        BigDecimal purchase = sumExpense("办公采购");
        long paidCount = expenseService.count(new LambdaQueryWrapper<Expense>().eq(Expense::getStatus, "PAID"));

        if (travel.compareTo(BigDecimal.ZERO) > 0 || purchase.compareTo(BigDecimal.ZERO) > 0 || paidCount > 0) {
            return List.of(
                    new FinanceItemResp("差旅报销", "¥ " + travel, "已入账"),
                    new FinanceItemResp("办公采购", "¥ " + purchase, "已入账"),
                    new FinanceItemResp("薪水发放", paidCount + " 条已支付", "归档")
            );
        }

        return List.of(
                new FinanceItemResp("差旅报销", "¥ 12,680", "+8.4%"),
                new FinanceItemResp("办公采购", "¥ 4,320", "-2.1%"),
                new FinanceItemResp("薪水发放", "本月已归档", "100%")
        );
    }

    private List<FinanceFlowResp> financeFlows() {
        return List.of(
                new FinanceFlowResp("差旅费报销", "员工提交票据，部门负责人初审，会计负责人复核。", "3 步"),
                new FinanceFlowResp("薪水发放", "人力确认考勤，会计生成发放批次，管理层最终审批。", "4 步")
        );
    }

    private List<RailLinkResp> rightLinks() {
        return List.of(
                new RailLinkResp("公司组织架构介绍", "查看部门关系、负责人和联系权限"),
                new RailLinkResp("消息中心", "审批提醒、系统通知、待办状态"),
                new RailLinkResp("公司OA系统", "出差、报销、入离职、调岗流程入口")
        );
    }

    private BigDecimal sumExpense(String category) {
        return expenseService.list(new LambdaQueryWrapper<Expense>().eq(Expense::getCategory, category))
                .stream()
                .map(Expense::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String contactText(Integer contactLevel) {
        if (contactLevel == null || contactLevel <= 1) {
            return "员工可联系";
        }
        if (contactLevel == 2) {
            return "部门负责人可联系";
        }
        return "管理层权限受限";
    }
}
