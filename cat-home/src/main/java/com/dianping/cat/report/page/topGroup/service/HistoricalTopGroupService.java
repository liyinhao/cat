package com.dianping.cat.report.page.topGroup.service;

import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;
import com.dianping.cat.consumer.topGroup.TopGroupAnalyzer;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;
import org.unidal.lookup.annotation.Inject;

import java.util.Date;

public class HistoricalTopGroupService extends BaseHistoricalModelService<TopGroupReport> {

	@Inject
	private TopGroupReportService m_reportService;

	public HistoricalTopGroupService() {
		super(TopGroupAnalyzer.ID);
	}

	@Override
	protected TopGroupReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		TopGroupReport report = getReportFromDatabase(date, domain);

		return report;
	}

	private TopGroupReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportService.queryReport(domain, new Date(timestamp), new Date(timestamp + TimeHelper.ONE_HOUR));
	}

}
