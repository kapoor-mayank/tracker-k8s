//package org.traccar;
//
//import com.squareup.okhttp.*;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.handler.codec.http.HttpRequestDecoder;
//
//import java.io.IOException;
//import java.util.concurrent.CompletableFuture;
//import java.net.InetSocketAddress;
//import java.sql.SQLException;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.LinkedHashSet;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//
//import org.json.JSONObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.traccar.database.StatisticsManager;
//import org.traccar.helper.DateUtil;
//import org.traccar.model.Position;
//
//
//public class MainEventHandler
//        extends ChannelInboundHandlerAdapter {
//    private static final Logger LOGGER = LoggerFactory.getLogger(MainEventHandler.class);
/// /    private static final String TOKEN_URL = "https://dev-api.flocksafety.com/oauth/token";
/// /    private static final String API_URL = "https://api.flocksafety.com/api/v3/geo/subjects";
/// /    private static final String CLIENT_ID = "MTDnMK8cptciglwiCkrSWCkns7qoLEdX";
/// /    private static final String CLIENT_SECRET = "iLVUY_PLT5TEq2zAOjOLsdvsTQZfmr9Yf8DHv3fJvo_nQx2bmEfEsUlyTsehH94Y";
/// /    private static final String AUDIENCE = "com.flocksafety.integrations.dev";
/// /    private static String accessToken = null;
/// /    private static long tokenExpiryTime = 0;
/// /    private static final OkHttpClient client = new OkHttpClient();
//
//    private static final String DEFAULT_LOGGER_ATTRIBUTES = "time,position,speed,course,accuracy,result";
//
//    private final Set<String> connectionlessProtocols = new HashSet<>();
//    private final Set<String> logAttributes = new LinkedHashSet<>();
//
//    public MainEventHandler() {
//        String connectionlessProtocolList = Context.getConfig().getString("status.ignoreOffline");
//        if (connectionlessProtocolList != null) {
//            this.connectionlessProtocols.addAll(Arrays.asList(connectionlessProtocolList.split("[, ]")));
//        }
//        this.logAttributes.addAll(Arrays.asList(
//                Context.getConfig().getString("logger.attributes", "time,position,speed,course,accuracy,result").split("[, ]")));
//    }
//
//
//    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        if (msg instanceof Position) {
//
//            Position position = (Position) msg;
//            try {
//                Context.getDeviceManager().updateLatestPosition(position);
//            } catch (SQLException error) {
//                LOGGER.warn("[{}] Failed to update latest position", ctx.channel().id().asShortText(), error);
//            }
//
//            String uniqueId = Context.getIdentityManager().getById(position.getDeviceId()).getUniqueId();
//
//            StringBuilder builder = new StringBuilder();
//            builder.append(formatDetails(ctx.channel())).append(" ");
//            builder.append("id: ").append(uniqueId);
//            for (String attribute : this.logAttributes) {
//                switch (attribute) {
//                    case "time":
//                        builder.append(", time: ").append(DateUtil.formatDate(position.getFixTime(), false));
//                        continue;
//                    case "position":
//                        builder.append(", lat: ").append(String.format("%.5f", new Object[]{Double.valueOf(position.getLatitude())}));
//                        builder.append(", lon: ").append(String.format("%.5f", new Object[]{Double.valueOf(position.getLongitude())}));
//                        continue;
//                    case "speed":
//                        if (position.getSpeed() > 0.0D) {
//                            builder.append(", speed: ").append(String.format("%.1f", new Object[]{Double.valueOf(position.getSpeed())}));
//                        }
//                        continue;
//                    case "course":
//                        builder.append(", course: ").append(String.format("%.1f", new Object[]{Double.valueOf(position.getCourse())}));
//                        continue;
//                    case "accuracy":
//                        if (position.getAccuracy() > 0.0D) {
//                            builder.append(", accuracy: ").append(String.format("%.1f", new Object[]{Double.valueOf(position.getAccuracy())}));
//                        }
//                        continue;
//                    case "outdated":
//                        if (position.getOutdated()) {
//                            builder.append(", outdated");
//                        }
//                        continue;
//                    case "invalid":
//                        if (!position.getValid()) {
//                            builder.append(", invalid");
//                        }
//                        continue;
//                }
//                Object value = position.getAttributes().get(attribute);
//                if (value != null) {
//                    builder.append(", ").append(attribute).append(": ").append(value);
//                }
//            }
//
////            // Making the HTTP call asynchronously
////            CompletableFuture.runAsync(() -> {
////                OkHttpClient client = new OkHttpClient();
////                MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
////                RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=MTDnMK8cptciglwiCkrSWCkns7qoLEdX&client_secret=iLVUY_PLT5TEq2zAOjOLsdvsTQZfmr9Yf8DHv3fJvo_nQx2bmEfEsUlyTsehH94Y&audience=com.flocksafety.integrations.dev");
////                Request request = new Request.Builder()
////                        .url("https://dev-api.flocksafety.com/oauth/token")
////                        .post(body)
////                        .addHeader("accept", "application/json")
////                        .addHeader("content-type", "application/x-www-form-urlencoded")
////                        .build();
////
////                Response response = null;
////                try {
////                    response = client.newCall(request).execute();
////                    String responseBody = response.body().string();
////                    LOGGER.info("ResponseBody: {}", responseBody);
////                    JSONObject jsonResponse = new JSONObject(responseBody);
////                    LOGGER.info("JSONResponseBody: {}", jsonResponse);
////                    if (response.isSuccessful()) {
////                        String accessToken = jsonResponse.optString("access_token", "");
////
////                        if (!accessToken.isEmpty()) {
////                            LOGGER.info("Successfully extracted access_token: {}", accessToken);
////                        } else {
////                            LOGGER.warn("access_token not found in response");
////                        }
////                    }
////                } catch (IOException e) {
////                    LOGGER.error("Error in API Call: ", e);
////                }
////            });
////            CompletableFuture<Void> voidCompletableFuture = sendApiRequestAsync(position.getLatitude(), position.getLongitude());
//
//
//            LOGGER.info(builder.toString());
//
//            ((StatisticsManager) Main.getInjector().getInstance(StatisticsManager.class)).registerMessageStored(position.getDeviceId());
//        }
//    }
//
//    private static String formatDetails(Channel channel) {
//        String remote = "-";
//
//        if (channel.remoteAddress() instanceof InetSocketAddress) {
//            InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
//            remote = String.format("%s:%d", new Object[]{socketAddress.getHostString(), Integer.valueOf(socketAddress.getPort())});
//        }
//
//        return String.format("[%s: %d] %s -", new Object[]{channel.id().asShortText(), Integer.valueOf(((InetSocketAddress) channel.localAddress()).getPort()), remote});
//    }
//
//
//    public void channelActive(ChannelHandlerContext ctx) {
//        if (!(ctx.channel() instanceof io.netty.channel.socket.DatagramChannel)) {
//            LOGGER.info(formatDetails(ctx.channel()) + "  " +ctx.channel().getClass().getSimpleName() + " connected");
//        }
//    }
//
//
//    public void channelInactive(ChannelHandlerContext ctx) {
//        LOGGER.info(formatDetails(ctx.channel()) + " disconnected");
//        closeChannel(ctx.channel());
//
//        if (BasePipelineFactory.getHandler(ctx.pipeline(), HttpRequestDecoder.class) == null &&
//                !this.connectionlessProtocols.contains(((BaseProtocolDecoder) ctx.pipeline().get(BaseProtocolDecoder.class)).getProtocolName())) {
//            Context.getConnectionManager().removeActiveDevice(ctx.channel());
//        }
//    }
//
//
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        while (cause.getCause() != null && cause.getCause() != cause) {
//            cause = cause.getCause();
//        }
//        LOGGER.warn(formatDetails(ctx.channel()) + " error", cause);
//        closeChannel(ctx.channel());
//    }
//
//
//    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
//        if (evt instanceof io.netty.handler.timeout.IdleStateEvent) {
//            LOGGER.info(formatDetails(ctx.channel()) + " timed out");
//            closeChannel(ctx.channel());
//        }
//    }
//
//    private void closeChannel(Channel channel) {
//        if (!(channel instanceof io.netty.channel.socket.DatagramChannel))
//            channel.close();
//    }
//
//
////    private static synchronized void fetchAccessToken() {
////        if (System.currentTimeMillis() < tokenExpiryTime) {
////            LOGGER.info("Using cached token");
////            return;
////        }
////        LOGGER.info("Fetching new access token");
////
////        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
////        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&audience=" + AUDIENCE);
////        Request request = new Request.Builder()
////                .url(TOKEN_URL)
////                .post(body)
////                .addHeader("accept", "application/json")
////                .addHeader("content-type", "application/x-www-form-urlencoded")
////                .build();
////
////        Response response = null;
////        try {
////            response = client.newCall(request).execute();
////            if (response.isSuccessful() && response.body() != null) {
////                String responseBody = response.body().string();
////                JSONObject jsonResponse = new JSONObject(responseBody);
////                accessToken = jsonResponse.optString("access_token", "");
////                int expiresIn = jsonResponse.optInt("expires_in", 3600);
////                tokenExpiryTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiresIn);
////                LOGGER.info("New token fetched and expires in: {} seconds", expiresIn);
////            } else {
////                LOGGER.error("Failed to fetch access token, response: {}", response.body() != null ? response.body().string() : "null");
////            }
////        }catch (Exception e) {
////            LOGGER.error("Error in fetching token: ", e);
////        }
////
////
////    }
////
////    public static CompletableFuture<Void> sendApiRequestAsync(double latitude, double longitude) {
////        return CompletableFuture.runAsync(() -> {
////            fetchAccessToken();
////            if(accessToken == null || accessToken.isEmpty()) {
////                LOGGER.error("Access Token Unavailable");
////                return;
////            }
////            LOGGER.info("Got Access token in sendAPI: {}, with lat: {}, long: {}", accessToken, latitude, longitude);
////        });
////        return CompletableFuture.runAsync(() -> {
////            fetchAccessToken();
////            if (accessToken == null || accessToken.isEmpty()) {
////                LOGGER.error("Access token is unavailable, cannot proceed with request");
////                return;
////            }
////
////            MediaType mediaType = MediaType.parse("application/json");
////            String jsonBody = String.format("{\"subjectType\":\"vehicle\",\"externalId\":\"generated UUID\",\"deviceStatus\":null,\"isRecording\":false,\"isLawEnforcement\":false,\"latitude\":%f,\"longitude\":%f}", latitude, longitude);
////            RequestBody body = RequestBody.create(mediaType, jsonBody);
////            Request request = new Request.Builder()
////                    .url(API_URL)
////                    .put(body)
////                    .addHeader("accept", "application/json")
////                    .addHeader("content-type", "application/json")
////                    .addHeader("authorization", "Bearer " + accessToken)
////                    .build();
////
////            try (Response response = client.newCall(request).execute()) {
////                if (response.isSuccessful() && response.body() != null) {
////                    LOGGER.info("API Response: {}", response.body().string());
////                } else {
////                    LOGGER.error("API Request failed: {}", response.body() != null ? response.body().string() : "null");
////                }
////            } catch (IOException e) {
////                LOGGER.error("Error in API Call", e);
////            }
////        });
////    }
//}
//
//
///* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\MainEventHandler.class
// * Java compiler version: 8 (52.0)
// * JD-Core Version:       1.1.3
// */
//**********************************************************************************************************************************************************************


package org.traccar;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequestDecoder;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.traccar.database.StatisticsManager;
import org.traccar.helper.DateUtil;
import org.traccar.model.Position;


public class MainEventHandler
        extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainEventHandler.class);

    private static final String DEFAULT_LOGGER_ATTRIBUTES = "time,position,speed,course,accuracy,result";

    private final Set<String> connectionlessProtocols = new HashSet<>();
    private final Set<String> logAttributes = new LinkedHashSet<>();

    public MainEventHandler() {
        String connectionlessProtocolList = Context.getConfig().getString("status.ignoreOffline");
        if (connectionlessProtocolList != null) {
            this.connectionlessProtocols.addAll(Arrays.asList(connectionlessProtocolList.split("[, ]")));
        }
        this.logAttributes.addAll(Arrays.asList(
                Context.getConfig().getString("logger.attributes", "time,position,speed,course,accuracy,result").split("[, ]")));
    }


    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Position) {

            Position position = (Position) msg;
            try {
                Context.getDeviceManager().updateLatestPosition(position);
            } catch (SQLException error) {
                LOGGER.warn("[{}] Failed to update latest position", ctx.channel().id().asShortText(), error);
            }

            String uniqueId = Context.getIdentityManager().getById(position.getDeviceId()).getUniqueId();

            StringBuilder builder = new StringBuilder();
            builder.append(formatDetails(ctx.channel())).append(" ");
            builder.append("id: ").append(uniqueId);
            for (String attribute : this.logAttributes) {
                switch (attribute) {
                    case "time":
                        builder.append(", time: ").append(DateUtil.formatDate(position.getFixTime(), false));
                        continue;
                    case "position":
                        builder.append(", lat: ").append(String.format("%.5f", new Object[]{Double.valueOf(position.getLatitude())}));
                        builder.append(", lon: ").append(String.format("%.5f", new Object[]{Double.valueOf(position.getLongitude())}));
                        continue;
                    case "speed":
                        if (position.getSpeed() > 0.0D) {
                            builder.append(", speed: ").append(String.format("%.1f", new Object[]{Double.valueOf(position.getSpeed())}));
                        }
                        continue;
                    case "course":
                        builder.append(", course: ").append(String.format("%.1f", new Object[]{Double.valueOf(position.getCourse())}));
                        continue;
                    case "accuracy":
                        if (position.getAccuracy() > 0.0D) {
                            builder.append(", accuracy: ").append(String.format("%.1f", new Object[]{Double.valueOf(position.getAccuracy())}));
                        }
                        continue;
                    case "outdated":
                        if (position.getOutdated()) {
                            builder.append(", outdated");
                        }
                        continue;
                    case "invalid":
                        if (!position.getValid()) {
                            builder.append(", invalid");
                        }
                        continue;
                }
                Object value = position.getAttributes().get(attribute);
                if (value != null) {
                    builder.append(", ").append(attribute).append(": ").append(value);
                }
            }


            LOGGER.info(builder.toString());

            ((StatisticsManager) Main.getInjector().getInstance(StatisticsManager.class)).registerMessageStored(position.getDeviceId());
        }
    }

    private static String formatDetails(Channel channel) {
        String remote = "-";

        if (channel.remoteAddress() instanceof InetSocketAddress) {
            InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
            remote = String.format("%s:%d", new Object[]{socketAddress.getHostString(), Integer.valueOf(socketAddress.getPort())});
        }

        return String.format("[%s: %d] %s -", new Object[]{channel.id().asShortText(), Integer.valueOf(((InetSocketAddress) channel.localAddress()).getPort()), remote});
    }


    public void channelActive(ChannelHandlerContext ctx) {
        if (!(ctx.channel() instanceof io.netty.channel.socket.DatagramChannel)) {
            LOGGER.info(formatDetails(ctx.channel()) + " connected");
        }
    }


    public void channelInactive(ChannelHandlerContext ctx) {
        LOGGER.info(formatDetails(ctx.channel()) + " disconnected");
        closeChannel(ctx.channel());

        if (BasePipelineFactory.getHandler(ctx.pipeline(), HttpRequestDecoder.class) == null &&
                !this.connectionlessProtocols.contains(((BaseProtocolDecoder) ctx.pipeline().get(BaseProtocolDecoder.class)).getProtocolName())) {
            Context.getConnectionManager().removeActiveDevice(ctx.channel());
        }
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        LOGGER.warn(formatDetails(ctx.channel()) + " error", cause);
        closeChannel(ctx.channel());
    }


    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof io.netty.handler.timeout.IdleStateEvent) {
            LOGGER.info(formatDetails(ctx.channel()) + " timed out");
            closeChannel(ctx.channel());
        }
    }

    private void closeChannel(Channel channel) {
        if (!(channel instanceof io.netty.channel.socket.DatagramChannel))
            channel.close();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\MainEventHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */