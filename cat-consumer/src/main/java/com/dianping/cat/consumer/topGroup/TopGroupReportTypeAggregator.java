package com.dianping.cat.consumer.topGroup;

import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupName;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupType;
import com.dianping.cat.consumer.topGroup.model.entity.Group;
import com.dianping.cat.consumer.topGroup.model.transform.BaseVisitor;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;

public class TopGroupReportTypeAggregator extends BaseVisitor {

	private TopGroupReport m_report;

	public String m_currentDomain;

	private String m_currentType;

	private AllReportConfigManager m_configManager;

	public TopGroupReportTypeAggregator(TopGroupReport report, AllReportConfigManager configManager) {
		m_report = report;
		m_configManager = configManager;
	}

	private void mergeName(TopGroupName old, TopGroupName other) {
		long totalCountSum = old.getTotalCount() + other.getTotalCount();

		old.setTotalCount(totalCountSum);
		old.setFailCount(old.getFailCount() + other.getFailCount());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
		}
		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}
		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	private void mergeType(TopGroupType old, TopGroupType other) {
		long totalCountSum = old.getTotalCount() + other.getTotalCount();

		old.setTotalCount(totalCountSum);
		old.setFailCount(old.getFailCount() + other.getFailCount());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
		}
		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}
		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	private boolean validateName(String type, String name) {
		return m_configManager.validate(TopGroupAnalyzer.ID, type, name);
	}

	private boolean validateType(String type) {
		return m_configManager.validate(TopGroupAnalyzer.ID, type);
	}

	@Override
	public void visitName(TopGroupName name) {
		if (validateName(m_currentType, name.getId())) {
			Group group = m_report.findOrCreateGroup(m_currentDomain);
			TopGroupType curentType = group.findOrCreateType(m_currentType);
			TopGroupName currentName = curentType.findOrCreateName(name.getId());

			mergeName(currentName, name);
		}
	}

	@Override
	public void visitTopGroupReport(TopGroupReport eventReport) {
		m_currentDomain = eventReport.getDomain();
		m_report.setStartTime(eventReport.getStartTime());
		m_report.setEndTime(eventReport.getEndTime());
		super.visitTopGroupReport(eventReport);
	}

	@Override
	public void visitType(TopGroupType type) {
		String typeName = type.getId();

		if (validateType(typeName)) {
			Group group = m_report.findOrCreateGroup(m_currentDomain);
			TopGroupType result = group.findOrCreateType(typeName);

			m_currentType = typeName;
			mergeType(result, type);

			super.visitType(type);
		}
	}
}