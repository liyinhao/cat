package com.dianping.cat.report.page.topGroup;

public enum JspFile {
	GRAPHS("/jsp/report/topGroup/event_graphs.jsp"),

	HISTORY_GRAPH("/jsp/report/topGroup/eventHistoryGraphs.jsp"),

	HISTORY_REPORT("/jsp/report/topGroup/eventHistoryReport.jsp"),

	HOURLY_REPORT("/jsp/report/topGroup/event.jsp"),

	GROUP_GRAPHS("/jsp/report/topGroup/event_graphs.jsp"),

	HISTORY_GROUP_GRAPH("/jsp/report/topGroup/eventHistoryGraphs.jsp"),

	HISTORY_GROUP_REPORT("/jsp/report/topGroup/eventHistoryGroupReport.jsp"),

	HOURLY_GROUP_REPORT("/jsp/report/topGroup/eventGroup.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
