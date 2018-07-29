package com.dianping.cat.report.page.heartbeat;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.cat.consumer.heartbeat.model.entity.Detail;
import com.dianping.cat.consumer.heartbeat.model.entity.Extension;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.home.heartbeat.entity.Metric;
import com.dianping.cat.report.graph.svg.GraphBuilder;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;

public class HeartbeatSvgGraph {

	private HeartbeatDisplayPolicyManager m_manager;

	private static final String DAL = "dal";

	private static final Map<String, Integer> INDEX = new HashMap<String, Integer>();

	private static final AtomicInteger INDEX_COUNTER = new AtomicInteger(0);

	private GraphBuilder m_builder;

	private Map<String, Map<String, SvgItem>> m_extensions = new LinkedHashMap<String, Map<String, SvgItem>>();

	public HeartbeatSvgGraph(GraphBuilder builder, HeartbeatDisplayPolicyManager manager) {
		m_builder = builder;
		m_manager = manager;
	}

	private void addSortedGroups(Map<String, Map<String, SvgItem>> tmpExtensions) {
		List<String> orderedGroupNames = m_manager.sortGroupNames(m_extensions.keySet());

		for (String groupName : orderedGroupNames) {
			Map<String, SvgItem> extensionGroup = m_extensions.get(groupName);

			tmpExtensions.put(groupName, extensionGroup);
		}
	}

	private void buildExtensionGraph(Map<String, ExtensionGroup> graphs, Entry<String, Map<String, SvgItem>> entry) {
		String title = entry.getKey();

		if (title.equalsIgnoreCase(DAL)) {
			for (Entry<String, SvgItem> subEntry : entry.getValue().entrySet()) {
				String key = subEntry.getKey();
				int pos = key.lastIndexOf('-');

				if (pos > 0) {
					String db = "Dal " + key.substring(0, pos);
					String subTitle = key.substring(pos + 1);
					ExtensionGroup extensitonGroup = graphs.get(db);

					if (extensitonGroup == null) {
						extensitonGroup = new ExtensionGroup();

						graphs.put(db, extensitonGroup);
					}

					if (!INDEX.containsKey(subTitle)) {
						INDEX.put(subTitle, INDEX_COUNTER.getAndIncrement());
					}

					String svg = m_builder.build(new HeartbeatSvgBuilder(INDEX.get(subTitle), subTitle, subTitle, "Minute", "Count",
					      subEntry.getValue().getItem()));
					extensitonGroup.getSvgs().put(subTitle, svg);
				}
			}
		} else {
			ExtensionGroup extensitonGroup = graphs.get(title);

			if (extensitonGroup == null) {
				extensitonGroup = new ExtensionGroup();
				graphs.put(title, extensitonGroup);
			}

			int i = 0;
			for (Entry<String, SvgItem> item : entry.getValue().entrySet()) {
				String key = item.getKey();
				Metric metricConfig = m_manager.queryMetric(title, key);
				String svgTitle = item.getValue().getTitle();
				String lable = null;

				if (metricConfig != null) {
					String configTitle = metricConfig.getTitle();

					if (svgTitle == null) {
						svgTitle = configTitle;
					}

					lable = metricConfig.getLable();
				} else {
					lable = m_manager.queryMetricLable(title, key);
				}

				if (svgTitle == null){
					svgTitle = key;
				}

				if (lable == null){
					lable = "MB";
				}

				String svg = m_builder.build(new HeartbeatSvgBuilder(i++, key, svgTitle, "Minute", lable, item.getValue().getItem()));
				extensitonGroup.getSvgs().put(key, svg);
			}
		}
	}

	private Map<String, Map<String, SvgItem>> dealWithExtensions() {
		Map<String, Map<String, SvgItem>> result = new LinkedHashMap<String, Map<String, SvgItem>>();

		addSortedGroups(result);
		for (Entry<String, Map<String, SvgItem>> entry : result.entrySet()) {
			String groupName = entry.getKey();
			Map<String, SvgItem> originMetrics = entry.getValue();
			List<String> metricNames = m_manager.sortMetricNames(groupName, originMetrics.keySet());
			Map<String, SvgItem> normalizedMetrics = new LinkedHashMap<String, SvgItem>();

			for (String metricName : metricNames) {

				SvgItem svgItem = originMetrics.get(metricName);
				double[] values = svgItem.getItem();

				if (m_manager.isDelta(groupName, metricName)) {
					values = getAddedCount(values);
				}

				int unit = m_manager.queryUnit(groupName, metricName);

				for (int i = 0; i <= 59; i++) {
					values[i] = values[i] / unit;
				}
				normalizedMetrics.put(metricName, svgItem);
			}
			entry.setValue(normalizedMetrics);
		}
		return result;
	}

	public HeartbeatSvgGraph display(HeartbeatReport report, String ip) {
		if (report == null) {
			return this;
		}
		Machine machine = report.getMachines().get(ip);

		if (machine == null) {
			return this;
		}

		List<Period> periods = machine.getPeriods();
		int size = periods.size();

		for (; size > 0; size--) {
			Period period = periods.get(size - 1);
			int minute = period.getMinute();

			for (Entry<String, Extension> entry : period.getExtensions().entrySet()) {
				String group = entry.getKey();
				Map<String, SvgItem> groups = m_extensions.get(group);

				if (groups == null) {
					groups = new LinkedHashMap<String, SvgItem>();

					m_extensions.put(group, groups);
				}
				for (Entry<String, Detail> detail : entry.getValue().getDetails().entrySet()) {
					String key = detail.getKey();
					SvgItem svgItem = groups.get(key);

					if (svgItem == null) {
						svgItem = new SvgItem(new double[60], detail.getValue().getDescription() );
						groups.put(key, svgItem);
					}

					double[] doubles = svgItem.getItem();
					doubles[minute] = detail.getValue().getValue();
				}
			}
		}
		m_extensions = dealWithExtensions();
		return this;
	}

	private double[] getAddedCount(double[] source) {
		double[] result = new double[60];

		for (int i = 1; i <= 59; i++) {
			if (source[i - 1] > 0) {
				double d = source[i] - source[i - 1];
				if (d < 0) {
					d = source[i];
				}
				result[i] = d;
			}
		}
		return result;
	}

	public GraphBuilder getBuilder() {
		return m_builder;
	}

	public Map<String, ExtensionGroup> getExtensionGraph() {
		Map<String, ExtensionGroup> graphs = new LinkedHashMap<String, ExtensionGroup>();

		for (Entry<String, Map<String, SvgItem>> items : m_extensions.entrySet()) {
			buildExtensionGraph(graphs, items);
		}

		return graphs;
	}

	public class ExtensionGroup {

		private Map<String, String> m_svgs = new LinkedHashMap<String, String>();

		public int getHeight() {
			if (m_svgs != null) {
				int size = m_svgs.size();

				if (size % 3 == 0) {
					return size / 3;
				} else {
					return size / 3 + 1;
				}
			} else {
				return 0;
			}
		}

		public Map<String, String> getSvgs() {
			return m_svgs;
		}
	}

	private class SvgItem {
		private double[] item;
		private String title;

		public SvgItem(double[] item, String title) {
			this.item = item;
			this.title = title;
		}

		public double[] getItem() {
			return item;
		}

		public String getTitle() {
			return title;
		}
	}

}
