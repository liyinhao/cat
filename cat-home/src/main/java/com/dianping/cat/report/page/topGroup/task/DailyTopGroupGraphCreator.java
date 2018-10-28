package com.dianping.cat.report.page.topGroup.task;

import com.dianping.cat.consumer.topGroup.model.entity.TopGroupName;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupType;
import com.dianping.cat.consumer.topGroup.model.entity.Group;
import com.dianping.cat.consumer.topGroup.model.transform.BaseVisitor;
import com.dianping.cat.consumer.topGroup.TopGroupAnalyzer;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;
import com.dianping.cat.core.dal.DailyGraph;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DailyTopGroupGraphCreator {

	private List<DailyGraph> m_graphs = new ArrayList<DailyGraph>();

	private TopGroupReport m_report;

	public List<DailyGraph> buildDailygraph(TopGroupReport report) {
		m_report = report;
		new EventReportVisitor().visitTopGroupReport(report);

		return m_graphs;
	}

	private DailyGraph buildDailyGraph(String ip) {
		DailyGraph graph = new DailyGraph();

		graph.setDomain(m_report.getDomain());
		graph.setPeriod(m_report.getStartTime());
		graph.setName(TopGroupAnalyzer.ID);
		graph.setIp(ip);
		graph.setType(3);
		graph.setCreationDate(new Date());
		return graph;
	}

	public class EventReportVisitor extends BaseVisitor {

		private String m_currentIp;

		private String m_currentType;

		private DailyGraph m_currentDailygraph;

		private StringBuilder m_summaryContent;

		private StringBuilder m_detailContent;

		@Override
		public void visitGroup(Group machine) {
			m_currentIp = machine.getBu();
			m_currentDailygraph = buildDailyGraph(m_currentIp);
			m_graphs.add(m_currentDailygraph);
			m_summaryContent = new StringBuilder();
			m_detailContent = new StringBuilder();

			super.visitGroup(machine);
			m_currentDailygraph.setDetailContent(m_detailContent.toString());
			m_currentDailygraph.setSummaryContent(m_summaryContent.toString());
		}

		@Override
		public void visitName(TopGroupName name) {
			// TYPE, NAME, TOTAL_COUNT, FAILURE_COUNT
			m_detailContent.append(m_currentType).append('\t');
			m_detailContent.append(name.getId()).append('\t');
			m_detailContent.append(name.getTotalCount()).append('\t');
			m_detailContent.append(name.getFailCount()).append('\t').append('\n');
			super.visitName(name);
		}

		@Override
		public void visitType(TopGroupType type) {
			// TYPE, TOTAL_COUNT, FAILURE_COUNT
			m_currentType = type.getId();
			m_summaryContent.append(type.getId()).append('\t');
			m_summaryContent.append(type.getTotalCount()).append('\t');
			m_summaryContent.append(type.getFailCount()).append('\t').append('\n');
			super.visitType(type);
		}
	}
}
