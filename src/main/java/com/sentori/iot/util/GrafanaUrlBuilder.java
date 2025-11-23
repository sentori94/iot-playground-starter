package com.sentori.iot.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Classe utilitaire pour construire des URLs Grafana avec les paramètres appropriés
 */
@Component
public class GrafanaUrlBuilder {

    @Value("${app.grafana.base-url}")
    private String gfBase;

    @Value("${app.grafana.dashboard-path}")
    private String gfPath;

    @Value("${app.grafana.org-id:1}")
    private String gfOrgId;

    @Value("${app.grafana.ds-prom-uid:prometheus}")
    private String gfDsPromUid;

    @Value("${app.grafana.timezone:browser}")
    private String gfTimezone;

    @Value("${app.grafana.default-from:now-15m}")
    private String gfDefaultFrom;

    @Value("${app.grafana.default-to:now}")
    private String gfDefaultTo;

    @Value("${app.grafana.default-refresh:5s}")
    private String gfRefresh;

    /**
     * Construit une URL Grafana complète avec filtres pour un run et un user
     *
     * @param runId Identifiant du run (peut être UUID ou runId format "run-20250123-...")
     * @param username Nom de l'utilisateur
     * @return URL Grafana complète avec tous les paramètres
     */
    public String buildUrl(String runId, String username) {
        String user = (username == null || username.isBlank()) ? "anonymous" : username;

        return UriComponentsBuilder.fromHttpUrl(gfBase)
                .path(gfPath)
                .queryParam("orgId", gfOrgId)
                .queryParam("from", gfDefaultFrom)
                .queryParam("to", gfDefaultTo)
                .queryParam("timezone", gfTimezone)
                .queryParam("var-DS_PROM", gfDsPromUid)
                .queryParam("var-user", user)
                .queryParam("var-run", runId)
                .queryParam("var-sensor", "$__all")
                .queryParam("refresh", gfRefresh)
                .build(true) // garde les $ et autres caractères spéciaux intacts
                .toUriString();
    }

    /**
     * Construit une URL Grafana simplifiée (pour compatibilité avec l'ancien code)
     *
     * @param runId Identifiant du run
     * @param username Nom de l'utilisateur
     * @return URL Grafana simplifiée
     */
    public String buildSimpleUrl(String runId, String username) {
        String user = (username == null || username.isBlank()) ? "anonymous" : username;

        return UriComponentsBuilder.fromHttpUrl(gfBase)
                .path(gfPath)
                .queryParam("var-run", runId)
                .queryParam("var-user", user)
                .queryParam("refresh", gfRefresh)
                .queryParam("from", "now-" + gfDefaultFrom)
                .queryParam("to", gfDefaultTo)
                .build(true)
                .toUriString();
    }
}

