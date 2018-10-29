<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.topGroup.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.topGroup.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.topGroup.Model" scope="request" />
<c:set var="report" value="${model.report}" />

<a:report
	title="TopGroup Report${empty payload.type ? '' : ' :: '}<a href='?domain=${model.domain}&date=${model.date}&type=${payload.type}'>${payload.type}</a>"
	navUrlPrefix="ip=${model.ipAddress}&queryname=${model.queryName}&domain=${model.domain}${empty payload.encodedType ? '' : '&type='}${payload.encodedType}"
	timestamp="${w:format(model.creatTime,'yyyy-MM-dd HH:mm:ss')}">

	<jsp:attribute name="subtitle">${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>
<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>

<table class="machines">
	<tr class="left">
		<th>&nbsp;[&nbsp; 
			<c:choose>
				<c:when test="${model.ipAddress eq 'All'}">
					<a href="?domain=${model.domain}&date=${model.date}&type=${payload.encodedType}&queryname=${model.queryName}" class="current">All</a>
				</c:when>
				<c:otherwise>
					<a href="?domain=${model.domain}&date=${model.date}&type=${payload.encodedType}&queryname=${model.queryName}">All</a>
				</c:otherwise>
			</c:choose> &nbsp;]&nbsp; <c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
   	  		<c:choose>
				<c:when test="${model.ipAddress eq ip}">
					<a href="?domain=${model.domain}&ip=${ip}&date=${model.date}&type=${payload.encodedType}&queryname=${model.queryName}" class="current">${ip}</a>
				</c:when>
				<c:otherwise>
					<a href="?domain=${model.domain}&ip=${ip}&date=${model.date}&type=${payload.encodedType}&queryname=${model.queryName}">${ip}</a>
				</c:otherwise>
			</c:choose>
   	 		&nbsp;]&nbsp;
			</c:forEach>
		</th>
	</tr>
</table>
<script type="text/javascript" src="/cat/js/appendHostname.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		appendHostname(${model.ipToHostnameStr});
	});

 	 $('.position').hide();
     $('#Dashboard_report').addClass('active open');
     $('#dashbord_topGroup').addClass('active');

</script>
<table class="groups">
	<tr class="left">
		<th> 
			<c:forEach var="group" items="${model.groups}">
	   	  		&nbsp;[&nbsp;
	   	  			<a href="?op=groupReport&domain=${model.domain}&date=${model.date}&group=${group}">${group}</a>
	   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th>
	</tr>
</table>
<table class='table table-hover table-striped table-condensed ' style="width:100%;">
	<tr>
	<th class="left"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&sort=type">App</a></th>
	<th class="right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&sort=total">Total</a></th>
	<th class="right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&sort=failure">Failure</a></th>
	<th class="right"><a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&sort=failurePercent">Failure%</a></th>
	<th class="right">Detail Link</th>
	</tr>
	<c:forEach var="item" items="${model.displayTypeReport.results}" varStatus="status">
		<c:set var="e" value="${item.detail}" />
		<c:set var="lastIndex" value="${status.index}" />
		<tr class="right">
			<td class="left">
				<a href="?domain=${model.domain}&date=${model.date}&ip=${model.ipAddress}&sort=type"><a href="?op=graphs&domain=${model.domain}&date=${model.date}&type=${item.type}&ip=${model.ipAddress}" class="graph_link" data-status="${status.index}">[:: show ::]</a>
				&nbsp;&nbsp;${item.detail.id}</a>
			</td>
			<td>${w:format(e.totalCount,'#,###,###,###,##0')}</td>
			<td>${w:format(e.failCount,'#,###,###,###,##0')}</td>
			<td>&nbsp;${w:format(e.failPercent/100,'0.0000%')}</td>
<td><a href="/cat/r/p/domain=${report.domain}&date=${model.date}">detail</a></td>
		</tr>
		<tr class="graphs">
			<td colspan="7" style="display:none"><div id="${status.index}" style="display: none"></div></td>
		</tr>
		<tr></tr>
	</c:forEach>

</table>

<font color="white">${lastIndex+1}</font>
<res:useJs value="${res.js.local.topGroup_js}" target="bottom-js" />

</jsp:body>
</a:report>
