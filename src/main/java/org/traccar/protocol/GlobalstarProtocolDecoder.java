/*     */ package org.traccar.protocol;
/*     */ 
/*     */ import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
/*     */ import io.netty.buffer.ByteBuf;
/*     */ import io.netty.buffer.ByteBufOutputStream;
/*     */ import io.netty.buffer.Unpooled;
/*     */ import io.netty.channel.Channel;
/*     */ import io.netty.handler.codec.http.DefaultFullHttpResponse;
/*     */ import io.netty.handler.codec.http.FullHttpRequest;
/*     */ import io.netty.handler.codec.http.HttpHeaderNames;
/*     */ import io.netty.handler.codec.http.HttpResponseStatus;
/*     */ import io.netty.handler.codec.http.HttpVersion;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.net.SocketAddress;
/*     */ import java.util.Date;
/*     */ import java.util.LinkedList;
/*     */ import java.util.List;
/*     */ import javax.xml.parsers.DocumentBuilder;
/*     */ import javax.xml.parsers.DocumentBuilderFactory;
/*     */ import javax.xml.parsers.ParserConfigurationException;
/*     */ import javax.xml.transform.Transformer;
/*     */ import javax.xml.transform.TransformerException;
/*     */ import javax.xml.transform.TransformerFactory;
/*     */ import javax.xml.transform.dom.DOMSource;
/*     */ import javax.xml.transform.stream.StreamResult;
/*     */ import javax.xml.xpath.XPath;
/*     */ import javax.xml.xpath.XPathConstants;
/*     */ import javax.xml.xpath.XPathExpression;
/*     */ import javax.xml.xpath.XPathFactory;
/*     */ import org.traccar.BaseHttpProtocolDecoder;
/*     */ import org.traccar.DeviceSession;
/*     */ import org.traccar.NetworkMessage;
/*     */ import org.traccar.Protocol;
/*     */ import org.traccar.helper.BitUtil;
/*     */ import org.traccar.helper.DataConverter;
/*     */ import org.traccar.helper.UnitsConverter;
/*     */ import org.traccar.model.Position;
/*     */ import org.w3c.dom.Document;
/*     */ import org.w3c.dom.Element;
/*     */ import org.w3c.dom.Node;
/*     */ import org.w3c.dom.NodeList;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class GlobalstarProtocolDecoder
/*     */   extends BaseHttpProtocolDecoder
/*     */ {
/*     */   private final DocumentBuilder documentBuilder;
/*     */   private final XPath xPath;
/*     */   private final XPathExpression messageExpression;
/*     */   
/*     */   public GlobalstarProtocolDecoder(Protocol protocol) {
/*  67 */     super(protocol);
/*     */     try {
/*  69 */       DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
/*  70 */       builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
/*  71 */       builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
/*  72 */       builderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
/*  73 */       builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
/*  74 */       builderFactory.setXIncludeAware(false);
/*  75 */       builderFactory.setExpandEntityReferences(false);
/*  76 */       this.documentBuilder = builderFactory.newDocumentBuilder();
/*  77 */       this.xPath = XPathFactory.newInstance().newXPath();
/*  78 */       this.messageExpression = this.xPath.compile("//stuMessages/stuMessage");
/*  79 */     } catch (ParserConfigurationException|javax.xml.xpath.XPathExpressionException e) {
/*  80 */       throw new RuntimeException(e);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void sendResponse(Channel channel) throws TransformerException {
/*  86 */     Document document = this.documentBuilder.newDocument();
/*  87 */     Element rootElement = document.createElement("stuResponseMsg");
/*  88 */     rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
/*  89 */     rootElement.setAttribute("xsi:noNamespaceSchemaLocation", "http://cody.glpconnect.com/XSD/StuResponse_Rev1_0.xsd");
/*     */     
/*  91 */     rootElement.setAttribute("deliveryTimeStamp", "25/08/2009 21:00:00 GMT");
/*  92 */     rootElement.setAttribute("messageID", "8675309");
/*  93 */     rootElement.setAttribute("correlationID", "56bdca48088610048fddba385e1cd5b8");
/*  94 */     document.appendChild(rootElement);
/*     */     
/*  96 */     Element state = document.createElement("state");
/*  97 */     state.appendChild(document.createTextNode("pass"));
/*  98 */     rootElement.appendChild(state);
/*     */     
/* 100 */     Element stateMessage = document.createElement("stateMessage");
/* 101 */     stateMessage.appendChild(document.createTextNode("Store OK"));
/* 102 */     rootElement.appendChild(stateMessage);
/*     */     
/* 104 */     Transformer transformer = TransformerFactory.newInstance().newTransformer();
/* 105 */     ByteBuf content = Unpooled.buffer();
/* 106 */     transformer.transform(new DOMSource(document), new StreamResult((OutputStream)new ByteBufOutputStream(content)));
/*     */     
/* 108 */     if (channel != null) {
/* 109 */       DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
/*     */       
/* 111 */       defaultFullHttpResponse.headers()
/* 112 */         .add((CharSequence)HttpHeaderNames.CONTENT_LENGTH, Integer.valueOf(content.readableBytes()))
/* 113 */         .add((CharSequence)HttpHeaderNames.CONTENT_TYPE, "text/xml");
/* 114 */       channel.writeAndFlush(new NetworkMessage(defaultFullHttpResponse, channel.remoteAddress()));
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected Object decode(Channel channel, SocketAddress remoteAddress, Object msg) throws Exception {
/* 122 */     FullHttpRequest request = (FullHttpRequest)msg;
/*     */     
/* 124 */     Document document = this.documentBuilder.parse((InputStream)new ByteBufferBackedInputStream(request.content().nioBuffer()));
/* 125 */     NodeList nodes = (NodeList)this.messageExpression.evaluate(document, XPathConstants.NODESET);
/*     */     
/* 127 */     List<Position> positions = new LinkedList<>();
/*     */     
/* 129 */     for (int i = 0; i < nodes.getLength(); i++) {
/* 130 */       Node node = nodes.item(i);
/* 131 */       DeviceSession deviceSession = getDeviceSession(channel, remoteAddress, new String[] { this.xPath.evaluate("esn", node) });
/* 132 */       if (deviceSession != null) {
/*     */         
/* 134 */         Position position = new Position(getProtocolName());
/* 135 */         position.setDeviceId(deviceSession.getDeviceId());
/*     */         
/* 137 */         position.setValid(true);
/* 138 */         position.setTime(new Date(Long.parseLong(this.xPath.evaluate("unixTime", node)) * 1000L));
/*     */         
/* 140 */         ByteBuf buf = Unpooled.wrappedBuffer(
/* 141 */             DataConverter.parseHex(this.xPath.evaluate("payload", node).substring(2)));
/*     */         
/* 143 */         int flags = buf.readUnsignedByte();
/* 144 */         position.set("in1", Boolean.valueOf(!BitUtil.check(flags, 1)));
/* 145 */         position.set("in2", Boolean.valueOf(!BitUtil.check(flags, 2)));
/* 146 */         position.set("charge", Boolean.valueOf(!BitUtil.check(flags, 3)));
/* 147 */         if (BitUtil.check(flags, 4)) {
/* 148 */           position.set("alarm", "vibration");
/*     */         }
/*     */         
/* 151 */         position.setCourse((BitUtil.from(flags, 5) * 45));
/*     */         
/* 153 */         position.setLatitude(buf.readUnsignedMedium() * 90.0D / 8388608.0D);
/* 154 */         if (position.getLatitude() > 90.0D) {
/* 155 */           position.setLatitude(position.getLatitude() - 180.0D);
/*     */         }
/*     */         
/* 158 */         position.setLongitude(buf.readUnsignedMedium() * 180.0D / 8388608.0D);
/* 159 */         if (position.getLongitude() > 180.0D) {
/* 160 */           position.setLongitude(position.getLongitude() - 360.0D);
/*     */         }
/*     */         
/* 163 */         int speed = buf.readUnsignedByte();
/* 164 */         position.setSpeed(UnitsConverter.knotsFromKph(speed));
/*     */         
/* 166 */         position.set("batteryReplace", Boolean.valueOf(BitUtil.check(buf.readUnsignedByte(), 7)));
/*     */         
/* 168 */         if (speed != 255) {
/* 169 */           positions.add(position);
/*     */         }
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 175 */     sendResponse(channel);
/* 176 */     return !positions.isEmpty() ? positions : null;
/*     */   }
/*     */ }


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\protocol\GlobalstarProtocolDecoder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */