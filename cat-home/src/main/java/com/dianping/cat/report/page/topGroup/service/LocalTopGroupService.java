package com.dianping.cat.report.page.topGroup.service;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.topGroup.TopGroupReportMerger;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupName;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupType;
import com.dianping.cat.consumer.topGroup.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.topGroup.TopGroupAnalyzer;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import org.unidal.lookup.annotation.Inject;

import java.util.Date;
import java.util.List;

public class LocalTopGroupService extends LocalModelService<TopGroupReport> {

	public static final String ID = TopGroupAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalTopGroupService() {
		super(TopGroupAnalyzer.ID);
	}

	private String filterReport(ApiPayload payload, TopGroupReport report) {
		String ipAddress = payload.getIpAddress();
		String type = payload.getType();
		String name = payload.getName();
		TopGroupReportFilter filter = new TopGroupReportFilter(type, name, ipAddress);

		return filter.buildXml(report);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
	      throws Exception {
		List<TopGroupReport> reports = super.getReport(period, domain);
		TopGroupReport report = null;

		if (reports != null) {
			report = new TopGroupReport(domain);
			TopGroupReportMerger merger = new TopGroupReportMerger(report);

			for (TopGroupReport tmp : reports) {
				tmp.accept(merger);
			}
		}

		if ((report == null || report.getBus().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
		}
		return filterReport(payload, report);
	}

	private TopGroupReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		TopGroupReport report = new TopGroupReport(domain);
		TopGroupReportMerger merger = new TopGroupReportMerger(report);

		report.setStartTime(new Date(timestamp));
		report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));

		for (int i = 0; i < ANALYZER_COUNT; i++) {
			ReportBucket bucket = null;
			try {
				bucket = m_bucketManager.getReportBucket(timestamp, TopGroupAnalyzer.ID, i);
				String xml = bucket.findById(domain);

				if (xml != null) {
					TopGroupReport tmp = DefaultSaxParser.parse(xml);

					tmp.accept(merger);
				} else {
					report.getDomainNames().addAll(bucket.getIds());
				}
			} finally {
				if (bucket != null) {
					m_bucketManager.closeBucket(bucket);
				}
			}
		}
		return report;
	}

	public static class TopGroupReportFilter extends com.dianping.cat.consumer.topGroup.model.transform.DefaultXmlBuilder {
		private String m_ipAddress;

		private String m_name;

		private String m_type;

		public TopGroupReportFilter(String type, String name, String ip) {
			super(true, new StringBuilder(DEFAULT_SIZE));
			m_type = type;
			m_name = name;
			m_ipAddress = ip;
		}

		@Override
		public void visitGroup(com.dianping.cat.consumer.topGroup.model.entity.Group machine) {
			if (m_ipAddress == null || m_ipAddress.equals(Constants.ALL)) {
				super.visitGroup(machine);
			} else if (machine.getBu().equals(m_ipAddress)) {
				super.visitGroup(machine);
			}
		}

		@Override
		public void visitName(TopGroupName name) {
			if (m_type != null) {
				super.visitName(name);
			}
		}

		@Override
		public void visitRange(com.dianping.cat.consumer.topGroup.model.entity.Range range) {
			if (m_type != null && m_name != null) {
				super.visitRange(range);
			}
		}

		@Override
		public void visitType(TopGroupType type) {
			if (m_type == null) {
				super.visitType(type);
			} else if (type.getId().equals(m_type)) {
				type.setSuccessMessageUrl(null);
				type.setFailMessageUrl(null);

				super.visitType(type);
			}
		}
	}

}
