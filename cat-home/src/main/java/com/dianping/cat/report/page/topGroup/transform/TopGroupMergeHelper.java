package com.dianping.cat.report.page.topGroup.transform;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;


public class TopGroupMergeHelper {

	public TopGroupReport mergeAllIps(TopGroupReport report, String ipAddress) {
		if (Constants.ALL.equalsIgnoreCase(ipAddress)) {
			AllMachineMerger all = new AllMachineMerger();

			all.visitTopGroupReport(report);
			report = all.getReport();
		}
		return report;
	}

	private TopGroupReport mergeAllNames(TopGroupReport report, String allName) {
		if (Constants.ALL.equalsIgnoreCase(allName)) {
			AllNameMerger all = new AllNameMerger();

			all.visitTopGroupReport(report);
			report = all.getReport();
		}
		return report;
	}

	public TopGroupReport mergeAllNames(TopGroupReport report, String ipAddress, String allName) {
		TopGroupReport temp = mergeAllIps(report, ipAddress);

		return mergeAllNames(temp, allName);
	}

}
