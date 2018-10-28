package com.dianping.cat.report.page.topGroup.transform;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.topGroup.TopGroupReportMerger;
import com.dianping.cat.consumer.topGroup.model.entity.*;
import com.dianping.cat.consumer.topGroup.model.transform.BaseVisitor;

public class AllNameMerger extends BaseVisitor {

	public TopGroupReport m_report;

	public String m_currentIp;

	public String m_currentType;

	public String m_currentName;

	public Integer m_currentRange;

	public TopGroupReportMerger m_merger = new TopGroupReportMerger(new TopGroupReport());

	public TopGroupReport getReport() {
		return m_report;
	}

	@Override
	public void visitTopGroupReport(TopGroupReport eventReport) {
		m_report = new TopGroupReport(eventReport.getDomain());
		m_report.setStartTime(eventReport.getStartTime());
		m_report.setEndTime(eventReport.getEndTime());
		m_report.getDomainNames().addAll(eventReport.getDomainNames());
		m_report.getBus().addAll(eventReport.getBus());

		super.visitTopGroupReport(eventReport);
	}

	@Override
	public void visitGroup(Group machine) {
		m_currentIp = machine.getBu();
		m_report.findOrCreateGroup(m_currentIp);
		super.visitGroup(machine);
	}

	@Override
	public void visitName(TopGroupName name) {
		m_currentName = name.getId();
		TopGroupName temp = m_report.findOrCreateGroup(m_currentIp).findOrCreateType(m_currentType)
		      .findOrCreateName(m_currentName);

		m_merger.mergeName(temp, name);

		TopGroupName all = m_report.findOrCreateGroup(m_currentIp).findOrCreateType(m_currentType)
		      .findOrCreateName(Constants.ALL);
		m_merger.mergeName(all, name);

		m_merger.mergeName(temp, name);
		super.visitName(name);
	}

	@Override
	public void visitRange(Range range) {
		m_currentRange = range.getValue();
		Range temp = m_report.findOrCreateGroup(m_currentIp).findOrCreateType(m_currentType)
		      .findOrCreateName(m_currentName).findOrCreateRange(m_currentRange);

		m_merger.mergeRange(temp, range);

		Range all = m_report.findOrCreateGroup(m_currentIp).findOrCreateType(m_currentType)
		      .findOrCreateName(Constants.ALL).findOrCreateRange(m_currentRange);

		m_merger.mergeRange(all, range);
		super.visitRange(range);
	}

	@Override
	public void visitType(TopGroupType type) {
		m_currentType = type.getId();
		TopGroupType temp = m_report.findOrCreateGroup(m_currentIp).findOrCreateType(m_currentType);

		m_merger.mergeType(temp, type);
		super.visitType(type);
	}

}
