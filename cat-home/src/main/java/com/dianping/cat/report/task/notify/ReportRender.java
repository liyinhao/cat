package com.dianping.cat.report.task.notify;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;

public interface ReportRender {

	public String renderReport(EventReport report);

	public String renderReport(ProblemReport report);

	public String renderReport(TransactionReport report);

	public String renderReport(TopGroupReport report);
}
