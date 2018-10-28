package com.dianping.cat.report.page.topGroup.service;

import com.dianping.cat.consumer.topGroup.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.topGroup.TopGroupAnalyzer;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupReport;
import com.dianping.cat.report.service.BaseRemoteModelService;
import org.xml.sax.SAXException;

import java.io.IOException;

public class RemoteTopGroupService extends BaseRemoteModelService<TopGroupReport> {
	public RemoteTopGroupService() {
		super(TopGroupAnalyzer.ID);
	}

	@Override
	protected TopGroupReport buildModel(String xml) throws SAXException, IOException {
		return DefaultSaxParser.parse(xml);
	}
}
