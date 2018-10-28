package com.dianping.cat.report.page.topGroup.service;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.topGroup.TopGroupReportMerger;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupName;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupType;
import com.dianping.cat.consumer.topGroup.model.transform.BaseVisitor;
import com.dianping.cat.consumer.topGroup.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.topGroup.TopGroupAnalyzer;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;
import com.dianping.cat.core.dal.*;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.AbstractReportService;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class TopGroupReportService extends AbstractReportService<TopGroupReport> {

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd");

	private TopGroupReport convert(TopGroupReport report) {
		Date start = report.getStartTime();
		Date end = report.getEndTime();

		try {
			if (start != null && end != null && end.before(m_sdf.parse("2015-01-05"))) {
				TpsStatistics statistics = new TpsStatistics((end.getTime() - start.getTime()) / 1000.0);

				report.accept(statistics);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return report;
	}

	@Override
	public TopGroupReport makeReport(String domain, Date start, Date end) {
		TopGroupReport report = new TopGroupReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public TopGroupReport queryDailyReport(String domain, Date start, Date end) {
		TopGroupReportMerger merger = new TopGroupReportMerger(new TopGroupReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = TopGroupAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				TopGroupReport reportModel = queryFromDailyBinary(report.getId(), domain);

				reportModel.accept(merger);
			} catch (DalNotFoundException e) {
				// ignore
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		TopGroupReport eventReport = merger.getTopGroupReport();

		eventReport.setStartTime(start);
		eventReport.setEndTime(end);
		return convert(eventReport);
	}

	private TopGroupReport queryFromDailyBinary(int id, String domain) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new TopGroupReport(domain);
		}
	}

	private TopGroupReport queryFromHourlyBinary(int id, String domain) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao.findByPK(id, HourlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new TopGroupReport(domain);
		}
	}

	private TopGroupReport queryFromMonthlyBinary(int id, String domain) throws DalException {
		MonthlyReportContent content = m_monthlyReportContentDao.findByPK(id, MonthlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new TopGroupReport(domain);
		}
	}

	private TopGroupReport queryFromWeeklyBinary(int id, String domain) throws DalException {
		WeeklyReportContent content = m_weeklyReportContentDao.findByPK(id, WeeklyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new TopGroupReport(domain);
		}
	}

	@Override
	public TopGroupReport queryHourlyReport(String domain, Date start, Date end) {
		TopGroupReportMerger merger = new TopGroupReportMerger(new TopGroupReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = TopGroupAnalyzer.ID;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			List<HourlyReport> reports = null;
			try {
				reports = m_hourlyReportDao.findAllByDomainNamePeriod(new Date(startTime), domain, name,
				      HourlyReportEntity.READSET_FULL);
			} catch (DalException e) {
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					try {
						TopGroupReport reportModel = queryFromHourlyBinary(report.getId(), domain);

						reportModel.accept(merger);
					} catch (DalNotFoundException e) {
						// ignore
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		}
		TopGroupReport eventReport = merger.getTopGroupReport();

		eventReport.setStartTime(start);
		eventReport.setEndTime(new Date(end.getTime() - 1));

		Set<String> domains = queryAllDomainNames(start, end, TopGroupAnalyzer.ID);
		eventReport.getDomainNames().addAll(domains);
		return convert(eventReport);
	}

	@Override
	public TopGroupReport queryMonthlyReport(String domain, Date start) {
		TopGroupReport eventReport = new TopGroupReport(domain);

		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain, TopGroupAnalyzer.ID,
			      MonthlyReportEntity.READSET_FULL);
		
			eventReport = queryFromMonthlyBinary(entity.getId(), domain);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return convert(eventReport);
	}

	@Override
	public TopGroupReport queryWeeklyReport(String domain, Date start) {
		TopGroupReport eventReport = new TopGroupReport(domain);

		try {
			WeeklyReport entity = m_weeklyReportDao.findReportByDomainNamePeriod(start, domain, TopGroupAnalyzer.ID,
			      WeeklyReportEntity.READSET_FULL);

			eventReport = queryFromWeeklyBinary(entity.getId(), domain);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return convert(eventReport);
	}

	public class TpsStatistics extends BaseVisitor {

		public double m_duration;

		public TpsStatistics(double duration) {
			m_duration = duration;
		}

		@Override
		public void visitName(TopGroupName name) {
			if (m_duration > 0) {
				name.setTps(name.getTotalCount() * 1.0 / m_duration);
			}
		}

		@Override
		public void visitType(TopGroupType type) {
			if (m_duration > 0) {
				type.setTps(type.getTotalCount() * 1.0 / m_duration);
				super.visitType(type);
			}
		}
	}
}
