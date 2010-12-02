/**
 * Distribution License:
 * BibleDesktop is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License, version 2.1 as published by
 * the Free Software Foundation. This program is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * The License is available on the internet at:
 *       http://www.gnu.org/copyleft/lgpl.html
 * or by writing to:
 *      Free Software Foundation, Inc.
 *      59 Temple Place - Suite 330
 *      Boston, MA 02111-1307, USA
 *
 * Copyright: 2005
 *     The copyright to this program is held by it's authors.
 *
 * ID: $Id$
 */
package org.crosswire.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * A WebResource is backed by an URL and potentially the proxy through which it
 * need go. It can get basic information about the resource and it can get the
 * resource. The requests are subject to a timeout, which can be set via the
 * constructor or previously by a call to set the default timeout. The initial
 * default timeout is 750 milliseconds.
 * 
 * 
 * @see gnu.lgpl.License for license details.<br>
 *      The copyright to this program is held by it's authors.
 * @author DM Smith [dmsmith555 at yahoo dot com]
 */
public class WebResource {
    /**
     * Construct a WebResource for the given URL, while timing out if too much
     * time has passed.
     * 
     * @param theURI
     *            the Resource to get via HTTP
     */
    public WebResource(URI theURI) {
        this(theURI, null, null, timeout);
    }

    /**
     * Construct a WebResource for the given URL, while timing out if too much
     * time has passed.
     * 
     * @param theURI
     *            the Resource to get via HTTP
     * @param theTimeout
     *            the length of time in milliseconds to allow a connection to
     *            respond before timing out
     */
    public WebResource(URI theURI, int theTimeout) {
        this(theURI, null, null, theTimeout);
    }

    /**
     * Construct a WebResource for the given URL, going through the optional
     * proxy and default port, while timing out if too much time has passed.
     * 
     * @param theURI
     *            the Resource to get via HTTP
     * @param theProxyHost
     *            the proxy host or null
     */
    public WebResource(URI theURI, String theProxyHost) {
        this(theURI, theProxyHost, null, timeout);
    }

    /**
     * Construct a WebResource for the given URL, going through the optional
     * proxy and default port, while timing out if too much time has passed.
     * 
     * @param theURI
     *            the Resource to get via HTTP
     * @param theProxyHost
     *            the proxy host or null
     * @param theTimeout
     *            the length of time in milliseconds to allow a connection to
     *            respond before timing out
     */
    public WebResource(URI theURI, String theProxyHost, int theTimeout) {
        this(theURI, theProxyHost, null, theTimeout);
    }

    /**
     * Construct a WebResource for the given URL, going through the optional
     * proxy and port, while timing out if too much time has passed.
     * 
     * @param theURI
     *            the Resource to get via HTTP
     * @param theProxyHost
     *            the proxy host or null
     * @param theProxyPort
     *            the proxy port or null, where null means use the standard port
     */
    public WebResource(URI theURI, String theProxyHost, Integer theProxyPort) {
        this(theURI, theProxyHost, theProxyPort, timeout);
    }

