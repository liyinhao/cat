package com.dianping.cat.consumer.topGroup;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;
import com.dianping.cat.consumer.topGroup.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.topGroup.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.topGroup.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;
import org.unidal.lookup.annotation.Inject;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class TopGroupDelegate implements ReportDelegate<TopGroupReport> {

	@Inject
	private TaskManager m_taskManager;

	@Inject
	private ServerFilterConfigManager m_configManager;

	@Inject
	private AllReportConfigManager m_allManager;

	private TopGroupTpsStatisticsComputer m_computer = new TopGroupTpsStatisticsComputer();

	@Override
	public void afterLoad(Map<String, TopGroupReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, TopGroupReport> reports) {
		for (TopGroupReport report : reports.values()) {
			Set<String> domainNames = report.getDomainNames();

			domainNames.clear();
			domainNames.addAll(reports.keySet());
		}
		if (reports.size() > 0) {
			TopGroupReport all = createAggregatedReport(reports);

			reports.put(all.getDomain(), all);
		}
	}

	@Override
	public byte[] buildBinary(TopGroupReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public String buildXml(TopGroupReport report) {
		report.accept(m_computer);

		new TopGroupReportCountFilter().visitTopGroupReport(report);

		return report.toString();
	}

	public TopGroupReport createAggregatedReport(Map<String, TopGroupReport> reports) {
		if (reports.size() > 0) {
			TopGroupReport first = reports.values().iterator().next();
			TopGroupReport all = makeReport(Constants.ALL, first.getStartTime().getTime(), Constants.HOUR);
			TopGroupReportTypeAggregator visitor = new TopGroupReportTypeAggregator(all, m_allManager);

			try {
				for (TopGroupReport report : reports.values()) {
					String domain = report.getDomain();

					if (!domain.equals(Constants.ALL)) {
						all.getBus().add(domain);
						all.getDomainNames().add(domain);

						visitor.visitTopGroupReport(report);
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
			return all;
		} else {
			return new TopGroupReport(Constants.ALL);
		}
	}

	@Override
	public boolean createHourlyTask(TopGroupReport report) {
		String domain = report.getDomain();

		if (domain.equals(Constants.ALL)) {
			return m_taskManager.createTask(report.getStartTime(), domain, TopGroupAnalyzer.ID,
			      TaskProlicy.ALL_EXCLUED_HOURLY);
		} else if (m_configManager.validateDomain(domain)) {
			return m_taskManager.createTask(report.getStartTime(), report.getDomain(), TopGroupAnalyzer.ID, TaskProlicy.ALL);
		} else {
			return true;
		}
	}

	@Override
	public String getDomain(TopGroupReport report) {
		return report.getDomain();
	}

	@Override
	public TopGroupReport makeReport(String domain, long startTime, long duration) {
		TopGroupReport report = new TopGroupReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public TopGroupReport mergeReport(TopGroupReport old, TopGroupReport other) {
		TopGroupReportMerger merger = new TopGroupReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public TopGroupReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public TopGroupReport parseXml(String xml) throws Exception {
		TopGroupReport report = DefaultSaxParser.parse(xml);

		return report;
	}
}
