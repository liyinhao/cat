package com.dianping.cat.report.page.topGroup.transform;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupName;
import com.dianping.cat.consumer.topGroup.model.entity.TopGroupType;
import com.dianping.cat.consumer.topGroup.model.entity.Group;
import com.dianping.cat.consumer.topGroup.model.transform.BaseVisitor;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import org.unidal.lookup.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PieGraphChartVisitor extends BaseVisitor {

	private String m_type;

	private String m_name;

	private Map<String, Long> m_items = new HashMap<String, Long>();

	private String m_ip;

	public PieGraphChartVisitor(String type, String name) {
		m_type = type;
		m_name = name;
	}

	public PieChart getPieChart() {
		PieChart chart = new PieChart();
		List<Item> items = new ArrayList<Item>();

		for (Entry<String, Long> entry : m_items.entrySet()) {
			Item item = new Item();

			item.setNumber(entry.getValue()).setTitle(entry.getKey());
			items.add(item);
		}
		chart.addItems(items);

		return chart;
	}

	@Override
	public void visitGroup(Group machine) {
		if (!Constants.ALL.equalsIgnoreCase(machine.getBu())) {
			m_ip = machine.getBu();

			for (TopGroupType type : machine.getTypes().values()) {
				if (m_type != null && m_type.equals(type.getId())) {
					if (StringUtils.isEmpty(m_name)) {
						m_items.put(m_ip, type.getTotalCount());
					} else {
						for (TopGroupName name : type.getNames().values()) {
							if (m_name.equals(name.getId())) {
								m_items.put(m_ip, name.getTotalCount());
								break;
							}
						}
					}
					break;
				}
			}
		}
	}
}
