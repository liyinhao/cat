package com.dianping.cat.report.page.topGroup.task;

import com.dianping.cat.consumer.topGroup.TopGroupReportMerger;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupName;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupType;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;

public class HistoryTopGroupReportMerger extends TopGroupReportMerger {

	double m_duration = 1;

	public HistoryTopGroupReportMerger(TopGroupReport eventReport) {
		super(eventReport);
	}

	@Override
	public void mergeName(TopGroupName old, TopGroupName other) {
		old.getRanges().clear();
		other.getRanges().clear();
		super.mergeName(old, other);
		old.setTps(old.getTotalCount() / (m_duration * 24 * 3600));
	}

	@Override
	public void visitName(TopGroupName name) {
		name.getRanges().clear();
		super.visitName(name);
	}

	@Override
	public void mergeType(TopGroupType old, TopGroupType other) {
		super.mergeType(old, other);
		old.setTps(old.getTotalCount() / (m_duration * 24 * 3600));
	}

	public HistoryTopGroupReportMerger setDuration(double duration) {
		m_duration = duration;
		return this;
	}
}
