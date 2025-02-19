/**
 * The MIT License
 *
 * Copyright for portions of unirest-java are held by Kong Inc (c) 2013.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package kong.unirest.core;

import kong.unirest.core.Client;
import kong.unirest.core.Config;
import kong.unirest.core.Proxy;
import kong.unirest.core.UnirestConfigException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class ConfigTest {

    @InjectMocks
    private Config config;

    @Test
    void shouldKeepConnectionTimeOutDefault(){
        assertEquals(Config.DEFAULT_CONNECT_TIMEOUT, config.getConnectionTimeout());
    }

    @Test
    void willRebuildIfEmpty() {
        assertSame(config.getClient(), config.getClient());
    }

    @Test
    void settingTTl() {
        assertEquals(-1, config.getTTL());

        assertEquals(42, config.connectionTTL(42, TimeUnit.MILLISECONDS).getTTL());
        assertEquals(2520000, config.connectionTTL(42, TimeUnit.MINUTES).getTTL());

        assertEquals(43, config.connectionTTL(Duration.ofMillis(43)).getTTL());
        assertEquals(2580000, config.connectionTTL(Duration.ofMinutes(43)).getTTL());
    }

    @Test
    void provideYourOwnClientBuilder() {
        Client cli = mock(Client.class);

        config.httpClient(c -> cli);

        assertSame(cli, config.getClient());
    }

    @Test
    void canDisableGZipencoding() {
        assertTrue(config.isRequestCompressionOn());
        config.requestCompression(false);
        assertFalse(config.isRequestCompressionOn());

    }

    @Test
    void canDisableAuthRetry() {
        assertTrue(config.isAutomaticRetries());
        config.automaticRetries(false);
        assertFalse(config.isAutomaticRetries());
    }

    @Test
    void canSetProxyViaSetter() {
        config.proxy(new Proxy("localhost", 8080, "ryan", "password"));
        assertProxy("localhost", 8080, "ryan", "password");

        config.proxy("local2", 8888);
        assertProxy("local2", 8888, null, null);

        config.proxy("local3", 7777, "barb", "12345");
        assertProxy("local3", 7777, "barb", "12345");
    }

    @Test
    void cannotConfigASslContextIfAKeystoreIsPresent() {
        KeyStore store = mock(KeyStore.class);
        SSLContext context = mock(SSLContext.class);

        config.clientCertificateStore(store, "foo");

        UnirestConfigException ex = assertThrows(UnirestConfigException.class, () -> config.sslContext(context));
        assertEquals("You may only configure a SSLContext OR a Keystore, but not both", ex.getMessage());
    }

    @Test
    void cannotConfigAKeyStoreIfASSLContextIsPresent() {
        KeyStore store = mock(KeyStore.class);
        SSLContext context = mock(SSLContext.class);

        config.sslContext(context);

        UnirestConfigException ex1 = assertThrows(UnirestConfigException.class, () -> config.clientCertificateStore(store, "foo"));
        assertEquals("You may only configure a SSLContext OR a Keystore, but not both", ex1.getMessage());

        UnirestConfigException ex = assertThrows(UnirestConfigException.class, () -> config.clientCertificateStore("/a/path/file.pk12", "foo"));
        assertEquals("You may only configure a SSLContext OR a Keystore, but not both", ex.getMessage());
    }

    @Test
    void isRunningIfStandardClientIsRunning() {
        assertFalse(config.isRunning());
        config.getClient();
        assertTrue(config.isRunning());
    }

    @Test
    void isRunningIfAsyncClientIsRunning() {
        assertFalse(config.isRunning());
        config.getClient();
        assertTrue(config.isRunning());
    }

    private void assertProxy(String host, Integer port, String username, String password) {
        assertEquals(host, config.getProxy().getHost());
        assertEquals(port, config.getProxy().getPort());
        assertEquals(username, config.getProxy().getUsername());
        assertEquals(password, config.getProxy().getPassword());
    }
}