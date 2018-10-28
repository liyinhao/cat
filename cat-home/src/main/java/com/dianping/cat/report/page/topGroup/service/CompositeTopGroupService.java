package com.dianping.cat.report.page.topGroup.service;

import com.dianping.cat.consumer.topGroup.TopGroupReportMerger;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;
import com.dianping.cat.consumer.topGroup.TopGroupAnalyzer;
import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;

import java.util.List;

public class CompositeTopGroupService extends BaseCompositeModelService<TopGroupReport> {
	public CompositeTopGroupService() {
		super(TopGroupAnalyzer.ID);
	}

	@Override
	protected BaseRemoteModelService<TopGroupReport> createRemoteService() {
		return new RemoteTopGroupService();
	}

	@Override
	protected TopGroupReport merge(ModelRequest request, List<ModelResponse<TopGroupReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		TopGroupReportMerger merger = new TopGroupReportMerger(new TopGroupReport(request.getDomain()));
		for (ModelResponse<TopGroupReport> response : responses) {
			TopGroupReport model = response.getModel();
			if (model != null) {
				model.accept(merger);
			}
		}

		return merger.getTopGroupReport();
	}
}
