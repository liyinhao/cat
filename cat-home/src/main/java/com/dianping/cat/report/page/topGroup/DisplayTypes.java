package com.dianping.cat.report.page.topGroup;

import com.dianping.cat.consumer.topGroup.model.entity.Group;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupType;
import com.dianping.cat.consumer.topGroup.model.entity.Group;
import org.unidal.lookup.util.StringUtils;

import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;

public class DisplayTypes {

	private List<EventTypeModel> m_results = new ArrayList<EventTypeModel>();

	public DisplayTypes display(String sorted, String ip, TopGroupReport report) {
		if (report == null) {
			return this;
		}
		Group machine = report.getGroups().get(ip);
		if (machine == null) {
			return this;
		}
		Map<String, TopGroupType> types = machine.getTypes();
		if (types != null) {
			for (Entry<String, TopGroupType> entry : types.entrySet()) {
				m_results.add(new EventTypeModel(entry.getKey(), entry.getValue()));
			}
		}
		if (!StringUtils.isEmpty(sorted)) {
			Collections.sort(m_results, new EventComparator(sorted));
		}
		return this;
	}

	public List<EventTypeModel> getResults() {
		return m_results;
	}

	public static class EventComparator implements Comparator<EventTypeModel> {

		private String m_sorted;

		public EventComparator(String type) {
			m_sorted = type;
		}

		@Override
		public int compare(EventTypeModel m1, EventTypeModel m2) {
			if (m_sorted.equals("name") || m_sorted.equals("type")) {
				return m1.getType().compareTo(m2.getType());
			}
			if (m_sorted.equals("total")) {
				return (int) (m2.getDetail().getTotalCount() - m1.getDetail().getTotalCount());
			}
			if (m_sorted.equals("failure")) {
				return (int) (m2.getDetail().getFailCount() - m1.getDetail().getFailCount());
			}
			if (m_sorted.equals("failurePercent")) {
				return (int) (m2.getDetail().getFailPercent() * 100 - m1.getDetail().getFailPercent() * 100);
			}
			return 0;
		}
	}

	public static class EventTypeModel {
		private TopGroupType m_detail;

		private String m_type;

		public EventTypeModel(String str, TopGroupType detail) {
			m_type = str;
			m_detail = detail;
		}

		public TopGroupType getDetail() {
			return m_detail;
		}

		public String getType() {
			try {
				return URLEncoder.encode(m_type, "utf-8");
			} catch (Exception e) {
				return m_type;
			}
		}
	}
}
