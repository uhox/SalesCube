<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="ja">
<head>
	<title>システムエラー</title>
	<%@ include file="/WEB-INF/view/common/header.jsp" %>
</head>
<body>
	
	<%@ include file="/WEB-INF/view/common/titlebar.jsp" %>

	
	<jsp:include page="/WEB-INF/view/common/menubar.jsp">
		<jsp:param name="PARENT_MENU_ID" value=""/>
		<jsp:param name="MENU_ID" value="0102"/>
	</jsp:include>

	
	<div id="main_function">
		<table class="forms" style="height: 40px; margin-top: 30px;">
			<tr>
				<th>システムエラーが発生しました</th>
			</tr>
			<tr>
				<td><pre><html:errors/></pre></td>
			</tr>
		</table>

		<div style="padding-left: 440px; margin-top: 50px">
			<a href="javascript:window.location.doHref('${f:url("/menu")}');">メニュー画面</a>
		</div>
	</div>

</body>
</html>
