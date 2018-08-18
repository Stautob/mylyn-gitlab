package org.gitlab.api.http;

import static org.gitlab.api.http.Method.GET;
import static org.gitlab.api.http.Method.POST;
import static org.gitlab.api.http.Method.PUT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.gitlab.api.AuthMethod;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.GitlabAPIException;
import org.gitlab.api.TokenType;


/**
 * Gitlab HTTP Requestor
 * Responsible for handling HTTP requests to the Gitlab API
 *
 * @author &#064;timols (Tim O)
 */
public class GitlabHTTPRequestor {

   private static final Pattern PAGE_PATTERN = Pattern.compile("([&|?])page=(\\d+)");

   private final GitlabAPI root;

   private Method              method      = GET;            // Default to GET requests
   private Map<String, Object> data        = new HashMap<>();
   private Map<String, File>   attachments = new HashMap<>();

   private String     apiToken;
   private TokenType  tokenType;
   private AuthMethod authMethod;

   public GitlabHTTPRequestor(GitlabAPI root) {
      this.root = root;
   }

   /**
    * Sets authentication data for the request.
    * Has a fluent api for method chaining.
    *
    * @param token
    *        The token value
    * @param type
    *        The type of the token
    * @param method
    *        The authentication method
    * @return this
    */
   public GitlabHTTPRequestor authenticate(String token, TokenType type, AuthMethod method) {
      this.apiToken = token;
      this.tokenType = type;
      this.authMethod = method;
      return this;
   }

   /**
    * Sets the HTTP Request method for the request.
    * Has a fluent api for method chaining.
    *
    * @param method
    *        The HTTP method
    * @return this
    */
   public GitlabHTTPRequestor method(Method method) {
      this.method = method;
      return this;
   }

   /**
    * Sets the HTTP Form Post parameters for the request
    * Has a fluent api for method chaining
    *
    * @param key
    *        Form parameter Key
    * @param value
    *        Form parameter Value
    * @return this
    */
   public GitlabHTTPRequestor with(String key, Object value) {
      if (value != null && key != null) {
         data.put(key, value);
      }
      return this;
   }

   /**
    * Sets the HTTP Form Post parameters for the request
    * Has a fluent api for method chaining
    *
    * @param key
    *        Form parameter Key
    * @param file
    *        File data
    * @return this
    */
   public GitlabHTTPRequestor withAttachment(String key, File file) {
      if (file != null && key != null) {
         attachments.put(key, file);
      }
      return this;
   }

   public <T> T to(String tailAPIUrl, T instance) throws IOException {
      return to(tailAPIUrl, null, instance);
   }

   public <T> T to(String tailAPIUrl, Class<T> type) throws IOException {
      return to(tailAPIUrl, type, null);
   }

