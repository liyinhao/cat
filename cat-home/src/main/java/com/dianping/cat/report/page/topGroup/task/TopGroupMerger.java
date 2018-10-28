/**
 * 
 */
package com.dianping.cat.report.page.topGroup.task;

import com.dianping.cat.consumer.topGroup.TopGroupReportCountFilter;
import com.dianping.cat.consumer.topGroup.TopGroupReportMerger;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;
import com.dianping.cat.consumer.topGroup.model.entity.Group;
import com.dianping.cat.report.task.TaskHelper;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class TopGroupMerger {

	private TopGroupReport merge(String reportDomain, List<TopGroupReport> reports, double duration) {
		TopGroupReportMerger merger = new HistoryTopGroupReportMerger(new TopGroupReport(reportDomain)).setDuration(duration);

		for (TopGroupReport report : reports) {
			report.accept(merger);
		}
		return merger.getTopGroupReport();
	}

	public TopGroupReport mergeForDaily(String reportDomain, List<TopGroupReport> reports, Set<String> domains, double duration) {
		TopGroupReport eventReport = merge(reportDomain, reports, duration);
		HistoryTopGroupReportMerger merger = new HistoryTopGroupReportMerger(new TopGroupReport(reportDomain));
		TopGroupReport eventReport2 = merge(reportDomain, reports, duration);
		Group allMachines = merger.mergesForAllMachine(eventReport2);

		eventReport.addGroup(allMachines);
		eventReport.getBus().add("All");
		eventReport.getDomainNames().addAll(domains);

		Date date = eventReport.getStartTime();
		eventReport.setStartTime(TaskHelper.todayZero(date));
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);
		eventReport.setEndTime(end);

		new TopGroupReportCountFilter().visitTopGroupReport(eventReport);
		return eventReport;
	}

}
