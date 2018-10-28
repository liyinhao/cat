package com.dianping.cat.consumer.topGroup;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupName;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupType;
import com.dianping.cat.consumer.topGroup.model.entity.Range;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.service.ProjectService;
import org.unidal.lookup.annotation.Inject;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import java.util.List;
import java.util.Map;

public class TopGroupAnalyzer extends AbstractMessageAnalyzer<TopGroupReport> implements LogEnabled {

	public static final String ID = "topGroup";

	@Inject
	private TopGroupDelegate m_delegate;

	@Inject(ID)
	private ReportManager<TopGroupReport> m_reportManager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Inject
	private ProjectService projectService;

	private TopGroupTpsStatisticsComputer m_computer = new TopGroupTpsStatisticsComputer();

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB, m_index);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public TopGroupReport getReport(String domain) {
		if (!Constants.ALL.equals(domain)) {
			long period = getStartTime();
			long timestamp = System.currentTimeMillis();
			long remainder = timestamp % 3600000;
			long current = timestamp - remainder;
			TopGroupReport report = m_reportManager.getHourlyReport(period, domain, false);

			report.getDomainNames().addAll(m_reportManager.getDomains(getStartTime()));
			if (period == current) {
				report.accept(m_computer.setDuration(remainder / 1000));
			} else if (period < current) {
				report.accept(m_computer.setDuration(3600));
			}
			return report;
		} else {
			Map<String, TopGroupReport> reports = m_reportManager.getHourlyReports(getStartTime());

			return m_delegate.createAggregatedReport(reports);
		}
	}

	@Override
	public ReportManager<TopGroupReport> getReportManager() {
		return m_reportManager;
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();

		String bu = null;
		Project project = projectService.findByDomain(domain);
		if (project == null){
			bu = "Defalut";
		}else {
			bu = project.getBu();
		}

		if (m_serverFilterConfigManager.validateDomain(domain)) {
			TopGroupReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);
			Message message = tree.getMessage();

			if (message instanceof Transaction) {
				processTransaction(report, tree, (Transaction) message, bu, domain);
			} else if (message instanceof Event) {
				processEvent(report, tree, (Event) message, bu, domain);
			}
		}
	}

	private void processEvent(TopGroupReport report, MessageTree tree, Event event, String bu, String domain) {
		int count = 1;
		TopGroupType type = report.findOrCreateGroup(bu).findOrCreateType(domain);
		TopGroupName name = type.findOrCreateName(domain);
		String messageId = tree.getMessageId();

		report.addBu(bu);
		type.incTotalCount(count);
		name.incTotalCount(count);

		if (event.isSuccess()) {
			type.setSuccessMessageUrl(messageId);
			name.setSuccessMessageUrl(messageId);
		} else {
			type.incFailCount(count);
			name.incFailCount(count);

			type.setFailMessageUrl(messageId);
			name.setFailMessageUrl(messageId);
		}
		type.setFailPercent(type.getFailCount() * 100.0 / type.getTotalCount());
		name.setFailPercent(name.getFailCount() * 100.0 / name.getTotalCount());

		processEventGrpah(name, event, count);
	}

	private void processEventGrpah(TopGroupName name, Event t, int count) {
		long current = t.getTimestamp() / 1000 / 60;
		int min = (int) (current % (60));
		Range range = name.findOrCreateRange(min);

		range.incCount(count);
		if (!t.isSuccess()) {
			range.incFails(count);
		}
	}

	private void processTransaction(TopGroupReport report, MessageTree tree, Transaction t, String bu, String domain) {
		List<Message> children = t.getChildren();

		for (Message child : children) {
			if (child instanceof Transaction) {
				processTransaction(report, tree, (Transaction) child, bu, domain);
			} else if (child instanceof Event) {
				processEvent(report, tree, (Event) child, bu, domain);
			}
		}
	}

	public void setReportManager(ReportManager<TopGroupReport> reportManager) {
		m_reportManager = reportManager;
	}

}
