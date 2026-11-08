package com.senati.biblioteca.service;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldif.LDIFReader;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Integración LDAP con autenticación centralizada.
 *
 * <p>Modos:
 * <ul>
 *   <li><b>embedded</b>: levanta un directorio LDAP en memoria (UnboundID) con usuarios demo.</li>
 *   <li><b>external</b>: consulta un servidor LDAP real mediante JNDI.</li>
 * </ul>
 * La configuración se lee de {@code ldap.properties}.</p>
 */
@ApplicationScoped
public class LdapService implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(LdapService.class.getName());
    public static final String PASSWORD_DEMO = "123456";

    private final Properties cfg = new Properties();
    private InMemoryDirectoryServer embedded;

    /** Datos de un usuario obtenidos desde LDAP. */
    public record LdapUser(String codigo, String nombre, String correo, boolean activo) {}

    @PostConstruct
    public void init() {
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("ldap.properties")) {
            if (in != null) cfg.load(in);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "No se pudo leer ldap.properties; se usa modo embedded", e);
        }
        if (esEmbebido()) startEmbedded();
    }

    private boolean esEmbebido() {
        return "embedded".equalsIgnoreCase(cfg.getProperty("ldap.mode", "embedded"));
    }

    private synchronized void startEmbedded() {
        if (embedded != null) return;
        try {
            String baseDn = cfg.getProperty("ldap.embedded.baseDn", "dc=senati,dc=edu,dc=pe");
            String bindDn = cfg.getProperty("ldap.embedded.bindDn", "cn=admin," + baseDn);
            String bindPw = cfg.getProperty("ldap.embedded.bindPassword", "admin123");
            int port = Integer.parseInt(cfg.getProperty("ldap.embedded.port", "10389"));

            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(baseDn);
            config.addAdditionalBindCredentials(bindDn, bindPw);
            config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("default", port));
            config.setSchema(null); // esquema permisivo para demo

            embedded = new InMemoryDirectoryServer(config);
            String ldif = "dn: " + baseDn + "\n"
                    + "objectClass: top\n"
                    + "objectClass: domain\n"
                    + "dc: senati\n\n"
                    + "dn: cn=admin," + baseDn + "\n"
                    + "objectClass: top\n"
                    + "objectClass: person\n"
                    + "cn: admin\n"
                    + "sn: admin\n"
                    + "userPassword: " + bindPw + "\n\n"
                    + "dn: ou=estudiantes," + baseDn + "\n"
                    + "objectClass: top\n"
                    + "objectClass: organizationalUnit\n"
                    + "ou: estudiantes\n\n"
                    + demoUser("U2026001", "Carlos Mendoza", "carlos.mendoza@senati.pe", "active", baseDn)
                    + demoUser("U2026002", "Ana Torres", "ana.torres@senati.pe", "active", baseDn)
                    + demoUser("U2026003", "Luis Rojas", "luis.rojas@senati.pe", "inactive", baseDn);
            embedded.importFromLDIF(false,
                    new LDIFReader(new ByteArrayInputStream(ldif.getBytes(StandardCharsets.UTF_8))));
            embedded.startListening();
            LOG.info("LDAP embebido iniciado en el puerto " + port + " (base: " + baseDn + ")");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "No se pudo iniciar el LDAP embebido; se usará el fallback de demo", e);
            embedded = null;
        }
    }

    private String demoUser(String uid, String cn, String mail, String status, String baseDn) {
        return "dn: uid=" + uid + ",ou=estudiantes," + baseDn + "\n"
                + "objectClass: top\n"
                + "objectClass: inetOrgPerson\n"
                + "uid: " + uid + "\n"
                + "cn: " + cn + "\n"
                + "sn: " + cn + "\n"
                + "mail: " + mail + "\n"
                + "employeeType: " + status + "\n"
                + "userPassword: " + PASSWORD_DEMO + "\n\n";
    }

    /** Valida si el estudiante está activo en el directorio LDAP. */
    public boolean validarEstudianteLdap(String codigoUtu) {
        if (codigoUtu == null || codigoUtu.isBlank()) return false;
        return buscarUsuario(codigoUtu.trim()).map(LdapUser::activo).orElse(false);
    }

    /** Busca los datos del usuario en LDAP (bind de servicio). */
    public Optional<LdapUser> buscarUsuario(String codigoUtu) {
        if (codigoUtu == null || codigoUtu.isBlank()) return Optional.empty();
        String baseDn = baseDn();
        String url = url();
        if (url == null) return fallback(codigoUtu);

        Hashtable<String, String> env = entorno(bindDn(), bindPassword(), url);
        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            controls.setReturningAttributes(new String[]{"uid", "cn", "mail", "employeeType"});
            controls.setCountLimit(1);
            String filter = String.format(cfg.getProperty("ldap.external.userFilter", "(uid=%s)"),
                    escapeLdap(codigoUtu));
            NamingEnumeration<SearchResult> results = ctx.search(baseDn, filter, controls);
            if (!results.hasMore()) return Optional.empty();
            SearchResult sr = results.next();
            Attributes attrs = sr.getAttributes();
            String nombre = attr(attrs, "cn", codigoUtu);
            String correo = attr(attrs, "mail", "");
            String estado = attr(attrs, "employeeType", "active");
            boolean activo = "active".equalsIgnoreCase(estado);
            return Optional.of(new LdapUser(codigoUtu, nombre, correo, activo));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Búsqueda LDAP falló para " + codigoUtu + ": " + e.getMessage());
            return fallback(codigoUtu);
        } finally {
            if (ctx != null) try { ctx.close(); } catch (Exception ignored) {}
        }
    }

    /**
     * Autentica a un estudiante contra LDAP (bind como usuario) y devuelve sus datos.
     * Demuestra la autenticación centralizada real.
     */
    public Optional<LdapUser> autenticar(String codigoUtu, String password) {
        if (codigoUtu == null || codigoUtu.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }
        String baseDn = baseDn();
        String url = url();
        if (url == null) {
            // Modo sin directorio: credenciales de demo
            if (PASSWORD_DEMO.equals(password)) return fallback(codigoUtu).filter(LdapUser::activo);
            return Optional.empty();
        }
        String userDn = "uid=" + escapeDn(codigoUtu.trim()) + ",ou=estudiantes," + baseDn;
        Hashtable<String, String> env = entorno(userDn, password, url);
        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(env); // bind real con la clave del estudiante
            return buscarUsuario(codigoUtu).filter(LdapUser::activo);
        } catch (Exception e) {
            LOG.log(Level.FINE, "Credenciales LDAP inválidas para " + codigoUtu);
            return Optional.empty();
        } finally {
            if (ctx != null) try { ctx.close(); } catch (Exception ignored) {}
        }
    }

    private Optional<LdapUser> fallback(String codigo) {
        // Permite probar sin directorio: los códigos U... se consideran activos
        boolean activo = codigo != null && codigo.matches("U\\d{6,}");
        return Optional.of(new LdapUser(codigo, codigo, codigo.toLowerCase() + "@senati.pe", activo));
    }

    private Hashtable<String, String> entorno(String principal, String credentials, String url) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, url);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, principal);
        env.put(Context.SECURITY_CREDENTIALS, credentials);
        env.put("com.sun.jndi.ldap.connect.timeout", "3000");
        env.put("com.sun.jndi.ldap.read.timeout", "3000");
        return env;
    }

    private String attr(Attributes attrs, String name, String def) throws Exception {
        Attribute a = attrs.get(name);
        Object v = a == null ? null : a.get();
        return v == null ? def : v.toString();
    }

    private String baseDn() {
        return cfg.getProperty(esEmbebido() ? "ldap.embedded.baseDn" : "ldap.external.baseDn",
                "dc=senati,dc=edu,dc=pe");
    }

    private String bindDn() {
        return cfg.getProperty(esEmbebido() ? "ldap.embedded.bindDn" : "ldap.external.bindDn",
                "cn=admin," + baseDn());
    }

    private String bindPassword() {
        return cfg.getProperty(esEmbebido() ? "ldap.embedded.bindPassword" : "ldap.external.bindPassword",
                "admin123");
    }

    private String url() {
        if (esEmbebido()) {
            return embedded == null ? null
                    : "ldap://127.0.0.1:" + cfg.getProperty("ldap.embedded.port", "10389");
        }
        return cfg.getProperty("ldap.external.url");
    }

    private String escapeLdap(String s) {
        return s.replace("\\", "\\5c").replace("*", "\\2a")
                .replace("(", "\\28").replace(")", "\\29").replace("\0", "\\00");
    }

    private String escapeDn(String s) {
        return s.replace("\\", "\\\\").replace(",", "\\,").replace("+", "\\+")
                .replace("\"", "\\\"").replace("<", "\\<").replace(">", "\\>");
    }

    @PreDestroy
    public void shutdown() {
        if (embedded != null) {
            try { embedded.shutDown(true); } catch (Exception ignored) {}
            embedded = null;
        }
    }
}
