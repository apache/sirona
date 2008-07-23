<%@ page contentType="text/html; charset=ISO-8859-1" %>
<%@ taglib prefix="monitoring" uri="http://commons.apache.org/monitoring/tags"%>
<html>
<head>
    <style type="text/css">
    li { list-style: none; }
    body { font-family: arial, sans-serif; font-size: 0.9em }
    </style>
</head>
<body>
<form>
<radiogroup>
  <ul>
    <li><input type=radio value="/monitors" checked="checked">all monitors</li>
    <li><input type=radio value="/monitorsInCategory">all monitors from category <select><option>default</option></select></li>
    <li><input type=radio value="/monitorsInSubsystem">all monitors from subsystem <select><option>default</option></select></li>
  </ul>
<radiogroup>
<ul>
    <li><input type="checkbox" checked="checked">performances <monitoring:unit name="unit" unit="ms"/>
        <ul>
            <li><input type="checkbox">current value</li>
            <li><input type="checkbox">total</li>
            <li><input type="checkbox">hits</li>
            <li><input type="checkbox">min</li>
            <li><input type="checkbox">max</li>
            <li><input type="checkbox">mean</li>
            <li><input type="checkbox">standard deviation</li>
        </ul>
    </li>
    <li><input type="checkbox" checked="checked">concurrency <monitoring:unit name="unit" unit=""/>
        <ul>
            <li><input type="checkbox">current value</li>
            <li><input type="checkbox">min</li>
            <li><input type="checkbox">max</li>
            <li><input type="checkbox">mean</li>
            <li><input type="checkbox">standard deviation</li>
        </ul>

    </li>
</ul>
</form>
</body>
</html>
