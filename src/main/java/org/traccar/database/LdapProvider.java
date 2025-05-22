package org.traccar.database;

import java.util.Hashtable;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.config.Config;
import org.traccar.model.User;


public class LdapProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapProvider.class);

    private String url;
    private String searchBase;
    private String idAttribute;
    private String nameAttribute;
    private String mailAttribute;
    private String searchFilter;
    private String adminFilter;
    private String serviceUser;
    private String servicePassword;

    public LdapProvider(Config config) {
        String url = config.getString("ldap.url");
        if (url != null) {
            this.url = url;
        } else {
            this.url = "ldap://" + config.getString("ldap.server") + ":" + config.getInteger("ldap.port", 389);
        }
        this.searchBase = config.getString("ldap.base");
        this.idAttribute = config.getString("ldap.idAttribute", "uid");
        this.nameAttribute = config.getString("ldap.nameAttribute", "cn");
        this.mailAttribute = config.getString("ldap.mailAttribute", "mail");
        this.searchFilter = config.getString("ldap.searchFilter", "(" + this.idAttribute + "=:login)");
        String adminGroup = config.getString("ldap.adminGroup");
        this.adminFilter = config.getString("ldap.adminFilter");
        if (this.adminFilter == null && adminGroup != null) {
            this.adminFilter = "(&(" + this.idAttribute + "=:login)(memberOf=" + adminGroup + "))";
        }
        this.serviceUser = config.getString("ldap.user");
        this.servicePassword = config.getString("ldap.password");
    }

    private InitialDirContext auth(String accountName, String password) throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("java.naming.provider.url", this.url);

        env.put("java.naming.security.authentication", "simple");
        env.put("java.naming.security.principal", accountName);
        env.put("java.naming.security.credentials", password);

        return new InitialDirContext(env);
    }

    private boolean isAdmin(String accountName) {
        if (this.adminFilter != null) {
            try {
                InitialDirContext context = initContext();
                String searchString = this.adminFilter.replace(":login", accountName);
                SearchControls searchControls = new SearchControls();
                searchControls.setSearchScope(2);
                NamingEnumeration<SearchResult> results = context.search(this.searchBase, searchString, searchControls);
                if (results.hasMoreElements()) {
                    results.nextElement();
                    if (results.hasMoreElements()) {
                        LOGGER.warn("Matched multiple users for the accountName: " + accountName);
                        return false;
                    }
                    return true;
                }
            } catch (NamingException e) {
                return false;
            }
        }
        return false;
    }

    public InitialDirContext initContext() throws NamingException {
        return auth(this.serviceUser, this.servicePassword);
    }

    private SearchResult lookupUser(String accountName) throws NamingException {
        InitialDirContext context = initContext();

        String searchString = this.searchFilter.replace(":login", accountName);

        SearchControls searchControls = new SearchControls();
        String[] attributeFilter = {this.idAttribute, this.nameAttribute, this.mailAttribute};
        searchControls.setReturningAttributes(attributeFilter);
        searchControls.setSearchScope(2);

        NamingEnumeration<SearchResult> results = context.search(this.searchBase, searchString, searchControls);

        SearchResult searchResult = null;
        if (results.hasMoreElements()) {
            searchResult = results.nextElement();
            if (results.hasMoreElements()) {
                LOGGER.warn("Matched multiple users for the accountName: " + accountName);
                return null;
            }
        }

        return searchResult;
    }


    public User getUser(String accountName) {
        User user = new User();
        try {
            SearchResult ldapUser = lookupUser(accountName);
            if (ldapUser != null) {
                Attribute attribute = ldapUser.getAttributes().get(this.idAttribute);
                if (attribute != null) {
                    user.setLogin((String) attribute.get());
                } else {
                    user.setLogin(accountName);
                }
                attribute = ldapUser.getAttributes().get(this.nameAttribute);
                if (attribute != null) {
                    user.setName((String) attribute.get());
                } else {
                    user.setName(accountName);
                }
                attribute = ldapUser.getAttributes().get(this.mailAttribute);
                if (attribute != null) {
                    user.setEmail((String) attribute.get());
                } else {
                    user.setEmail(accountName);
                }
            }
            user.setAdministrator(isAdmin(accountName));
        } catch (NamingException e) {
            user.setLogin(accountName);
            user.setName(accountName);
            user.setEmail(accountName);
            LOGGER.warn("User lookup error", e);
        }
        return user;
    }

    public boolean login(String username, String password) {
        try {
            SearchResult ldapUser = lookupUser(username);
            if (ldapUser != null) {
                auth(ldapUser.getNameInNamespace(), password).close();
                return true;
            }
        } catch (NamingException e) {
            return false;
        }
        return false;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\database\LdapProvider.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */