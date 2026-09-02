<%-- 
    File di reindirizzamento radice (index.jsp).
    Reindirizza automaticamente le richieste in ingresso alla pagina principale (home.jsp)
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%-- Importazione della libreria JSTL Core per il controllo del flusso e il reindirizzamento --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- Reindirizzamento nativo JSTL: include e gestisce automaticamente il Context Path dell'applicazione --%>
<c:redirect url="/jsp/common/home.jsp" />