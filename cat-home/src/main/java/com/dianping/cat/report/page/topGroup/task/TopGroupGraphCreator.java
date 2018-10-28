package com.dianping.cat.report.page.topGroup.task;

import com.dianping.cat.consumer.topGroup.model.entity.*;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.report.task.GraphLine;

import java.util.*;
import java.util.Map.Entry;

public class TopGroupGraphCreator {

	private long[] arrayAdd(long[] src, long added[]) {
		int size = added.length;
		if (src == null) {
			src = new long[size];
		}
		for (int i = 0; i < size; i++) {
			src[i] = src[i] + added[i];
		}
		return src;
	}

	private String arrayToString(long[] array) {
		StringBuilder sb = new StringBuilder();
		int size = 60;
		for (int i = 0; i < size; i++) {
			sb.append(array[i]);
			if (i < 59) {
				sb.append(',');
			}
		}
		return sb.toString();
	}

	private long[] getFailsCount(List<Range> ranges) {
		long[] value = new long[60];
		for (Range range : ranges) {
			int minute = range.getValue();
			value[minute] = range.getFails();
		}
		return value;
	}

	private long[] getTotalCount(List<Range> ranges) {
		long[] value = new long[60];
		for (Range range : ranges) {
			int minute = range.getValue();
			value[minute] = range.getCount();
		}
		return value;
	}

	public List<Graph> splitReportToGraphs(Date reportPeriod, String domainName, String reportName,
	      TopGroupReport eventReport) {
		Set<String> ips = eventReport.getBus();
		List<Graph> graphs = new ArrayList<Graph>(ips.size() + 1);
		Map<String, GraphLine> allDetailCache = new TreeMap<String, GraphLine>();
		Map<String, GraphLine> allSummaryCache = new TreeMap<String, GraphLine>();
		Date creationDate = new Date();
		for (String ip : ips) {
			Graph graph = new Graph();
			graph.setIp(ip);
			graph.setDomain(domainName);
			graph.setName(reportName);
			graph.setPeriod(reportPeriod);
			graph.setType(3);
			graph.setCreationDate(creationDate);
			Group machine = eventReport.getGroups().get(ip);
			Map<String, TopGroupType> types = machine.getTypes();
			StringBuilder detailBuilder = new StringBuilder();
			StringBuilder summaryBuilder = new StringBuilder();
			for (Entry<String, TopGroupType> eventEntry : types.entrySet()) {
				TopGroupType eventType = eventEntry.getValue();
				long[] typeCounts = new long[60];
				long[] typeFails = new long[60];

				Map<String, TopGroupName> names = eventType.getNames();
				for (Entry<String, TopGroupName> nameEntry : names.entrySet()) {
					TopGroupName eventName = nameEntry.getValue();
					List<Range> ranges = new ArrayList<Range>(eventName.getRanges().values());
					detailBuilder.append(eventType.getId());
					detailBuilder.append('\t');
					detailBuilder.append(eventName.getId());
					detailBuilder.append('\t');

					long[] totalCount = getTotalCount(ranges);
					detailBuilder.append(arrayToString(totalCount));
					detailBuilder.append('\t');
					long[] failCount = getFailsCount(ranges);
					detailBuilder.append(arrayToString(failCount));
					detailBuilder.append('\n');

					String key = eventType.getId() + "\t" + eventName.getId();
					GraphLine detailLine = allDetailCache.get(key);
					if (detailLine == null) {
						detailLine = new GraphLine();
						allDetailCache.put(key, detailLine);
					}

					detailLine.totalCounts = arrayAdd(detailLine.totalCounts, totalCount);
					detailLine.failCounts = arrayAdd(detailLine.failCounts, failCount);

					typeCounts = arrayAdd(typeCounts, totalCount);
					typeFails = arrayAdd(typeFails, failCount);
				}

				String summaryKey = eventType.getId();
				GraphLine summaryLine = allSummaryCache.get(summaryKey);
				if (summaryLine == null) {
					summaryLine = new GraphLine();
					allSummaryCache.put(summaryKey, summaryLine);
				}
				summaryLine.totalCounts = arrayAdd(summaryLine.totalCounts, typeCounts);
				summaryLine.failCounts = arrayAdd(summaryLine.failCounts, typeFails);

				summaryBuilder.append(eventType.getId());
				summaryBuilder.append('\t');
				summaryBuilder.append(arrayToString(typeCounts));
				summaryBuilder.append('\t');
				summaryBuilder.append(arrayToString(typeFails));
				summaryBuilder.append('\n');
			}
			graph.setDetailContent(detailBuilder.toString());
			graph.setSummaryContent(summaryBuilder.toString());
			graphs.add(graph);
		}

		Graph allGraph = new Graph();
		allGraph.setIp("all");
		allGraph.setDomain(domainName);
		allGraph.setName(reportName);
		allGraph.setPeriod(reportPeriod);
		allGraph.setType(3);
		allGraph.setCreationDate(creationDate);

		StringBuilder detailSb = new StringBuilder();
		for (Entry<String, GraphLine> entry : allDetailCache.entrySet()) {
			detailSb.append(entry.getKey());
			detailSb.append('\t');
			GraphLine value = entry.getValue();
			detailSb.append(arrayToString(value.totalCounts));
			detailSb.append('\t');
			detailSb.append(arrayToString(value.failCounts));
			detailSb.append('\t');
			detailSb.append('\n');
		}
		allGraph.setDetailContent(detailSb.toString());

		StringBuilder summarySb = new StringBuilder();
		for (Entry<String, GraphLine> entry : allSummaryCache.entrySet()) {
			summarySb.append(entry.getKey());
			summarySb.append('\t');
			GraphLine value = entry.getValue();
			summarySb.append(arrayToString(value.totalCounts));
			summarySb.append('\t');
			summarySb.append(arrayToString(value.failCounts));
			summarySb.append('\n');
		}
		allGraph.setSummaryContent(summarySb.toString());
		graphs.add(allGraph);
		return graphs;
	}
}