package com.dianping.cat.report.page.topGroup.task;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;

import com.dianping.cat.consumer.topGroup.TopGroupReportCountFilter;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;
import com.dianping.cat.consumer.topGroup.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.topGroup.TopGroupAnalyzer;
import com.dianping.cat.core.dal.*;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.topGroup.service.TopGroupReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TopGroupReportBuilder implements TaskBuilder {

	public static final String ID = TopGroupAnalyzer.ID;

	@Inject
	protected GraphDao m_graphDao;

	@Inject
	protected DailyGraphDao m_dailyGraphDao;

	@Inject
	protected TopGroupReportService m_reportService;

	@Inject
	private TopGroupGraphCreator m_eventGraphCreator;

	@Inject
	private TopGroupMerger m_eventMerger;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			TopGroupReport eventReport = queryHourlyReportsByDuration(name, domain, period, TaskHelper.tomorrowZero(period));

			buildEventDailyGraph(eventReport);

			DailyReport report = new DailyReport();

			report.setCreationDate(new Date());
			report.setDomain(domain);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(name);
			report.setPeriod(period);
			report.setType(1);
			byte[] binaryContent = DefaultNativeBuilder.build(eventReport);
			return m_reportService.insertDailyReport(report, binaryContent);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	private void buildEventDailyGraph(TopGroupReport report) {
		DailyTopGroupGraphCreator creator = new DailyTopGroupGraphCreator();
		List<DailyGraph> graphs = creator.buildDailygraph(report);

		for (DailyGraph graph : graphs) {
			try {
				m_dailyGraphDao.insert(graph);
			} catch (DalException e) {
				Cat.logError(e);
			}
		}
	}

	private List<Graph> buildHourlyGraphs(String name, String domain, Date period) throws DalException {
		long startTime = period.getTime();
		TopGroupReport report = m_reportService.queryReport(domain, new Date(startTime), new Date(startTime
		      + TimeHelper.ONE_HOUR));

		return m_eventGraphCreator.splitReportToGraphs(period, domain, TopGroupAnalyzer.ID, report);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		try {
			List<Graph> graphs = buildHourlyGraphs(name, domain, period);
			if (graphs != null) {
				for (Graph graph : graphs) {
					this.m_graphDao.insert(graph);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		Date end = null;

		if (period.equals(TimeHelper.getCurrentMonth())) {
			end = TimeHelper.getCurrentDay();
		} else {
			end = TaskHelper.nextMonthStart(period);
		}

		TopGroupReport eventReport = queryDailyReportsByDuration(domain, period, end);
		MonthlyReport report = new MonthlyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(eventReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		Date end = null;

		if (period.equals(TimeHelper.getCurrentWeek())) {
			end = TimeHelper.getCurrentDay();
		} else {
			end = new Date(period.getTime() + TimeHelper.ONE_WEEK);
		}

		TopGroupReport eventReport = queryDailyReportsByDuration(domain, period, end);
		WeeklyReport report = new WeeklyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(eventReport);
		return m_reportService.insertWeeklyReport(report, binaryContent);
	}

	private TopGroupReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		double duration = (endTime - startTime) * 1.0 / TimeHelper.ONE_DAY;
		HistoryTopGroupReportMerger merger = new HistoryTopGroupReportMerger(new TopGroupReport(domain)).setDuration(duration);

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				TopGroupReport reportModel = m_reportService.queryReport(domain, new Date(startTime), new Date(startTime
				      + TimeHelper.ONE_DAY));
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		TopGroupReport eventReport = merger.getTopGroupReport();

		eventReport.setStartTime(start);
		eventReport.setEndTime(end);

		new TopGroupReportCountFilter().visitTopGroupReport(eventReport);
		return eventReport;
	}

	private TopGroupReport queryHourlyReportsByDuration(String name, String domain, Date start, Date end)
	      throws DalException {
		Set<String> domainSet = m_reportService.queryAllDomainNames(start, end, TopGroupAnalyzer.ID);
		List<TopGroupReport> reports = new ArrayList<TopGroupReport>();
		long startTime = start.getTime();
		long endTime = end.getTime();
		double duration = (endTime - startTime) * 1.0 / TimeHelper.ONE_DAY;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			TopGroupReport report = m_reportService.queryReport(domain, new Date(startTime), new Date(startTime
			      + TimeHelper.ONE_HOUR));

			reports.add(report);
		}
		return m_eventMerger.mergeForDaily(domain, reports, domainSet, duration);
	}

}
