package com.dianping.cat.consumer.topGroup;

import com.dianping.cat.Constants;

import com.dianping.cat.consumer.topGroup.model.entity.*;
import com.dianping.cat.consumer.topGroup.model.transform.DefaultMerger;

public class TopGroupReportMerger extends DefaultMerger {
	public TopGroupReportMerger(TopGroupReport eventReport) {
		super(eventReport);
	}

	@Override
	public void mergeGroup(Group old, Group group) {
	}

	@Override
	public void mergeName(TopGroupName old, TopGroupName other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
		old.setFailCount(old.getFailCount() + other.getFailCount());
		old.setTps(old.getTps() + other.getTps());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
		}

		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}

		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	@Override
	public void mergeRange(Range old, Range range) {
		old.setCount(old.getCount() + range.getCount());
		old.setFails(old.getFails() + range.getFails());
	}

	public Group mergesForAllMachine(TopGroupReport report) {
		Group machine = new Group(Constants.ALL);

		for (Group m : report.getGroups().values()) {
			if (!m.getBu().equals(Constants.ALL)) {
				visitGroupChildren(machine, m);
			}
		}

		return machine;
	}

	@Override
	public void mergeType(TopGroupType old, TopGroupType other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
		old.setFailCount(old.getFailCount() + other.getFailCount());
		old.setTps(old.getTps() + other.getTps());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
		}

		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}

		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	@Override
	public void visitTopGroupReport(TopGroupReport topGroupReport) {
		super.visitTopGroupReport(topGroupReport);

		TopGroupReport report = getTopGroupReport();
		report.getDomainNames().addAll(topGroupReport.getDomainNames());
		report.getBus().addAll(topGroupReport.getBus());
	}

}