   /**
    * Opens the HTTP(S) connection, submits any data and parses the response.
    * Will throw an error
    *
    * @param <T>
    *        The return type of the method
    * @param tailAPIUrl
    *        The url to open a connection to (after the host and namespace)
    * @param type
    *        The type of the response to be deserialized from
    * @param instance
    *        The instance to update from the response
    * @return An object of type T
    * @throws java.io.IOException
    *         on gitlab api error
    */
   public <T> T to(String tailAPIUrl, Class<T> type, T instance) throws IOException {
      HttpURLConnection connection = null;
      try {
         connection = setupConnection(root.getAPIUrl(tailAPIUrl));
         if (hasAttachments()) {
            submitAttachments(connection);
         } else if (hasOutput()) {
            submitData(connection);
         } else if (PUT.equals(method)) {
            // PUT requires Content-Length: 0 even when there is no body (eg: API for protecting a branch)
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(0);
         }

         try {
            return parse(connection, type, instance);
         } catch (IOException e) {
            handleAPIError(e, connection);
         }

         return null;
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   public <T> List<T> getAll(final String tailUrl, final Class<T[]> type) {
      List<T> results = new ArrayList<>();
      Iterator<T[]> iterator = asIterator(tailUrl, type);

      while (iterator.hasNext()) {
         T[] requests = iterator.next();

         if (requests.length > 0) {
            results.addAll(Arrays.asList(requests));
         }
      }
      return results;
   }

   public <T> Iterator<T> asIterator(final String tailApiUrl, final Class<T> type) {
      method(GET); // Ensure we only use iterators for GET requests

      // Ensure that we don't submit any data and alert the user
      if (!data.isEmpty()) { throw new IllegalStateException(); }

      return new Iterator<T>() {

         T   next;
         URL url;

         {
            try {
               url = root.getAPIUrl(tailApiUrl);
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
         }

         @Override
         public boolean hasNext() {
            fetch();
            if (next != null && next.getClass().isArray()) {
               Object[] arr = (Object[]) next;
               return arr.length != 0;
            } else {
               return next != null;
            }
         }

         @Override
         public T next() {
            fetch();
            T record = next;

            if (record == null) { throw new NoSuchElementException(); }

            next = null;
            return record;
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException();
         }

         private void fetch() {
            if (next != null) { return; }

            if (url == null) { return; }

            try {
               HttpURLConnection connection = setupConnection(url);
               try {
                  next = parse(connection, type, null);
                  assert next != null;
                  findNextUrl();
               } catch (IOException e) {
                  handleAPIError(e, connection);
               }
            } catch (IOException e) {
               throw new RuntimeException(e);
            }
         }

         private void findNextUrl() throws MalformedURLException {
            String url = this.url.toString();

            this.url = null;
            /*
             * Increment the page number for the url if a "page" property exists,
             * otherwise, add the page property and increment it.
             * The Gitlab API is not a compliant hypermedia REST api, so we use
             * a naive implementation.
             */
            Matcher matcher = PAGE_PATTERN.matcher(url);

            if (matcher.find()) {
               Integer page = Integer.parseInt(matcher.group(2)) + 1;
               this.url = new URL(matcher.replaceAll(matcher.group(1) + "page=" + page));
            } else {
               // Since the page query was not present, its safe to assume that we just
               // currently used the first page, so we can default to page 2
               this.url = new URL(url + (url.indexOf('?') > 0 ? '&' : '?') + "page=2");
            }
         }
      };
   }

   private void submitAttachments(HttpURLConnection connection) throws IOException {
      String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
      connection.setDoOutput(true);
      connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
      String charset = "UTF-8";
      String CRLF = "\r\n"; // Line separator required by multipart/form-data.
      OutputStream output = connection.getOutputStream();
      try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true)) {
         for (Map.Entry<String, Object> paramEntry : data.entrySet()) {
            String paramName = paramEntry.getKey();
            String param = GitlabAPI.MAPPER.writeValueAsString(paramEntry.getValue());
            writer.append("--").append(boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"").append(paramName).append("\"").append(CRLF);
            writer.append("Content-Type: text/plain; charset=").append(charset).append(CRLF);
            writer.append(CRLF).append(param).append(CRLF).flush();
         }
         for (Map.Entry<String, File> attachMentEntry : attachments.entrySet()) {
            File binaryFile = attachMentEntry.getValue();
            writer.append("--").append(boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; name=\"").append(attachMentEntry.getKey()).append("\"; filename=\"").append(binaryFile
                  .getName()).append("\"").append(CRLF);
            writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF).flush();
            try (Reader fileReader = new FileReader(binaryFile)) {
               IOUtils.copy(fileReader, output);
            }
            output.flush(); // Important before continuing with writer!
            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
         }
         writer.append("--").append(boundary).append("--").append(CRLF).flush();
      }
   }

   private void submitData(HttpURLConnection connection) throws IOException {
      connection.setDoOutput(true);
      connection.setRequestProperty("Content-Type", "application/json");
      GitlabAPI.MAPPER.writeValue(connection.getOutputStream(), data);
   }

   private boolean hasAttachments() {
      return !attachments.isEmpty();
   }

   private boolean hasOutput() {
      return method.equals(POST) || method.equals(PUT) && !data.isEmpty();
   }

