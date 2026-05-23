package org.example.enterprisebacksystem.dto.portal;

import lombok.Data;

import java.util.List;

@Data
public class PortalOverviewResp {
    private List<MetricResp> metrics;
    private List<NoticeResp> notices;
    private List<ContactRuleResp> contactRules;
    private List<OrgGroupResp> orgGroups;
    private List<WorkAppResp> workApps;
    private List<FinanceItemResp> financeItems;
    private List<FinanceFlowResp> financeFlows;
    private List<RailLinkResp> rightLinks;
}
