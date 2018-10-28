package com.dianping.cat.report.page.topGroup;

import com.dianping.cat.consumer.topGroup.model.entity.TopGroupName;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupType;

import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;

public class DisplayNames {

	private List<EventNameModel> m_results = new ArrayList<EventNameModel>();

	public DisplayNames display(String sorted, String type, String ip, TopGroupReport report, String queryName) {
		Map<String, TopGroupType> types = report.findOrCreateGroup(ip).getTypes();
		TopGroupName all = new TopGroupName("TOTAL");
		all.setTotalPercent(1);
		if (types != null) {
			TopGroupType names = types.get(type);

			if (names != null) {
				for (Entry<String, TopGroupName> entry : names.getNames().entrySet()) {
					String eventTypeName = entry.getValue().getId();
					boolean isAdd = (queryName == null || queryName.length() == 0 || isFit(queryName, eventTypeName));
					if (isAdd){
						m_results.add(new EventNameModel(entry.getKey(), entry.getValue()));
						mergeName(all, entry.getValue());
					}
				}
			}
		}
		if (sorted == null) {
			sorted = "avg";
		}
		Collections.sort(m_results, new EventComparator(sorted));

		long total = all.getTotalCount();
		for (EventNameModel nameModel : m_results) {
			TopGroupName eventName = nameModel.getDetail();
			eventName.setTotalPercent(eventName.getTotalCount() / (double) total);
		}
		m_results.add(0, new EventNameModel("TOTAL", all));
		return this;
	}

	private boolean isFit(String queryName, String transactionName) {
		String[] args = queryName.split("\\|");

		if (args != null) {
			for (String str : args) {
				if (str.length() > 0 && transactionName.toLowerCase().contains(str.trim().toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	public List<EventNameModel> getResults() {
		return m_results;
	}

	public void mergeName(TopGroupName old, TopGroupName other) {
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

	public static class EventComparator implements Comparator<EventNameModel> {

		private String m_sorted;

		public EventComparator(String type) {
			m_sorted = type;
		}

		@Override
		public int compare(EventNameModel m1, EventNameModel m2) {
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

	public static class EventNameModel {
		private TopGroupName m_detail;

		private String m_type;

		public EventNameModel(String str, TopGroupName detail) {
			m_type = str;
			m_detail = detail;
		}

		public TopGroupName getDetail() {
			return m_detail;
		}

		public String getName() {
			String id = m_detail.getId();

			try {
				return URLEncoder.encode(id, "utf-8");
			} catch (Exception e) {
				return id;
			}
		}

		public String getType() {
			return m_type;
		}
	}

}
