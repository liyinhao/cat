package com.dianping.cat.consumer.topGroup;



import com.dianping.cat.consumer.topGroup.model.entity.TopGroupName;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupType;
import com.dianping.cat.consumer.topGroup.model.transform.BaseVisitor;

import java.util.*;

public class TopGroupReportCountFilter extends BaseVisitor {

	private int m_maxItems = 400;

	private void mergeName(TopGroupName old, TopGroupName other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
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

	@Override
	public void visitType(TopGroupType type) {
		Map<String, TopGroupName> eventNames = type.getNames();

		int size = eventNames.size();

		if (size > m_maxItems) {
			List<TopGroupName> all = new ArrayList<TopGroupName>(eventNames.values());

			Collections.sort(all, new EventNameCompator());
			type.getNames().clear();

			for (int i = 0; i < m_maxItems; i++) {
				type.addName(all.get(i));
			}

			TopGroupName other = type.findOrCreateName("OTHERS");

			for (int i = m_maxItems; i < size; i++) {
				mergeName(other, all.get(i));
			}
		}
		super.visitType(type);
	}

	private static class EventNameCompator implements Comparator<TopGroupName> {
		@Override
		public int compare(TopGroupName o1, TopGroupName o2) {
			return (int) (o2.getTotalCount() - o1.getTotalCount());
		}
	}
}