    /**
     * Construct a WebResource for the given URL, going through the optional
     * proxy and port, while timing out if too much time has passed.
     * 
     * @param theURI
     *            the Resource to get via HTTP
     * @param theProxyHost
     *            the proxy host or null
     * @param theProxyPort
     *            the proxy port or null, where null means use the standard port
     * @param theTimeout
     *            the length of time in milliseconds to allow a connection to
     *            respond before timing out
     */
    public WebResource(URI theURI, String theProxyHost, Integer theProxyPort, int theTimeout) {
        uri = theURI;
        client = new DefaultHttpClient();
        HttpParams params = client.getParams();

        // Allowable time between packets
        HttpConnectionParams.setSoTimeout(params, theTimeout);
        // Allowable time to get a connection
        HttpConnectionParams.setConnectionTimeout(params, theTimeout);

        // Configure proxy info if necessary and defined
        if (theProxyHost != null && theProxyHost.length() > 0) {
            // Configure the host and port
            HttpHost proxy = new HttpHost(theProxyHost, theProxyPort == null ? -1 : theProxyPort.intValue());
            ConnRouteParams.setDefaultProxy(params, proxy);
        }
        ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
                client.getConnectionManager().getSchemeRegistry(),
                ProxySelector.getDefault());
                ((AbstractHttpClient) client).setRoutePlanner(routePlanner);
    }

    /**
     * When this WebResource is no longer needed it should be shutdown to return
     * underlying resources back to the OS.
     */
    public void shutdown() {
        client.getConnectionManager().shutdown();
    }

    /**
     * @return the timeout in milliseconds
     */
    public static int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout
     *            the timeout to set in milliseconds
     */
    public static void setTimeout(int timeout) {
        WebResource.timeout = timeout;
    }

    /**
     * Determine the size of this WebResource.
     * <p>
     * Note that the http client may read the entire file to determine this.
     * </p>
     * 
     * @return the size of the file
     */
    public long getSize() {
        HttpRequestBase method = new HttpHead(uri);
        HttpResponse response = null;
        try {
            // Execute the method.
            response = client.execute(method);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                Header header = response.getFirstHeader("Content-Length");
                String value = header.getValue();
                try {
                    return Long.parseLong(value);
                } catch (NumberFormatException ex) {
                    return 0;
                }
            }
            String reason = response.getStatusLine().getReasonPhrase();
            // TRANSLATOR: Common error condition: {0} is a placeholder for the
            // URL of what could not be found.
            Reporter.informUser(this, UserMsg.gettext("Unable to find: {0}", new Object[] {
                reason + ':' + uri.getPath()
            }));
        } catch (IOException e) {
            return 0;
        }
        return 0;
    }

    /**
     * Determine the last modified date of this WebResource.
     * <p>
     * Note that the http client may read the entire file.
     * </p>
     * 
     * @return the last mod date of the file
     */
    @SuppressWarnings("deprecation")
    public long getLastModified() {
        HttpRequestBase method = new HttpHead(uri);
        HttpResponse response = null;
        try {
            // Execute the method.
            response = client.execute(method);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                Header header = response.getFirstHeader("Last-Modified");
                String value = header.getValue();
                try {
                    // This date cannot be readily parsed with DateFormatter
                    return Date.parse(value);
                } catch (Exception ex) {
                    return 0;
                }
            }
            String reason = response.getStatusLine().getReasonPhrase();
            // TRANSLATOR: Common error condition: {0} is a placeholder for the
            // URL of what could not be found.
            Reporter.informUser(this, UserMsg.gettext("Unable to find: {0}", new Object[] {
                reason + ':' + uri.getPath()
            }));
        } catch (IOException e) {
            return new Date().getTime();
        }
        return new Date().getTime();
    }

    /**
     * Copy this WebResource to the destination.
     * 
     * @param dest
     * @throws LucidException
     */
    public void copy(URI dest) throws LucidException {
        InputStream in = null;
        OutputStream out = null;

        HttpRequestBase method = new HttpGet(uri);
        HttpResponse response = null;
        HttpEntity entity = null;
        try {
            // Execute the method.
            response = client.execute(method);
            entity = response.getEntity();
            if (entity != null) {
                in = entity.getContent();

                // Download the index file
                out = NetUtil.getOutputStream(dest);

                byte[] buf = new byte[4096];
                int count = in.read(buf);
                while (-1 != count) {
                    out.write(buf, 0, count);
                    count = in.read(buf);
                }
            } else {
                String reason = response.getStatusLine().getReasonPhrase();
                // TRANSLATOR: Common error condition: {0} is a placeholder for
                // the URL of what could not be found.
                Reporter.informUser(this, UserMsg.gettext("Unable to find: {0}", new Object[] {
                    reason + ':' + uri.getPath()
                }));
            }
        } catch (IOException e) {
            // TRANSLATOR: Common error condition: {0} is a placeholder for the
            // URL of what could not be found.
            throw new LucidException(UserMsg.gettext("Unable to find: {0}", new Object[] {
                uri.toString()
            }), e);
        } finally {
            // Close the streams
            IOUtil.close(in);
            IOUtil.close(out);
        }
    }

    /**
     * Define a 750 ms timeout to get a connection
     */
    private static int timeout = 750;

    private URI uri;
    private HttpClient client;
}