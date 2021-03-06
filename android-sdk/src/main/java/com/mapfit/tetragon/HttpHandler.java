package com.mapfit.tetragon;

import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;

import static com.mapfit.android.utils.CommonUtils.isNetworkAvailable;

/**
 * FIXME: Rename to UrlHandler.
 * {@code HttpHandler} is a class for customizing HTTP requests for map resources, it can be
 * extended to override the request or caching behavior.
 */
public class HttpHandler {

    protected OkHttpClient okClient;
    protected CachePolicy cachePolicy;

    /**
     * Enables TLS v1.2 when creating SSLSockets.
     * <p/>
     * For some reason, android supports TLS v1.2 from API 16, but enables it by
     * default only from API 20.
     *
     * @link https://developer.android.com/reference/javax/net/ssl/SSLSocket.html
     * @see SSLSocketFactory
     */
    private class Tls12SocketFactory extends SSLSocketFactory {
        private final String[] TLS_V12_ONLY = {"TLSv1", "TLSv1.1", "TLSv1.2"};

        final SSLSocketFactory delegate;

        public Tls12SocketFactory(final SSLSocketFactory base) {
            this.delegate = base;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(final Socket s, final String host, final int port, final boolean autoClose) throws IOException {
            return patch(delegate.createSocket(s, host, port, autoClose));
        }

        @Override
        public Socket createSocket(final String host, final int port) throws IOException, UnknownHostException {
            return patch(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(final String host, final int port, final InetAddress localHost,
                                   final int localPort) throws IOException, UnknownHostException {
            return patch(delegate.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return patch(delegate.createSocket(host, port));
        }

        @Override
        public Socket createSocket(final InetAddress address, final int port, final InetAddress localAddress,
                                   final int localPort) throws IOException {
            return patch(delegate.createSocket(address, port, localAddress, localPort));
        }

        private Socket patch(final Socket s) {
            if (s instanceof SSLSocket) {
                ((SSLSocket) s).setEnabledProtocols(TLS_V12_ONLY);
            }
            return s;
        }
    }

    /**
     * Construct an {@code HttpHandler} with default options.
     */
    public HttpHandler() {
        this(null, 0, null);
    }

    /**
     * Construct an {@code HttpHandler} with cache.
     * Cache map data in a directory with a specified size limit
     *
     * @param directory Directory in which map data will be cached
     * @param maxSize   Maximum size of data to cache, in bytes
     */
    public HttpHandler(final File directory, final long maxSize) {
        this(directory, maxSize, null);
    }

    /**
     * Construct an {@code HttpHandler} with cache.
     * Cache map data in a directory with a specified size limit
     *
     * @param directory Directory in which map data will be cached
     * @param maxSize   Maximum size of data to cache, in bytes
     * @param policy    Cache policy to apply on requests
     */
    public HttpHandler(final File directory, final long maxSize, final CachePolicy policy) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .addInterceptor(getOfflineCacheInterceptor())
                .addNetworkInterceptor(getCacheInterceptor())
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS);

        if (directory != null && maxSize > 0) {
            builder.cache(new Cache(directory, maxSize));
        }

        // Use specified policy or construct default if null.
        cachePolicy = policy;
        if (cachePolicy == null) {
            cachePolicy = new CachePolicy() {
                @Override
                public CacheControl apply(final HttpUrl url) {
                    return null;
                }
            };
        }

        if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
            try {
                final SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, null, null);
                builder.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));

                final ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2)
                        .build();

                final List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);

                builder.connectionSpecs(specs);
            } catch (final Exception exc) {
                android.util.Log.e("Tangram", "Error while setting TLS 1.2", exc);
            }
        }

        okClient = builder.build();
    }

    /**
     * Returns network interceptor to request caching to be 7 days.
     *
     * @return interceptor
     */
    private static Interceptor getCacheInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());

                CacheControl cacheControl = new CacheControl.Builder()
                        .maxAge(7, TimeUnit.DAYS)
                        .build();

                return response.newBuilder()
                        .header("Cache-control", cacheControl.toString())
                        .build();
            }
        };
    }

    /**
     * Returns interceptor to use cached responses when the device is offline.
     *
     * @return interceptor
     */
    private static Interceptor getOfflineCacheInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                if (isNetworkAvailable()) {

                    /* overriding the cache control */
                    CacheControl cacheControl = new CacheControl.Builder()
                            .maxStale(7, TimeUnit.DAYS)
                            .build();
                    request = request.newBuilder().header("Cache-control", cacheControl.toString()).build();

                } else {
                    request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
                }

                return chain.proceed(request);
            }
        };
    }

    /**
     * Begin an HTTP request
     *
     * @param url           URL for the requested resource
     * @param cb            Callback for handling request result
     * @param requestHandle the identifier for the request
     */
    public void onRequest(final String url, final Callback cb, final long requestHandle) {
        final HttpUrl httpUrl = HttpUrl.parse(url);
        final Request.Builder builder = new Request.Builder().url(httpUrl).tag(requestHandle);

        final CacheControl cacheControl = cachePolicy.apply(httpUrl);
        if (cacheControl != null) {
            builder.cacheControl(cacheControl);
        }
        final Request request = builder.build();
        final Call call = okClient.newCall(request);

        call.enqueue(cb);
    }

    /**
     * Cancel an HTTP request
     *
     * @param requestHandle the identifier for the request to be cancelled
     */
    public void onCancel(final long requestHandle) {
        // check and cancel running call
        for (final Call runningCall : okClient.dispatcher().runningCalls()) {
            if (runningCall.request().tag().equals(requestHandle)) {
                runningCall.cancel();
            }
        }

        // check and cancel queued call
        for (final Call queuedCall : okClient.dispatcher().queuedCalls()) {
            if (queuedCall.request().tag().equals(requestHandle)) {
                queuedCall.cancel();
            }
        }
    }

}