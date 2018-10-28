package com.dianping.cat.consumer.topGroup;

import com.dianping.cat.consumer.topGroup.model.entity.TopGroupName;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupType;
import com.dianping.cat.consumer.topGroup.model.transform.BaseVisitor;

public class TopGroupTpsStatisticsComputer extends BaseVisitor {

	public double m_duration = 3600;

	public TopGroupTpsStatisticsComputer setDuration(double duration) {
		m_duration = duration;
		return this;
	}

	@Override
	public void visitName(TopGroupName name) {
		if (m_duration > 0) {
			name.setTps(name.getTotalCount() * 1.0 / m_duration);
		}
	}

	@Override
	public void visitType(TopGroupType type) {
		if (m_duration > 0) {
			type.setTps(type.getTotalCount() * 1.0 / m_duration);
			super.visitType(type);
		}
	}
}