   private HttpURLConnection setupConnection(URL url) throws IOException {
      if (root.isIgnoreCertificateErrors()) {
         ignoreCertificateErrors();
      }

      if (apiToken != null && authMethod == AuthMethod.URL_PARAMETER) {
         String urlWithAuth = url.toString();
         urlWithAuth = urlWithAuth + (urlWithAuth.indexOf('?') > 0 ? '&' : '?') + tokenType.getTokenParamName() + "=" + apiToken;
         url = new URL(urlWithAuth);
      }

      HttpURLConnection connection = root.getProxy() != null ? (HttpURLConnection) url.openConnection(root.getProxy()) : (HttpURLConnection) url
            .openConnection();
      if (apiToken != null && authMethod == AuthMethod.HEADER) {
         connection.setRequestProperty(tokenType.getTokenHeaderName(), String.format(tokenType.getTokenHeaderFormat(), apiToken));
      }

      connection.setReadTimeout(root.getResponseReadTimeout());
      connection.setConnectTimeout(root.getConnectionTimeout());

      try {
         connection.setRequestMethod(method.name());
      } catch (ProtocolException e) {
         // Hack in case the API uses a non-standard HTTP verb
         try {
            Field methodField = connection.getClass().getDeclaredField("method");
            methodField.setAccessible(true);
            methodField.set(connection, method.name());
         } catch (Exception x) {
            throw new IOException("Failed to set the custom verb", x);
         }
      }
      connection.setRequestProperty("User-Agent", root.getUserAgent());
      connection.setRequestProperty("Accept-Encoding", "gzip");
      return connection;
   }

   private <T> T parse(HttpURLConnection connection, Class<T> type, T instance) throws IOException {
      InputStreamReader reader = null;
      try {
         if (byte[].class == type) { return type.cast(IOUtils.toByteArray(wrapStream(connection, connection.getInputStream()))); }
         reader = new InputStreamReader(wrapStream(connection, connection.getInputStream()), "UTF-8");
         String json = IOUtils.toString(reader);
         if (type != null && type == String.class) { return type.cast(json); }
         if (type != null && type != Void.class) {
            return GitlabAPI.MAPPER.readValue(json, type);
         } else if (instance != null) {
            return GitlabAPI.MAPPER.readerForUpdating(instance).readValue(json);
         } else {
            return null;
         }
      } catch (SSLHandshakeException e) {
         throw new SSLException("You can disable certificate checking by setting ignoreCertificateErrors " + "on GitlabHTTPRequestor.", e);
      } finally {
         IOUtils.closeQuietly(reader);
      }
   }

   private InputStream wrapStream(HttpURLConnection connection, InputStream inputStream) throws IOException {
      String encoding = connection.getContentEncoding();

      if (encoding == null || inputStream == null) {
         return inputStream;
      } else if (encoding.equals("gzip")) {
         return new GZIPInputStream(inputStream);
      } else {
         throw new UnsupportedOperationException("Unexpected Content-Encoding: " + encoding);
      }
   }

   private void handleAPIError(IOException e, HttpURLConnection connection) throws IOException {
      if (e instanceof FileNotFoundException || // pass through 404 Not Found to allow the caller to handle it intelligently
          e instanceof SocketTimeoutException || e instanceof ConnectException) { throw e; }

      InputStream es = wrapStream(connection, connection.getErrorStream());
      try {
         String error = null;
         if (es != null) {
            error = IOUtils.toString(es, "UTF-8");
         }
         throw new GitlabAPIException(error, connection.getResponseCode(), e);
      } finally {
         IOUtils.closeQuietly(es);
      }
   }

   private void ignoreCertificateErrors() {
      TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

         @Override
         public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
         }

         @Override
         public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}

         @Override
         public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
      } };
      // Added per https://github.com/timols/java-gitlab-api/issues/44
      HostnameVerifier nullVerifier = (hostname, session) -> true;

      try {
         SSLContext sc = SSLContext.getInstance("SSL");
         sc.init(null, trustAllCerts, new java.security.SecureRandom());
         HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
         // Added per https://github.com/timols/java-gitlab-api/issues/44
         HttpsURLConnection.setDefaultHostnameVerifier(nullVerifier);
      } catch (Exception ignore) {}
   }
}
